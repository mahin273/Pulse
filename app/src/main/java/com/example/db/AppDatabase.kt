package com.example.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "scripts")
data class SavedScript(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val scriptContent: String,
    val triggerSource: String, // BOOT, SCREEN_ON, SCREEN_OFF, CHARGE, APP, TIME
    val isPinned: Boolean = false,
    val lastRunTimestamp: Long = 0L,
    val isUserCreated: Boolean = true
)

@Entity(tableName = "saved_profiles")
data class TweakProfile(
    @PrimaryKey val profileId: String, // GAMING, BATTERY, BALANCED, NIGHT, CUSTOM
    val name: String,
    val cpuGovernor: String,
    val minCpuFreqPercent: Int,
    val maxCpuFreqPercent: Int,
    val swappiness: Int,
    val lmkPreset: String, // Aggressive, Balanced, Conservative
    val zramEnabled: Boolean,
    val zramSizeMb: Int,
    val tcpCongestion: String,
    val isSystemPreset: Boolean = false
)

@Entity(tableName = "automation_rules")
data class AutomationRule(
    @PrimaryKey(autoGenerate = true) val ruleId: Int = 0,
    val triggerType: String, // BATTERY, THERMAL, SCREEN, APP, TIME, WIFI
    val triggerCondition: String, // e.g., "< 20", ">= 45"
    val actionType: String, // APPLY_PROFILE, RUN_SCRIPT, KILL_APPS, NOTIFY
    val actionArg: String, // profile name, script content, array of packagenames, etc
    val lastTriggered: Long = 0L,
    val isEnabled: Boolean = true
)

@Entity(tableName = "app_profiles")
data class AppProfile(
    @PrimaryKey val packageName: String,
    val presetMode: String, // ECO, BALANCED, PERFORMANCE, GAME
    val refreshRate: Int, // 60, 90, 120, 0 (default)
    val compileMode: String // speed, speed-profile, quicken, interpret-only
)

@Entity(tableName = "battery_logs")
data class BatteryLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val batteryPct: Int,
    val currentMa: Float,
    val voltageV: Float,
    val tempC: Float,
    val isCharging: Boolean
)

@Dao
interface PulseDao {
    // Scripts
    @Query("SELECT * FROM scripts ORDER BY isPinned DESC, id DESC")
    fun getAllScripts(): Flow<List<SavedScript>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: SavedScript)

    @Update
    suspend fun updateScript(script: SavedScript)

    @Delete
    suspend fun deleteScript(script: SavedScript)

    // Profiles
    @Query("SELECT * FROM saved_profiles")
    fun getAllProfiles(): Flow<List<TweakProfile>>

    @Query("SELECT * FROM saved_profiles WHERE profileId = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: String): TweakProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: TweakProfile)

    // Automation
    @Query("SELECT * FROM automation_rules WHERE isEnabled = 1")
    fun getActiveAutomationRules(): Flow<List<AutomationRule>>

    @Query("SELECT * FROM automation_rules")
    fun getAllAutomationRules(): Flow<List<AutomationRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomationRule(rule: AutomationRule)

    @Delete
    suspend fun deleteAutomationRule(rule: AutomationRule)

    // App Profiles
    @Query("SELECT * FROM app_profiles")
    fun getAllAppProfiles(): Flow<List<AppProfile>>

    @Query("SELECT * FROM app_profiles WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppProfile(packageName: String): AppProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppProfile(profile: AppProfile)

    @Delete
    suspend fun deleteAppProfile(profile: AppProfile)

    // Battery Logs
    @Query("SELECT * FROM battery_logs ORDER BY timestamp DESC LIMIT 500")
    fun getBatteryLogs(): Flow<List<BatteryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatteryLog(log: BatteryLog)
}

@Database(entities = [SavedScript::class, TweakProfile::class, AutomationRule::class, AppProfile::class, BatteryLog::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): PulseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pulse_optimizer_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
