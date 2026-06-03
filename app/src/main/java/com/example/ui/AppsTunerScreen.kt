package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.PulseViewModel
import com.example.viewmodel.AppInfo
import com.example.db.AppProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsTunerScreen(
    viewModel: PulseViewModel,
    modifier: Modifier = Modifier
) {
    val installedApps by viewModel.installedApps.collectAsState()
    val searchQuery by viewModel.appsSearchQuery.collectAsState()
    val appsTerminalOutput by viewModel.appsTerminalOutput.collectAsState()
    val accessibilityTrackerActive by viewModel.accessibilityTrackerActive.collectAsState()
    val activeAccessibilityServices by viewModel.activeAccessibilityServices.collectAsState()
    val appProfiles by viewModel.appProfiles.collectAsState(initial = emptyList())

    var showSystemApps by remember { mutableStateOf(false) }
    var expandedAppPackageName by remember { mutableStateOf<String?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val filteredApps = remember(installedApps, searchQuery, showSystemApps) {
        installedApps.filter { app ->
            (showSystemApps || !app.isSystem) &&
            (app.label.contains(searchQuery, ignoreCase = true) || app.packageName.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module title header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Apps Node Icon",
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "APPLICATION PROFILE AUTOMATOR",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Optimize foreground threads, freeze packages, extract APKs, and manage cgroups",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Accessibility active detector monitoring controls
        item {
            CardTunerContainer(title = "ACCESSIBILITY SERVICE DETECTOR MONITORS") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(text = "Accessibility Change Monitor Trigger", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Observe registered accessibility bindings to auto-reallocate heavy system resources.", color = TextSecondary, fontSize = 8.sp)
                        }
                        Switch(
                            checked = accessibilityTrackerActive,
                            onCheckedChange = { viewModel.toggleAccessibilityTracker() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Secondary,
                                checkedTrackColor = Secondary.copy(alpha = 0.4f),
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = SurfaceVariant
                            ),
                            modifier = Modifier.testTag("accessibility_tracker_switch")
                        )
                    }

                    AnimatedVisibility(
                        visible = accessibilityTrackerActive,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceVariant, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ACTIVE LICENSED ACCESSIBILITY SERVICES",
                                    color = Secondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync",
                                    tint = TextSecondary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { viewModel.refreshAccessibilityServices() }
                                )
                            }
                            
                            if (activeAccessibilityServices.isEmpty()) {
                                Text(
                                    text = "No active services detected. Standard input monitoring standing by.",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            } else {
                                activeAccessibilityServices.forEach { svc ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Success, CircleShape)
                                        )
                                        Text(
                                            text = svc,
                                            color = TextPrimary,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active app list with search filters
        item {
            CardTunerContainer(title = "APPLICATIONS OPTIMIZATION INDEX") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setAppsSearchQuery(it) },
                        placeholder = { Text("Filter package or label...", fontSize = 12.sp, color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                        textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = Primary,
                            focusedContainerColor = SurfaceVariant,
                            unfocusedContainerColor = SurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("app_search_input")
                    )

                    // Toggle System Apps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Display standard core system components",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                        Checkbox(
                            checked = showSystemApps,
                            onCheckedChange = { showSystemApps = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Primary,
                                uncheckedColor = BorderColor,
                                checkmarkColor = Color.Black
                            ),
                            modifier = Modifier.testTag("system_apps_checkbox")
                        )
                    }
                }
            }
        }

        // List of applications
        if (filteredApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No applications match current filters",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            items(filteredApps, key = { it.packageName }) { app ->
                val isExpanded = expandedAppPackageName == app.packageName
                val profile = appProfiles.find { it.packageName == app.packageName }
                
                AppTunerItemCard(
                    app = app,
                    profile = profile,
                    viewModel = viewModel,
                    isExpanded = isExpanded,
                    onExpandToggle = {
                        keyboardController?.hide()
                        expandedAppPackageName = if (isExpanded) null else app.packageName
                    },
                    onFreezeToggle = { viewModel.toggleAppFreeze(app.packageName) },
                    onEnableToggle = { viewModel.toggleAppEnable(app.packageName) },
                    onApkExtract = { viewModel.extractAppApk(app.packageName) },
                    onCgroupSelect = { grp -> viewModel.setAppCgroup(app.packageName, grp) }
                )
            }
        }

        // Live Package/Automation Console Terminal Output
        item {
            CardTunerContainer(title = "SERIAL CONSOLE APPLICATION LOGS") {
                HardwareTerminalConsole(text = appsTerminalOutput)
            }
        }

        // Standard screen buffer bottom space
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AppTunerItemCard(
    app: AppInfo,
    profile: AppProfile?,
    viewModel: PulseViewModel,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onFreezeToggle: () -> Unit,
    onEnableToggle: () -> Unit,
    onApkExtract: () -> Unit,
    onCgroupSelect: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isExpanded) Primary.copy(alpha = 0.5f) else BorderColor,
                RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onExpandToggle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Icon badge, Labels, and expand chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular icon placeholder representing the app with initials/first-char
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (app.isFrozen) Error.copy(alpha = 0.12f) else if (!app.isEnabled) BorderColor else Primary.copy(alpha = 0.12f),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (app.isFrozen) Error.copy(alpha = 0.4f) else if (!app.isEnabled) BorderColor else Primary.copy(alpha = 0.4f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (app.label.takeOrNull(1) ?: "A").uppercase(),
                        color = if (app.isFrozen) Error else if (!app.isEnabled) TextSecondary else Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.label,
                        color = if (!app.isEnabled) TextSecondary else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = app.packageName,
                        color = TextSecondary,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick state status indicators
                    if (app.isFrozen) {
                        StatusChip(text = "FREEZE", color = Error)
                    }
                    if (!app.isEnabled) {
                        StatusChip(text = "DISABLED", color = BorderColor)
                    }
                    if (app.isSystem) {
                        StatusChip(text = "SYSTEM", color = Secondary)
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand controls",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expanding custom profile adjustments
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(color = BorderColor, thickness = 1.dp)

                    // Performance Preset Selector
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "APP PROFILE SETTINGS",
                            color = Primary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Preset Mode:", color = TextSecondary, fontSize = 8.sp, modifier = Modifier.width(60.dp))
                            val modes = listOf("ECO", "BALANCED", "PERFORMANCE", "GAME")
                            modes.forEach { mode ->
                                val isSel = (profile?.presetMode ?: "BALANCED") == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSel) Primary.copy(alpha = 0.15f) else SurfaceVariant,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSel) Primary else BorderColor,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            viewModel.insertAppProfile(
                                                packageName = app.packageName,
                                                presetMode = mode,
                                                refreshRate = profile?.refreshRate ?: 0,
                                                compileMode = profile?.compileMode ?: "speed-profile"
                                            )
                                        }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = mode, color = if (isSel) Primary else TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Refresh rate override selector
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Refresh Rate:", color = TextSecondary, fontSize = 8.sp, modifier = Modifier.width(60.dp))
                            val rates = listOf(0 to "DEFAULT", 60 to "60Hz", 90 to "90Hz", 120 to "120Hz")
                            rates.forEach { (rateVal, rateLabel) ->
                                val isSel = (profile?.refreshRate ?: 0) == rateVal
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSel) Primary.copy(alpha = 0.15f) else SurfaceVariant,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSel) Primary else BorderColor,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            viewModel.insertAppProfile(
                                                packageName = app.packageName,
                                                presetMode = profile?.presetMode ?: "BALANCED",
                                                refreshRate = rateVal,
                                                compileMode = profile?.compileMode ?: "speed-profile"
                                            )
                                        }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = rateLabel, color = if (isSel) Primary else TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BorderColor, thickness = 1.dp)

                    // dex2oat compiler trigger interface
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "ART DEX2OAT COMPILER TARGET",
                            color = Warning,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val compileModes = listOf("quicken", "interpret-only", "speed-profile", "speed")
                            compileModes.forEach { mode ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(SurfaceVariant, RoundedCornerShape(6.dp))
                                        .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            viewModel.compileAppDex2oat(app.packageName, mode)
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = mode.uppercase(), color = Warning, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (profile != null) {
                        Button(
                            onClick = { viewModel.deleteAppProfile(app.packageName) },
                            colors = ButtonDefaults.buttonColors(containerColor = Error.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().height(32.dp).border(1.dp, Error.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("DELETE PROFILE OVERRIDES", color = Error, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = BorderColor, thickness = 1.dp)

                    // Details: path information
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "APK Package Path:", color = TextSecondary, fontSize = 8.sp)
                        Text(
                            text = app.apkPath.take(45) + if (app.apkPath.length > 45) "..." else "",
                            color = TextPrimary,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // CGROUPS Resource Bandwidth Allocator
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "CPU SCHEDULER CGROUP POOL ALLOCATION",
                            color = Secondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val groups = listOf("Defaults", "Low CPU Share", "High / Shielded")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            groups.forEach { grp ->
                                val isSelected = app.cgroup == grp
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) Secondary.copy(alpha = 0.15f) else SurfaceVariant,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) Secondary else BorderColor,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onCgroupSelect(grp) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = grp.uppercase(),
                                        color = if (isSelected) Secondary else TextSecondary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Multi button commands: Freeze, Disable, Extract APK
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Background process freezer (Freeze/Thaw toggle)
                        Button(
                            onClick = onFreezeToggle,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (app.isFrozen) Success else Error.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(36.dp)
                                .border(
                                    1.dp,
                                    if (app.isFrozen) Success else Error,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (app.isFrozen) Icons.Default.PlayArrow else Icons.Default.Lock,
                                    contentDescription = "Freeze status",
                                    tint = if (app.isFrozen) Color.Black else Error,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (app.isFrozen) "THAW THREAD" else "FREEZE ACTIVE",
                                    color = if (app.isFrozen) Color.Black else Error,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // App Component Enabler (pm disabler/enabler)
                        Button(
                            onClick = onEnableToggle,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!app.isEnabled) Success.copy(alpha = 0.15f) else Warning.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(36.dp)
                                .border(
                                    1.dp,
                                    if (!app.isEnabled) Success else Warning,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (!app.isEnabled) Icons.Default.CheckCircle else Icons.Default.Delete,
                                    contentDescription = "Disabler status",
                                    tint = if (!app.isEnabled) Success else Warning,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (!app.isEnabled) "ENABLE PKG" else "DISABLE PKG",
                                    color = if (!app.isEnabled) Success else Warning,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // System APK Extractor (Extract APK backing)
                        Button(
                            onClick = onApkExtract,
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Extract APK",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "EXTRACT APK",
                                    color = TextPrimary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Helper string bounds safety
private fun String.takeOrNull(n: Int): String? {
    if (this.isEmpty()) return null
    return this.take(n)
}
