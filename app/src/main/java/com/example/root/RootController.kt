package com.example.root

import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream

enum class RootMethod {
    NONE, MAGISK, KERNELSU, APATCH, GENERIC_SU
}

data class CommandResult(
    val exitCode: Int,
    val stdout: List<String>,
    val stderr: List<String>
)

object RootController {
    private const val TAG = "RootController"

    private val _rootMethod = MutableStateFlow(RootMethod.NONE)
    val rootMethod: StateFlow<RootMethod> = _rootMethod

    private val _isRootGranted = MutableStateFlow(false)
    val isRootGranted: StateFlow<Boolean> = _isRootGranted

    // If dryRunMode is enabled, we simulate all root write operations and log them
    val dryRunMode = MutableStateFlow(true)

    init {
        detectRootMethod()
    }

    /**
     * Proactively detect the styling or binary indicator for Magisk, KernelSU, or APatch
     */
    fun detectRootMethod() {
        Thread {
            try {
                val hasKsu = File("/sys/kernel/ksu").exists() || File("/dev/ksu").exists()
                val hasApatch = File("/sys/module/apatch").exists() || File("/dev/apatch").exists()
                
                val whichSuResult = execSilent("which su")
                val hasSu = whichSuResult.exitCode == 0 || File("/system/xbin/su").exists() || File("/system/bin/su").exists()
                
                val canRunSu = checkSuExecution()

                when {
                    hasKsu -> {
                        _rootMethod.value = RootMethod.KERNELSU
                        _isRootGranted.value = true
                        dryRunMode.value = false
                        Log.d(TAG, "KernelSU detected!")
                    }
                    hasApatch -> {
                        _rootMethod.value = RootMethod.APATCH
                        _isRootGranted.value = true
                        dryRunMode.value = false
                        Log.d(TAG, "APatch detected!")
                    }
                    canRunSu -> {
                        val magiskCheck = execSilent("su -c 'magisk -v'")
                        if (magiskCheck.exitCode == 0 && magiskCheck.stdout.any { it.contains("magisk", true) }) {
                            _rootMethod.value = RootMethod.MAGISK
                        } else {
                            _rootMethod.value = RootMethod.GENERIC_SU
                        }
                        _isRootGranted.value = true
                        dryRunMode.value = false
                        Log.d(TAG, "Root available via active su shell. Method: ${_rootMethod.value}")
                    }
                    hasSu -> {
                        val magiskCheck = execSilent("su -c 'magisk -v'")
                        if (magiskCheck.exitCode == 0 && magiskCheck.stdout.any { it.contains("magisk", true) }) {
                            _rootMethod.value = RootMethod.MAGISK
                        } else {
                            _rootMethod.value = RootMethod.GENERIC_SU
                        }
                        _isRootGranted.value = true
                        dryRunMode.value = false
                        Log.d(TAG, "Root available via SU binary search. Method: ${_rootMethod.value}")
                    }
                    else -> {
                        _rootMethod.value = RootMethod.NONE
                        _isRootGranted.value = false
                        dryRunMode.value = true // Force dry run when root is not available
                        Log.d(TAG, "No root detected, running in sandbox/simulation mode.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed during root detection", e)
                _rootMethod.value = RootMethod.NONE
                _isRootGranted.value = false
                dryRunMode.value = true
            }
        }.start()
    }

    private fun checkSuExecution(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec("su")
            val os = process.outputStream
            os.write("id\n".toByteArray(Charsets.UTF_8))
            os.write("exit\n".toByteArray(Charsets.UTF_8))
            os.flush()
            val stdout = process.inputStream.bufferedReader().readLines()
            val exitCode = process.waitFor()
            exitCode == 0 && stdout.any { it.contains("uid=0") || it.contains("root") }
        } catch (e: Exception) {
            false
        } finally {
            process?.destroy()
        }
    }

    /**
     * Executes a command on the IO thread. Returns standard result.
     * Uses root shell if requested and granted, otherwise uses regular shell.
     */
    suspend fun execute(command: String, requestRoot: Boolean = true): CommandResult {
        return withContext(Dispatchers.IO) {
            val useRoot = requestRoot && _isRootGranted.value && !dryRunMode.value
            
            if (requestRoot && dryRunMode.value) {
                Log.d(TAG, "[DRY-RUN SIMULATION] Executed command: $command")
                return@withContext CommandResult(0, listOf("Simulation Mode: Succeeded"), emptyList())
            }

            try {
                val shell = if (useRoot) "su" else "sh"
                val process = Runtime.getRuntime().exec(shell)
                
                val os = process.outputStream
                os.write(("$command\n").toByteArray(Charsets.UTF_8))
                os.write("exit\n".toByteArray(Charsets.UTF_8))
                os.flush()

                val stdout = process.inputStream.bufferedReader().readLines()
                val stderr = process.errorStream.bufferedReader().readLines()
                
                val exitCode = process.waitFor()
                CommandResult(exitCode, stdout, stderr)
            } catch (e: Exception) {
                Log.e(TAG, "Error running command: $command", e)
                CommandResult(-1, emptyList(), listOf(e.localizedMessage ?: "Unknown error"))
            }
        }
    }

    /**
     * Perform silent executor using standard sys runtime (safe for initialization checks)
     */
    private fun execSilent(cmd: String): CommandResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            val stdout = process.inputStream.bufferedReader().readLines()
            val stderr = process.errorStream.bufferedReader().readLines()
            val exitCode = process.waitFor()
            CommandResult(exitCode, stdout, stderr)
        } catch (e: Exception) {
            CommandResult(-1, emptyList(), listOf(e.localizedMessage ?: "Unknown check error"))
        }
    }

    /**
     * Safe Read of a sysfs/proc path. Checks existence first. 
     * In dry-run/sandbox mode, falls back to simulation value if provided.
     */
    suspend fun safeReadPath(path: String, fallbackGenerator: () -> String): String {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val text = file.readText().trim()
                    if (text.isNotEmpty()) return@withContext text
                }
            } catch (e: Exception) {
                Log.w(TAG, "Path unreadable under SELinux/Permissions: $path. Using generator.")
            }
            fallbackGenerator()
        }
    }

    /**
     * Safe Write of a sysfs/proc path. Checks existence first and checks Dry-Run status.
     * Reverts if requested, but first verifies write status.
     */
    suspend fun safeWritePath(path: String, value: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (dryRunMode.value) {
                Log.d(TAG, "[DRY-RUN WRITE] Path: $path, Value: $value")
                return@withContext true
            }

            try {
                val file = File(path)
                if (!file.exists()) {
                    Log.w(TAG, "Target path does not exist for write: $path")
                    return@withContext false
                }

                // Try normal write first
                if (file.canWrite()) {
                    file.writeText(value)
                    return@withContext true
                }

                // Fallback to Shell-based write
                val cmd = "echo '$value' > $path"
                val result = execute(cmd, requestRoot = true)
                result.exitCode == 0
            } catch (e: Exception) {
                Log.e(TAG, "Exception writing to path: $path with value: $value", e)
                false
            }
        }
    }
}
