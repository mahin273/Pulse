package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiveGauge
import com.example.ui.theme.*
import com.example.viewmodel.PulseViewModel

@Composable
fun RamTunerScreen(
    viewModel: PulseViewModel,
    modifier: Modifier = Modifier
) {
    val systemState by viewModel.uiState.collectAsState()
    
    // VM parameters
    val vmSwappiness by viewModel.vmSwappiness.collectAsState()
    val vfsCachePressure by viewModel.vfsCachePressure.collectAsState()

    // zRAM config
    val zramCompressor by viewModel.zramCompressor.collectAsState()
    val zramSizeMb by viewModel.zramSizeMb.collectAsState()
    val zramEnabled by viewModel.zramEnabled.collectAsState()

    // LMK
    val lmkPreset by viewModel.lmkPreset.collectAsState()

    // OOM score
    val oomPidTarget by viewModel.oomPidTarget.collectAsState()
    val customOomPid by viewModel.customOomPid.collectAsState()
    val oomScoreAdj by viewModel.oomScoreAdj.collectAsState()

    // Console output
    val ramTerminalOutput by viewModel.ramTerminalOutput.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    // Calculated RAM percentage
    val totalRam = systemState?.totalRamMb ?: 8192f
    val usedRam = systemState?.usedRamMb ?: 4120f
    val ramPercent = if (totalRam > 0) (usedRam / totalRam) * 100f else 50f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Node Title
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
                        imageVector = Icons.Default.Info,
                        contentDescription = "RAM Swap Icon",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "VIRTUAL MEMORY SUPERVISOR",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Real-time swap control & process level OOM prioritization",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 1. DETAILED HEAP MONITORING (LIVE GAUGE AND ALLOCATIONS)
        item {
            CardTunerContainer(title = "CORE MEMORY ALLOCATION INDEX") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Live Gauge
                        LiveGauge(
                            label = "RAM Load",
                            value = ramPercent,
                            color = Primary,
                            unit = "%",
                            secondaryLabel = String.format("%.0f / %.0f MB", usedRam, totalRam),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(180.dp)
                        )

                        // Right: Visual distributions
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MemoryInfoRow(label = "Active (Working)", sizeMb = (usedRam * 0.65f).toInt(), color = Primary)
                            MemoryInfoRow(label = "Inactive (Cached)", sizeMb = (usedRam * 0.35f).toInt(), color = Secondary)
                            MemoryInfoRow(
                                label = "Virtual Swap", 
                                sizeMb = if (zramEnabled) (zramSizeMb * 0.42f).toInt() else 0, 
                                color = Warning
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { viewModel.refreshMemInfo() },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderColor),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("refresh_meminfo_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text("READ MEMINFO", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. VM SWAPPINESS & VFS_CACHE_PRESSURE KNOBS
        item {
            CardTunerContainer(title = "KERNEL SWAPPINESS & FLUSH PRESSURE") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Customize swap aggression vs physical memory compression thresholds dynamically.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // Swappiness Slider
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "VM Swappiness Threshold", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${vmSwappiness.toInt()}%", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            text = "0 = Maximize RAM retention; 100 = Compress unused processes rapidly.",
                            color = TextSecondary,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Slider(
                            value = vmSwappiness,
                            onValueChange = { viewModel.setSwappiness(it) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                activeTrackColor = Primary,
                                inactiveTrackColor = BorderColor,
                                thumbColor = Primary
                            ),
                            modifier = Modifier.fillMaxWidth().height(24.dp).testTag("swappiness_slider")
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Cache Pressure Slider
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Directory Cache Pressure", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${vfsCachePressure.toInt()}", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            text = "Determines how aggressively the kernel reclaims VFS cache inodes.",
                            color = TextSecondary,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Slider(
                            value = vfsCachePressure,
                            onValueChange = { viewModel.setCachePressure(it) },
                            valueRange = 10f..200f,
                            colors = SliderDefaults.colors(
                                activeTrackColor = Primary,
                                inactiveTrackColor = BorderColor,
                                thumbColor = Primary
                            ),
                            modifier = Modifier.fillMaxWidth().height(24.dp).testTag("cache_pressure_slider")
                        )
                    }

                    Button(
                        onClick = { viewModel.applySwappinessAndCachePressure() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("apply_vm_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Done, contentDescription = "Apply VM", modifier = Modifier.size(16.dp))
                            Text("APPLY SYS VM TUNINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. zRAM SWAP COMPRESS-ALGORITHM TOGGLER
        item {
            CardTunerContainer(title = "ZRAM SWAP SPACE POOL") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Enable Virtual zRAM Compression", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Create real-time memory backing swap partitions.", color = TextSecondary, fontSize = 8.sp)
                        }
                        Switch(
                            checked = zramEnabled,
                            onCheckedChange = { viewModel.toggleZramEnabled() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Secondary,
                                checkedTrackColor = Secondary.copy(alpha = 0.4f),
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = SurfaceVariant
                            ),
                            modifier = Modifier.testTag("zram_enabled_switch")
                        )
                    }

                    AnimatedVisibility(
                        visible = zramEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Compressor selection
                            Text(text = "SWAP COMPRESSION ALGORITHM", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            val algorithms = listOf("zstd", "lz4", "lzo", "deflate")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                algorithms.forEach { alg ->
                                    val isSelected = zramCompressor == alg
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
                                            .clickable { viewModel.setZramCompressor(alg) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = alg,
                                            color = if (isSelected) Secondary else TextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // RAM Expansion size
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Swap Partition Allocations", color = TextPrimary, fontSize = 10.sp)
                                    Text(text = "$zramSizeMb MB", color = Secondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                Slider(
                                    value = zramSizeMb.toFloat(),
                                    onValueChange = { viewModel.setZramSize(it.toInt()) },
                                    valueRange = 1024f..8192f,
                                    steps = 6, // Locks increments to regular MB values (1GB, 2GB, 3GB etc)
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = Secondary,
                                        inactiveTrackColor = BorderColor,
                                        thumbColor = Secondary
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(24.dp).testTag("zram_size_slider")
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.applyZramConfig() },
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("apply_zram_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Apply zRAM", modifier = Modifier.size(16.dp))
                            Text("INITIALIZE BACKING SWAP COMPRESSION", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. LOWMEMORYKILLER PRESETS
        item {
            CardTunerContainer(title = "LOW MEMORY KILLER (LMK) PRESET PROFILES") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Fine-tune system background app termination triggers to allocate free space.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    val presets = listOf("Conservative", "Balanced", "Aggressive")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            val isSelected = lmkPreset == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Success.copy(alpha = 0.15f) else SurfaceVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Success else BorderColor,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setLmkPreset(preset) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = preset.uppercase(),
                                        color = if (isSelected) Success else TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when(preset) {
                                            "Conservative" -> "Preserve Apps"
                                            "Aggressive" -> "Maximum RAM"
                                            else -> "Default Stock"
                                        },
                                        color = TextSecondary,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.applyLmkPreset() },
                        colors = ButtonDefaults.buttonColors(containerColor = Success),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("apply_lmk_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Apply LMK", modifier = Modifier.size(16.dp))
                            Text("UPDATE MEMORY RECLAIM PRIORITIES", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. OOM PROCESS TARGET SCORE ADJUSTER
        item {
            CardTunerContainer(title = "OOM OUT-OF-MEMORY WEIGHT TARGETS") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Force system processes into protection shields or instant-kill queue indexes under low RAM loads.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // Selection chips
                    val oomPids = listOf("system_server", "surfaceflinger", "com.android.systemui", "CUSTOM")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        oomPids.forEach { target ->
                            val isSelected = oomPidTarget == target
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
                                    .clickable {
                                        viewModel.setOomPidTarget(target)
                                        if (target != "CUSTOM") keyboardController?.hide()
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (target == "CUSTOM") "CUSTOM" else target.take(10) + if(target.length > 10) ".." else "",
                                    color = if (isSelected) Warning else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Custom input custom PID
                    if (oomPidTarget == "CUSTOM") {
                        OutlinedTextField(
                            value = customOomPid,
                            onValueChange = { viewModel.setCustomOomPid(it) },
                            placeholder = { Text("Enter target process PID or packageName", fontSize = 11.sp, color = TextSecondary) },
                            textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary, fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Warning,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = Warning,
                                focusedContainerColor = SurfaceVariant,
                                unfocusedContainerColor = SurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("custom_oom_pid_input")
                        )
                    }

                    // Score adjuster slider
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "OOM Score Adjustment Index", color = TextPrimary, fontSize = 10.sp)
                            Text(
                                text = "${oomScoreAdj}", 
                                color = Warning, 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.Bold, 
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "-1000 = Immune to kernel termination; 1000 = Evict process immediately.",
                            color = TextSecondary,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Slider(
                            value = oomScoreAdj.toFloat(),
                            onValueChange = { viewModel.setOomScoreAdj(it.toInt()) },
                            valueRange = -1000f..1000f,
                            colors = SliderDefaults.colors(
                                activeTrackColor = Warning,
                                inactiveTrackColor = BorderColor,
                                thumbColor = Warning
                            ),
                            modifier = Modifier.fillMaxWidth().height(24.dp).testTag("oom_adj_slider")
                        )
                    }

                    Button(
                        onClick = { 
                            keyboardController?.hide()
                            viewModel.applyOomScoreAdj() 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Warning),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("apply_oom_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Apply OOM Score", modifier = Modifier.size(16.dp))
                            Text("UPDATE SYSTEM OOM INDEX", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Console Terminal
        item {
            CardTunerContainer(title = "SERIAL CONSOLE VM MONITORS") {
                HardwareTerminalConsole(text = ramTerminalOutput)
            }
        }

        // Buffer space
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MemoryInfoRow(
    label: String,
    sizeMb: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Text(text = label, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = "$sizeMb MB",
            color = TextPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
