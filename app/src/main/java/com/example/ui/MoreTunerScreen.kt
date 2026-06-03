package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.PulseViewModel
import com.example.db.SavedScript
import com.example.db.BatteryLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreTunerScreen(
    viewModel: PulseViewModel,
    modifier: Modifier = Modifier
) {
    // Collect ViewModel streams
    val tcpCongestion by viewModel.tcpCongestion.collectAsState()
    val ioScheduler by viewModel.ioScheduler.collectAsState()
    val readAheadKb by viewModel.readAheadKb.collectAsState()
    val resetpropName by viewModel.resetpropName.collectAsState()
    val resetpropValue by viewModel.resetpropValue.collectAsState()

    // Phase 6 States
    val gpuGovernor by viewModel.gpuGovernor.collectAsState()
    val gpuMaxFreq by viewModel.gpuMaxFreq.collectAsState()
    val adrenoBoost by viewModel.adrenoBoost.collectAsState()
    val fpsLock by viewModel.fpsLock.collectAsState()
    val thermalProfile by viewModel.thermalProfile.collectAsState()
    val fastCharging by viewModel.fastCharging.collectAsState()
    val entropyPoolSize by viewModel.entropyPoolSize.collectAsState()
    val fsyncEnabled by viewModel.fsyncEnabled.collectAsState()
    val sysctlKeyInput by viewModel.sysctlKeyInput.collectAsState()
    val sysctlValueInput by viewModel.sysctlValueInput.collectAsState()

    // Advanced Charging, SELinux, Ad-Blocker, Wakelocks States
    val isBypassChargingEnabled by viewModel.isBypassChargingEnabled.collectAsState()
    val isSmartChargingEnabled by viewModel.isSmartChargingEnabled.collectAsState()
    val smartChargingLimit by viewModel.smartChargingLimit.collectAsState()
    val selinuxEnforcing by viewModel.selinuxEnforcing.collectAsState()
    val isAdBlockerEnabled by viewModel.isAdBlockerEnabled.collectAsState()
    val blockedWakelocks by viewModel.blockedWakelocks.collectAsState()
    
    val zipIncludeCpu by viewModel.zipIncludeCpu.collectAsState()
    val zipIncludeRam by viewModel.zipIncludeRam.collectAsState()
    val zipIncludeTcp by viewModel.zipIncludeTcp.collectAsState()
    val zipIncludeScripts by viewModel.zipIncludeScripts.collectAsState()
    val zipCompilationLogs by viewModel.zipCompilationLogs.collectAsState()
    val isZipCompiling by viewModel.isZipCompiling.collectAsState()
    val moreTerminalOutput by viewModel.moreTerminalOutput.collectAsState()

    val savedScripts by viewModel.savedScriptsList.collectAsState(initial = emptyList())

    // Internal input fields for scripting
    var scriptTitleInput by remember { mutableStateOf("") }
    var scriptDescInput by remember { mutableStateOf("") }
    var scriptCodeInput by remember { mutableStateOf("") }
    var scriptTriggerInput by remember { mutableStateOf("BOOT") }

    // Toggle expand states to keep UI tidy
    var isTcpExpanded by remember { mutableStateOf(false) }
    var isIoExpanded by remember { mutableStateOf(false) }
    var isPropExpanded by remember { mutableStateOf(false) }
    var isScriptExpanded by remember { mutableStateOf(false) }
    var isZipExpanded by remember { mutableStateOf(false) }

    // Phase 6 UI expands
    var isGpuExpanded by remember { mutableStateOf(false) }
    var isThermalExpanded by remember { mutableStateOf(false) }
    var isEntropyFsyncExpanded by remember { mutableStateOf(false) }
    var isSysctlExpanded by remember { mutableStateOf(false) }
    var isSELinuxExpanded by remember { mutableStateOf(false) }
    var isAdBlockerExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module Badge Title Header
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
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Kernel Advanced Modules",
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "ADVANCED KERNELS & AUTOMATION",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Tune TCP algorithms, I/O schedulers, properties path-builders, scripting shells, and systemless APK zips",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Section 1: TCP/IP Congestion Protocols
        item {
            AdvancedCollapsibleCard(
                title = "TCP NETWORK CONGESTION ALGORITHMS",
                isExpanded = isTcpExpanded,
                onToggleExpand = { isTcpExpanded = !isTcpExpanded },
                statusBadgeText = tcpCongestion.uppercase(),
                statusBadgeColor = Primary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Optimize networking packets flow speed on cellular/Wi-Fi and select suitable congestion window sizing buffers.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    val algosList = listOf("cubic", "bbr", "reno", "westwood", "htcp")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        algosList.forEach { algo ->
                            val isSelected = tcpCongestion == algo
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Primary.copy(alpha = 0.15f) else SurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Primary else BorderColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setTcpCongestion(algo) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = algo.uppercase(),
                                    color = if (isSelected) Primary else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.applyTcpCongestion() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("apply_tcp_congestion"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("APPLY TCP NETWORK OPTIMIZATION", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 2: Block Storage I/O Schedulers
        item {
            AdvancedCollapsibleCard(
                title = "STORAGE I/O DEVICE SCHEDULERS",
                isExpanded = isIoExpanded,
                onToggleExpand = { isIoExpanded = !isIoExpanded },
                statusBadgeText = "${ioScheduler.uppercase()} (${readAheadKb}KB)",
                statusBadgeColor = Secondary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Customize disk queues algorithm disciplines and read-ahead read buffers memory limit allocations.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // Scheduler Options
                    Text("ACTIVE STORAGE SCHEDULER QUEUE", color = Secondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    val scheds = listOf("cfq", "deadline", "bfq", "noop", "kyber")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        scheds.forEach { sched ->
                            val isSelected = ioScheduler == sched
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Secondary.copy(alpha = 0.12f) else SurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Secondary else BorderColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setIoScheduler(sched) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sched.uppercase(),
                                    color = if (isSelected) Secondary else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Read Ahead Selection Options
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("READ-AHEAD BUFFER SIZE CONFIG", color = Secondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    val readAheads = listOf(128, 256, 512, 1024, 2048)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        readAheads.forEach { kb ->
                            val isSelected = readAheadKb == kb
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Secondary.copy(alpha = 0.12f) else SurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Secondary else BorderColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setReadAhead(kb) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${kb}KB",
                                    color = if (isSelected) Secondary else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.applyIoScheduler() },
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("apply_io_scheduler"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("APPLY STORAGE SCHEDULER TUNING", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 3: Resetprop Property Builder
        item {
            AdvancedCollapsibleCard(
                title = "RESETPROP READ/WRITE PROPERTIES BYPASS",
                isExpanded = isPropExpanded,
                onToggleExpand = { isPropExpanded = !isPropExpanded },
                statusBadgeText = "ACTIVE BYPASS",
                statusBadgeColor = Warning
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Modify volatile system properties safely inline (e.g., mock safety triggers, bypass safety certification constraints, and enable mock USB overlays).",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = resetpropName,
                            onValueChange = { viewModel.setResetpropName(it) },
                            label = { Text("System Property Key", fontSize = 10.sp, color = TextSecondary) },
                            textStyle = TextStyle(fontSize = 11.sp, color = TextPrimary),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Warning,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = Warning,
                                focusedContainerColor = SurfaceVariant,
                                unfocusedContainerColor = SurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(54.dp)
                                .testTag("resetprop_name_input")
                        )

                        OutlinedTextField(
                            value = resetpropValue,
                            onValueChange = { viewModel.setResetpropValue(it) },
                            label = { Text("Value", fontSize = 10.sp, color = TextSecondary) },
                            textStyle = TextStyle(fontSize = 11.sp, color = TextPrimary),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Warning,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = Warning,
                                focusedContainerColor = SurfaceVariant,
                                unfocusedContainerColor = SurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("resetprop_value_input")
                        )
                    }

                    Button(
                        onClick = { viewModel.applyResetProp() },
                        colors = ButtonDefaults.buttonColors(containerColor = Warning),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("submit_resetprop"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("PATCH VOLATILE ROM PROPERTY", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 3b: SELinux Security Context Switcher
        item {
            AdvancedCollapsibleCard(
                title = "SELINUX SECURITY CONTROLLER STATUS",
                isExpanded = isSELinuxExpanded,
                onToggleExpand = { isSELinuxExpanded = !isSELinuxExpanded },
                statusBadgeText = if (selinuxEnforcing) "ENFORCING" else "PERMISSIVE",
                statusBadgeColor = if (selinuxEnforcing) Success else Error
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Toggle Android Access Control policies (SELinux) dynamically between strict Enforcing and debug Permissive states.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleSELinux(!selinuxEnforcing) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selinuxEnforcing) "Enforcing Mode Enabled" else "Permissive Mode Active",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (selinuxEnforcing) "Security context policies are fully operational and active" else "Policy checks disabled for debugging (Caution: security risk)",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                            Switch(
                                checked = selinuxEnforcing,
                                onCheckedChange = { viewModel.toggleSELinux(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Success,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = Surface
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 3c: Systemless Hosts Ad Blocker
        item {
            AdvancedCollapsibleCard(
                title = "SYSTEMLESS HOSTS AD BLOCK TOGGLER",
                isExpanded = isAdBlockerExpanded,
                onToggleExpand = { isAdBlockerExpanded = !isAdBlockerExpanded },
                statusBadgeText = if (isAdBlockerEnabled) "BLOCKING ACTIVE" else "DISABLED",
                statusBadgeColor = if (isAdBlockerEnabled) Success else Secondary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Install custom localized loopback hosts mapping table blocks directly, bypassing network advertisements system-wide.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleAdBlocker(!isAdBlockerEnabled) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Systemless Hosts Blocker",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Diverts telemetry & advertisement host domains to null socket loops",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                            Switch(
                                checked = isAdBlockerEnabled,
                                onCheckedChange = { viewModel.toggleAdBlocker(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Success,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = Surface
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Bash Syntax-Highlighted Scripting Terminal
        item {
            AdvancedCollapsibleCard(
                title = "SYNTAX-HIGHLIGHTED AUTOMATION SCRIPTING",
                isExpanded = isScriptExpanded,
                onToggleExpand = { isScriptExpanded = !isScriptExpanded },
                statusBadgeText = "${savedScripts.size} ACTIVE SCRIPT${if (savedScripts.size == 1) "" else "S"}",
                statusBadgeColor = Success
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Build and record local custom automation blocks triggered on Boot, screen toggles, battery rates, or run inline.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // Editing form card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(10.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("SHELL SCRIPT BLUEPRINT BUILDER", color = Success, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        
                        OutlinedTextField(
                            value = scriptTitleInput,
                            onValueChange = { scriptTitleInput = it },
                            placeholder = { Text("Script title...", fontSize = 11.sp, color = TextSecondary) },
                            textStyle = TextStyle(fontSize = 11.sp, color = TextPrimary),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Success,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = Success
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("script_title_field")
                        )

                        OutlinedTextField(
                            value = scriptDescInput,
                            onValueChange = { scriptDescInput = it },
                            placeholder = { Text("Brief description of actions...", fontSize = 11.sp, color = TextSecondary) },
                            textStyle = TextStyle(fontSize = 11.sp, color = TextPrimary),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Success,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = Success
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("script_desc_field")
                        )

                        // Trigger Source Row Selection
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("AUTOMATION TRIGGER BINDING SOURCE", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            val triggers = listOf("BOOT", "SCREEN_ON", "SCREEN_OFF", "CHARGE")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                triggers.forEach { trig ->
                                    val isPicked = scriptTriggerInput == trig
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isPicked) Success.copy(alpha = 0.15f) else Surface,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isPicked) Success else BorderColor,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { scriptTriggerInput = trig }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = trig,
                                            color = if (isPicked) Success else TextSecondary,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Multiline Code Editor
                        OutlinedTextField(
                            value = scriptCodeInput,
                            onValueChange = { scriptCodeInput = it },
                            placeholder = { 
                                Text(
                                    "echo \"Initializing...\"\nwrite /sys/devices/system/cpu/cpu0/online 1\necho \"Core online.\"", 
                                    fontSize = 10.sp, 
                                    fontFamily = FontFamily.Monospace, 
                                    color = TextSecondary
                                ) 
                            },
                            textStyle = TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Success,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = Success
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("script_code_field")
                        )

                        // Save and Run Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { 
                                    viewModel.saveCustomScript(scriptTitleInput, scriptDescInput, scriptCodeInput, scriptTriggerInput)
                                    scriptTitleInput = ""
                                    scriptDescInput = ""
                                    scriptCodeInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Success.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .border(1.dp, Success, RoundedCornerShape(8.dp))
                                    .testTag("save_script_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Success, modifier = Modifier.size(14.dp))
                                    Text("SAVE TO DATABASE", color = Success, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { viewModel.runScriptInline(scriptTitleInput.takeIf { it.isNotBlank() } ?: "Workbench", scriptCodeInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = Success),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("execute_inline_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Execute", tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Text("RUN CODE INLINE", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Saved DB Scripts Listing
                    Text("SAVED DATABASE REPOSITORY MODULES", color = Success, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    
                    if (savedScripts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceVariant, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No personal scripts registered. Populate editor above.", color = TextSecondary, fontSize = 9.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            savedScripts.forEach { script ->
                                SavedScriptItemCard(
                                    script = script,
                                    onRunClick = { viewModel.runScriptInline(script.title, script.scriptContent) },
                                    onDeleteClick = { viewModel.deleteScript(script) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Systemless Magisk Module ZIP compiler compilation
        item {
            AdvancedCollapsibleCard(
                title = "SYSTEMLESS MAGISK ARCHIVE ZIP COMPILER",
                isExpanded = isZipExpanded,
                onToggleExpand = { isZipExpanded = !isZipExpanded },
                statusBadgeText = if (isZipCompiling) "COMPILING" else "STANDBY",
                statusBadgeColor = if (isZipCompiling) Warning else Secondary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Bundle active system adjustments and user-defined background loop scripts into a fully-flashable, systemless zip package structure.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // Export Option checkboxes
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("ELECTION OPTION PARAMETERS", color = Secondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)

                        CheckboxOptionLink(
                            label = "Inject Custom CPU Core Affinity Governors",
                            checked = zipIncludeCpu,
                            onCheckedChange = { viewModel.setZipIncludeCpu(it) }
                        )

                        CheckboxOptionLink(
                            label = "Inject Memory Swappiness Swapping Limits & ZRAM Configs",
                            checked = zipIncludeRam,
                            onCheckedChange = { viewModel.setZipIncludeRam(it) }
                        )

                        CheckboxOptionLink(
                            label = "Inject TCP Congestion Speed Sizing Buffers",
                            checked = zipIncludeTcp,
                            onCheckedChange = { viewModel.setZipIncludeTcp(it) }
                        )

                        CheckboxOptionLink(
                            label = "Include Database Shell Scripts inside service.sh tasks",
                            checked = zipIncludeScripts,
                            onCheckedChange = { viewModel.setZipIncludeScripts(it) }
                        )
                    }

                    // Progress Compiler Board Terminal Console
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Text(
                                    text = zipCompilationLogs,
                                    color = if (zipCompilationLogs.contains("SUCCESSFUL")) Success else if (isZipCompiling) Warning else TextSecondary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }

                    // Compile execution button
                    Button(
                        onClick = { viewModel.compileMagiskZip() },
                        enabled = !isZipCompiling,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Secondary,
                            disabledContainerColor = SurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .border(1.dp, if (isZipCompiling) BorderColor else Secondary, RoundedCornerShape(8.dp))
                            .testTag("compile_zip_btn"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isZipCompiling) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Warning, strokeWidth = 2.dp)
                                Text("COMPILING Payloads ZIP...", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("COMPILE MAGISK OPTIMIZATION MODULE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 6: GPU & Display Refresh Rate Tuner
        item {
            AdvancedCollapsibleCard(
                title = "GPU & DISPLAY REFRESH RATE TUNER",
                isExpanded = isGpuExpanded,
                onToggleExpand = { isGpuExpanded = !isGpuExpanded },
                statusBadgeText = "${gpuGovernor.uppercase()} / ${fpsLock}HZ",
                statusBadgeColor = Primary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Customize the integrated graphics co-processor (GPU) governors, limits, and lock the system display frame-rate pipeline.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // GPU Governor Row
                    Text("GPU GOVERNOR OVERRIDE", color = Primary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    val gpuGovs = listOf("msm-adreno-tz", "simple_ondemand", "performance", "powersave")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        gpuGovs.forEach { gov ->
                            val isSelected = gpuGovernor == gov
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Primary.copy(alpha = 0.15f) else SurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Primary else BorderColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setGpuGovernor(gov) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (gov == "msm-adreno-tz") "ADRENO" else gov.substringBefore("_").uppercase(),
                                    color = if (isSelected) Primary else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // GPU Frequency Selector
                    Text("MAX GPU FREQUENCY LOCK LIMIT", color = Primary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    val gpuFreqs = listOf(300, 450, 600, 800)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        gpuFreqs.forEach { freq ->
                            val isSelected = gpuMaxFreq == freq
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Primary.copy(alpha = 0.15f) else SurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Primary else BorderColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setGpuMaxFreq(freq) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${freq} MHz",
                                    color = if (isSelected) Primary else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Display Refresh Frame Lock
                    Text("DISPLAY FRAME REFRESH RATE LOCK", color = Primary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    val fpsOptions = listOf(60, 90, 120)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        fpsOptions.forEach { fps ->
                            val isSelected = fpsLock == fps
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Primary.copy(alpha = 0.15f) else SurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Primary else BorderColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setFpsLock(fps) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${fps} Hz",
                                    color = if (isSelected) Primary else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Adreno Boost Switch Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setAdrenoBoost(!adrenoBoost) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Aesthetic Adreno Boost Optimizer", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Dynamic scaling during frame drop transients", color = TextSecondary, fontSize = 9.sp)
                            }
                            Switch(
                                checked = adrenoBoost,
                                onCheckedChange = { viewModel.setAdrenoBoost(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Primary,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = Surface
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.applyGpuTuning() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("submit_gpu_tuning"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("APPLY HARDWARE GRAPHICS TUNING", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 7: Thermal & Battery Governor
        item {
            AdvancedCollapsibleCard(
                title = "THERMAL PROFILES & BATTERY CURRENT GOVERNOR",
                isExpanded = isThermalExpanded,
                onToggleExpand = { isThermalExpanded = !isThermalExpanded },
                statusBadgeText = thermalProfile.uppercase(),
                statusBadgeColor = Secondary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Calibrate the device thermal cooling envelope under workloads, and configure charging current controllers.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // Thermal Preset Chips
                    Text("ACTIVE THERMAL DE-THROTTLING PRESETS", color = Secondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    val profiles = listOf("Cool", "Balanced", "Gaming", "Extreme")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        profiles.forEach { prof ->
                            val isSelected = thermalProfile == prof
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Secondary.copy(alpha = 0.15f) else SurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Secondary else BorderColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setThermalProfile(prof) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prof.uppercase(),
                                    color = if (isSelected) Secondary else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Hyper Fast Charging Switch Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setFastCharging(!fastCharging) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Intelligent PD Force fast charger policy", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Bypasses default 5V trickle limit safety lock", color = TextSecondary, fontSize = 9.sp)
                            }
                            Switch(
                                checked = fastCharging,
                                onCheckedChange = { viewModel.setFastCharging(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Secondary,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = Surface
                                )
                            )
                        }
                    }

                    // Live telemetry info board
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("ACTIVE POWER METRICS TELEMETRY FEED", color = Secondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Forced power state:", color = TextSecondary, fontSize = 9.sp)
                                Text(if (fastCharging) "pd_high_current_18w" else "sb_trickle_5w", color = Success, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Thermal throttle profile:", color = TextSecondary, fontSize = 9.sp)
                                Text("env_${thermalProfile.lowercase()}_cool", color = Secondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.applyThermalTuning() },
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("apply_thermal_tuning"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("APPLY THERMAL THERMODYNAMICS MATRIX", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 8: Entropy & Fsync
        item {
            AdvancedCollapsibleCard(
                title = "ENTROPY RANDOMIZER & FSYNC COMPILER ENGINE",
                isExpanded = isEntropyFsyncExpanded,
                onToggleExpand = { isEntropyFsyncExpanded = !isEntropyFsyncExpanded },
                statusBadgeText = if (fsyncEnabled) "FSYNC: SAFE" else "FSYNC: DISABLED",
                statusBadgeColor = Warning
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Customize the Linux Kernel random pool generator seeds feed and choose SQLite synchronizations (Fsync) state options.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // Entropy selector row
                    Text("ENTROPY GENERATOR WAKEUP READ THRESHOLD", color = Warning, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    val readWakeups = listOf("Standard (256)", "High (1024)", "Maximum (4096)")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        readWakeups.forEach { opt ->
                            val isSelected = entropyPoolSize == opt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Warning.copy(alpha = 0.15f) else SurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Warning else BorderColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setEntropyPoolSize(opt) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opt,
                                    color = if (isSelected) Warning else TextSecondary,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Fsync switch box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setFsyncEnabled(!fsyncEnabled) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Hardware Storage Fsync Engine", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Disabling improves I/O speeds but risks volatile corruption on panics", color = TextSecondary, fontSize = 9.sp)
                            }
                            Switch(
                                checked = fsyncEnabled,
                                onCheckedChange = { viewModel.setFsyncEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Warning,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = Surface
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.applyEntropyFsyncTuning() },
                        colors = ButtonDefaults.buttonColors(containerColor = Warning),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("apply_entropy_fsync"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("APPLY SYSTEM OPTIMIZATION BUFFER OPTIONS", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 9: Sysctl Kernel variables listing Explorer/Modifier
        item {
            AdvancedCollapsibleCard(
                title = "SYSCTL RUNTIME COMPILER VARIATOR",
                isExpanded = isSysctlExpanded,
                onToggleExpand = { isSysctlExpanded = !isSysctlExpanded },
                statusBadgeText = "SYSCTL ENGINE",
                statusBadgeColor = Success
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Modify core sysctl virtual memory, fs limits, sched latency, and networking parameter maps directly. Click standard shortcuts below to auto-populate.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // Quick buttons grid
                    val sysctlShortcuts = listOf(
                        "kernel.panic" to "10",
                        "kernel.sched_latency_ns" to "12000000",
                        "fs.file-max" to "2097152",
                        "vm.dirty_background_ratio" to "5"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("PRE-BUILT SYSCTL TUNING KEYSHORTCUTS", color = Success, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sysctlShortcuts.forEach { (key, value) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(SurfaceVariant, RoundedCornerShape(8.dp))
                                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.setSysctlKey(key)
                                            viewModel.setSysctlValue(value)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(key.substringAfter("."), color = Success, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                        Text(value, color = TextPrimary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }

                    // Key-Value Editing row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sysctlKeyInput,
                            onValueChange = { viewModel.setSysctlKey(it) },
                            label = { Text("Sysctl Key", fontSize = 10.sp, color = TextSecondary) },
                            textStyle = TextStyle(fontSize = 11.sp, color = TextPrimary),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Success,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = Success,
                                focusedContainerColor = SurfaceVariant,
                                unfocusedContainerColor = SurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(54.dp)
                                .testTag("sysctl_key_input")
                        )

                        OutlinedTextField(
                            value = sysctlValueInput,
                            onValueChange = { viewModel.setSysctlValue(it) },
                            label = { Text("Value", fontSize = 10.sp, color = TextSecondary) },
                            textStyle = TextStyle(fontSize = 11.sp, color = TextPrimary),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Success,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = Success,
                                focusedContainerColor = SurfaceVariant,
                                unfocusedContainerColor = SurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("sysctl_value_input")
                        )
                    }

                    Button(
                        onClick = { viewModel.applyCustomSysctl() },
                        colors = ButtonDefaults.buttonColors(containerColor = Success),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("apply_custom_sysctl"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("PATCH LINUX SYSTEM COMPILER MATRIX", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 10: KCAL Display Calibrator
        item {
            val rVal by viewModel.kcalRed.collectAsState()
            val gVal by viewModel.kcalGreen.collectAsState()
            val bVal by viewModel.kcalBlue.collectAsState()
            val satVal by viewModel.kcalSaturation.collectAsState()
            val contVal by viewModel.kcalContrast.collectAsState()
            val hueVal by viewModel.kcalHue.collectAsState()
            val valVal by viewModel.kcalValue.collectAsState()
            var isKcalExp by remember { mutableStateOf(false) }

            AdvancedCollapsibleCard(
                title = "KCAL COLOR CALIBRATOR",
                isExpanded = isKcalExp,
                onToggleExpand = { isKcalExp = !isKcalExp },
                statusBadgeText = "KCAL DRIVER",
                statusBadgeColor = Primary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Calibrate screen R/G/B channel multipliers, saturation level, and contrast ranges.", color = TextSecondary, fontSize = 10.sp)

                    Text("DISPLAY PROFILE PRESETS", color = Primary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    val presets = listOf("sRGB", "DCI-P3", "AMOLED Vibrant", "Warm Cinema", "Cold Tech")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.applyKcalPreset(preset) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = preset,
                                    color = Primary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Red Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Red Channel", color = TextPrimary, fontSize = 9.sp)
                            Text("${rVal.toInt()}", color = Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = rVal, onValueChange = { viewModel.setKcalRed(it) }, valueRange = 0f..256f, colors = SliderDefaults.colors(activeTrackColor = Primary, thumbColor = Primary), modifier = Modifier.height(24.dp))
                    }

                    // Green Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Green Channel", color = TextPrimary, fontSize = 9.sp)
                            Text("${gVal.toInt()}", color = Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = gVal, onValueChange = { viewModel.setKcalGreen(it) }, valueRange = 0f..256f, colors = SliderDefaults.colors(activeTrackColor = Primary, thumbColor = Primary), modifier = Modifier.height(24.dp))
                    }

                    // Blue Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Blue Channel", color = TextPrimary, fontSize = 9.sp)
                            Text("${bVal.toInt()}", color = Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = bVal, onValueChange = { viewModel.setKcalBlue(it) }, valueRange = 0f..256f, colors = SliderDefaults.colors(activeTrackColor = Primary, thumbColor = Primary), modifier = Modifier.height(24.dp))
                    }

                    // Saturation
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Saturation", color = TextPrimary, fontSize = 9.sp)
                            Text("${satVal.toInt()}", color = Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = satVal, onValueChange = { viewModel.setKcalSaturation(it) }, valueRange = 0f..256f, colors = SliderDefaults.colors(activeTrackColor = Primary, thumbColor = Primary), modifier = Modifier.height(24.dp))
                    }

                    // Contrast
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Contrast", color = TextPrimary, fontSize = 9.sp)
                            Text("${contVal.toInt()}", color = Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = contVal, onValueChange = { viewModel.setKcalContrast(it) }, valueRange = 0f..256f, colors = SliderDefaults.colors(activeTrackColor = Primary, thumbColor = Primary), modifier = Modifier.height(24.dp))
                    }

                    // Value
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Value (Brightness)", color = TextPrimary, fontSize = 9.sp)
                            Text("${valVal.toInt()}", color = Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = valVal, onValueChange = { viewModel.setKcalValue(it) }, valueRange = 0f..256f, colors = SliderDefaults.colors(activeTrackColor = Primary, thumbColor = Primary), modifier = Modifier.height(24.dp))
                    }

                    // Hue
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Hue Matrix", color = TextPrimary, fontSize = 9.sp)
                            Text("${hueVal.toInt()}", color = Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = hueVal, onValueChange = { viewModel.setKcalHue(it) }, valueRange = 0f..360f, colors = SliderDefaults.colors(activeTrackColor = Primary, thumbColor = Primary), modifier = Modifier.height(24.dp))
                    }

                    Button(
                        onClick = { viewModel.applyKcal() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("APPLY KCAL MULTIPLIERS", color = Color.Black, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 11: Sound Amplifier Controls
        item {
            val spkGain by viewModel.soundSpeakerGain.collectAsState()
            val hpGain by viewModel.soundHeadphoneGain.collectAsState()
            val micGain by viewModel.soundMicGain.collectAsState()
            var isSoundExp by remember { mutableStateOf(false) }

            AdvancedCollapsibleCard(
                title = "SOUND DIGITAL GAIN CONTROLLER",
                isExpanded = isSoundExp,
                onToggleExpand = { isSoundExp = !isSoundExp },
                statusBadgeText = "SOUND CONTROL",
                statusBadgeColor = Secondary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Modify DAC speaker, headphone, and microphone analog/digital gains safely.", color = TextSecondary, fontSize = 10.sp)

                    // Speaker Gain
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Speaker Boost", color = TextPrimary, fontSize = 9.sp)
                            Text(String.format("%+d dB", spkGain.toInt()), color = Secondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = spkGain, onValueChange = { viewModel.setSoundSpeakerGain(it) }, valueRange = -20f..20f, colors = SliderDefaults.colors(activeTrackColor = Secondary, thumbColor = Secondary), modifier = Modifier.height(24.dp))
                    }

                    // Headphone Gain
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Headphone Gain", color = TextPrimary, fontSize = 9.sp)
                            Text(String.format("%+d dB", hpGain.toInt()), color = Secondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = hpGain, onValueChange = { viewModel.setSoundHeadphoneGain(it) }, valueRange = -20f..20f, colors = SliderDefaults.colors(activeTrackColor = Secondary, thumbColor = Secondary), modifier = Modifier.height(24.dp))
                    }

                    // Mic Gain
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Microphone Boost", color = TextPrimary, fontSize = 9.sp)
                            Text(String.format("%+d dB", micGain.toInt()), color = Secondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = micGain, onValueChange = { viewModel.setSoundMicGain(it) }, valueRange = -20f..20f, colors = SliderDefaults.colors(activeTrackColor = Secondary, thumbColor = Secondary), modifier = Modifier.height(24.dp))
                    }

                    Button(
                        onClick = { viewModel.applySoundTuning() },
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("APPLY DAC AMPLIFIER GAIN", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 12: Wakelocks List
        item {
            val wakelocks by viewModel.wakelocksList.collectAsState()
            var isWakelocksExp by remember { mutableStateOf(false) }

            AdvancedCollapsibleCard(
                title = "WAKELOCKS & SUSPEND STATE ANALYZER",
                isExpanded = isWakelocksExp,
                onToggleExpand = { isWakelocksExp = !isWakelocksExp },
                statusBadgeText = "${wakelocks.size} SOURCES",
                statusBadgeColor = Warning
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Identify kernel and platform wake locks preventing system sleep and draining power.", color = TextSecondary, fontSize = 10.sp)

                    if (wakelocks.isEmpty()) {
                        Text("Searching sleep block processes...", color = TextSecondary, fontSize = 9.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            wakelocks.take(8).forEach { wl ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceVariant, RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(wl.name, color = TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("Wakeups: ${wl.wakeupCount} • Expires: ${wl.expireCount}", color = TextSecondary, fontSize = 8.sp)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = if (wl.activeTimeMs >= 1000) String.format("%.2fs", wl.activeTimeMs / 1000f) else "${wl.activeTimeMs}ms",
                                            color = Warning,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        IconButton(
                                            onClick = { viewModel.toggleWakelockBlock(wl.name) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            val isBlocked = blockedWakelocks.contains(wl.name)
                                            Icon(
                                                imageVector = Icons.Default.Block,
                                                contentDescription = if (isBlocked) "Unblock Wakelock" else "Block Wakelock",
                                                tint = if (isBlocked) Error else TextSecondary.copy(alpha = 0.4f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 13: Battery Wear Logging & Analytics
        item {
            val logs by viewModel.batteryLogs.collectAsState(initial = emptyList())
            val batteryHealth by viewModel.batteryHealth.collectAsState()
            val batteryCycles by viewModel.batteryCycles.collectAsState()
            var isBatteryExp by remember { mutableStateOf(false) }

            AdvancedCollapsibleCard(
                title = "BATTERY DEGRADATION & CYCLES DIAGNOSTICS",
                isExpanded = isBatteryExp,
                onToggleExpand = { isBatteryExp = !isBatteryExp },
                statusBadgeText = "HEALTH: $batteryHealth%",
                statusBadgeColor = Success
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Examine charging metrics log databases to estimate cycle wear degradation patterns.", color = TextSecondary, fontSize = 10.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SurfaceVariant, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ESTIMATED HEALTH", color = TextSecondary, fontSize = 8.sp)
                                Text("$batteryHealth%", color = Success, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Wear: ${100 - batteryHealth}%", color = TextSecondary, fontSize = 8.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SurfaceVariant, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("CHARGE CYCLES", color = TextSecondary, fontSize = 8.sp)
                                Text("$batteryCycles", color = Success, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Standard baseline", color = TextSecondary, fontSize = 8.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = BorderColor, thickness = 1.dp)

                    Text("ADVANCED BATTERY PROTECTION RULES", color = Success, fontSize = 8.sp, fontWeight = FontWeight.Bold)

                    // Bypass Charging Switch Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleBypassCharging(!isBypassChargingEnabled) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bypass Charging Mode", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Routes power directly to board, bypassing battery to reduce heat & wear", color = TextSecondary, fontSize = 9.sp)
                            }
                            Switch(
                                checked = isBypassChargingEnabled,
                                onCheckedChange = { viewModel.toggleBypassCharging(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Success,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = Surface
                                )
                            )
                        }
                    }

                    // Smart Charging Switch Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleSmartCharging(!isSmartChargingEnabled) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Smart Charge Protection", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Automatically cuts charge current when limit threshold is reached", color = TextSecondary, fontSize = 9.sp)
                            }
                            Switch(
                                checked = isSmartChargingEnabled,
                                onCheckedChange = { viewModel.toggleSmartCharging(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Success,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = Surface
                                )
                            )
                        }
                    }

                    if (isSmartChargingEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceVariant, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Smart Charge Limit", color = TextPrimary, fontSize = 10.sp)
                                Text("$smartChargingLimit%", color = Success, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = smartChargingLimit.toFloat(),
                                onValueChange = { viewModel.setSmartChargingLimit(it.toInt()) },
                                valueRange = 50f..95f,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Success,
                                    thumbColor = Success
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("TELEMETRY LOG ENTRIES (${logs.size.coerceAtMost(5)} of ${logs.size})", color = Success, fontSize = 8.sp, fontWeight = FontWeight.Bold)

                    if (logs.isEmpty()) {
                        Text("No battery entries logged yet. Standing by...", color = TextSecondary, fontSize = 9.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            logs.take(5).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceVariant, RoundedCornerShape(6.dp))
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${log.batteryPct}% @ ${log.tempC}°C", color = TextPrimary, fontSize = 8.5.sp)
                                    Text("${log.voltageV}V • ${log.currentMa.toInt()}mA", color = TextSecondary, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 14: Boot.img Partition Flasher
        item {
            val flasherLogs by viewModel.flasherLogs.collectAsState()
            val isWorking by viewModel.isFlasherWorking.collectAsState()
            var isFlasherExp by remember { mutableStateOf(false) }
            var imagePathInput by remember { mutableStateOf("") }

            AdvancedCollapsibleCard(
                title = "PARTITION KERNEL FLASH ENGINE",
                isExpanded = isFlasherExp,
                onToggleExpand = { isFlasherExp = !isFlasherExp },
                statusBadgeText = "BOOT FLASHER",
                statusBadgeColor = Warning
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Backup default boot.img blocks, or rewrite boot kernel images to flash custom scripts.", color = TextSecondary, fontSize = 10.sp)

                    Button(
                        onClick = { viewModel.backupBootPartition() },
                        enabled = !isWorking,
                        colors = ButtonDefaults.buttonColors(containerColor = Warning.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp).border(1.dp, Warning, RoundedCornerShape(8.dp))
                    ) {
                        Text("BACKUP DEVICE CURRENT BOOT PARTITION", color = Warning, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = BorderColor, thickness = 1.dp)

                    OutlinedTextField(
                        value = imagePathInput,
                        onValueChange = { imagePathInput = it },
                        placeholder = { Text("Absolute path to kernel .img (e.g. /sdcard/boot.img)", fontSize = 11.sp, color = TextSecondary) },
                        textStyle = TextStyle(fontSize = 11.sp, color = TextPrimary, fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Warning, cursorColor = Warning),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )

                    Button(
                        onClick = { viewModel.flashBootPartition(imagePathInput) },
                        enabled = !isWorking && imagePathInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Warning),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("FLASH BOOT PARTITION WITH TARGET IMAGE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Flasher log output console
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Text(
                                    text = flasherLogs,
                                    color = if (flasherLogs.contains("[Success]")) Success else if (isWorking) Warning else TextSecondary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Advanced Module terminal logs window
        item {
            CardTunerContainer(title = "SERIAL CONSOLE OPERATIONS SYSTEM LOGS") {
                HardwareTerminalConsole(text = moreTerminalOutput)
            }
        }

        // Bottom space buffer
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AdvancedCollapsibleCard(
    title: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    statusBadgeText: String,
    statusBadgeColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isExpanded) statusBadgeColor.copy(alpha = 0.5f) else BorderColor,
                RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggleExpand)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row of Collapsible card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(statusBadgeColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, statusBadgeColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusBadgeText,
                            color = statusBadgeColor,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Tweak Collapsible Action",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = BorderColor, thickness = 1.dp)
                    content()
                }
            }
        }
    }
}

@Composable
fun SavedScriptItemCard(
    script: SavedScript,
    onRunClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariant, RoundedCornerShape(8.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(script.title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .background(Success.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(script.triggerSource, color = Success, fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (script.description.isNotEmpty()) {
                Text(script.description, color = TextSecondary, fontSize = 8.5.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = script.scriptContent.take(35) + if (script.scriptContent.length > 35) "..." else "",
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                maxLines = 1
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Success.copy(alpha = 0.15f), CircleShape)
                    .clip(CircleShape)
                    .clickable { onRunClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Run script", tint = Success, modifier = Modifier.size(14.dp))
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Error.copy(alpha = 0.15f), CircleShape)
                    .clip(CircleShape)
                    .clickable { onDeleteClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete script", tint = Error, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun CheckboxOptionLink(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Secondary,
                uncheckedColor = BorderColor,
                checkmarkColor = Color.White
            ),
            modifier = Modifier.size(20.dp)
        )
    }
}
