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
import com.example.monitor.CoreMetric
import com.example.monitor.SystemState
import com.example.ui.theme.*
import com.example.viewmodel.PulseViewModel

@Composable
fun CpuTunerScreen(
    viewModel: PulseViewModel,
    modifier: Modifier = Modifier
) {
    val systemState by viewModel.uiState.collectAsState()
    
    // Cluster Governments
    val cluster0Gov by viewModel.cluster0Governor.collectAsState()
    val cluster1Gov by viewModel.cluster1Governor.collectAsState()

    // Governor Tunables
    val governorTunables by viewModel.cpuGovernorTunables.collectAsState()

    // Cluster min/max
    val cluster0Min by viewModel.cluster0MinFreq.collectAsState()
    val cluster0Max by viewModel.cluster0MaxFreq.collectAsState()
    val cluster1Min by viewModel.cluster1MinFreq.collectAsState()
    val cluster1Max by viewModel.cluster1MaxFreq.collectAsState()

    // Cluster locked indicators
    val cluster0Locked by viewModel.cluster0FreqLocked.collectAsState()
    val cluster1Locked by viewModel.cluster1FreqLocked.collectAsState()

    // Affinity State
    val selectedPid by viewModel.selectedPid.collectAsState()
    val customPid by viewModel.customPid.collectAsState()
    val affinityCores by viewModel.affinityCores.collectAsState()
    val terminalOutput by viewModel.terminalOutput.collectAsState()

    // IRQ State
    val selectedIrq by viewModel.selectedIrq.collectAsState()
    val irqCores by viewModel.irqCores.collectAsState()
    val irqTerminalOutput by viewModel.irqTerminalOutput.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Space header
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
                        imageVector = Icons.Default.Build,
                        contentDescription = "CPU Icon",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "CPU SCHEDULER TUNER",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Kernel policy managers & core affinity controllers",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 1. ACTIVE CORE CLUSTER LAYOUT GRAPHIC (HOTPLUGGING & STATS)
        item {
            CpuClusterControlSection(
                cores = systemState?.cores ?: emptyList(),
                onCoreToggle = { viewModel.toggleCoreOnline(it) }
            )
        }

        // 1b. CPU FREQUENCY RESIDENCY & SLEEP MONITOR
        item {
            val residencyMap by viewModel.cpuResidency.collectAsState()
            var isExpanded by remember { mutableStateOf(true) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Residency",
                                tint = Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "CPU FREQUENCY RESIDENCY & SLEEP",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        if (residencyMap.isEmpty()) {
                            Text(
                                text = "No residency telemetry available.",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                residencyMap.forEach { (freq, pct) ->
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = freq, color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                            Text(text = String.format("%.1f%%", pct), color = Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        LinearProgressIndicator(
                                            progress = pct / 100f,
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                            color = if (freq == "Deep Sleep") Success else Primary,
                                            trackColor = BorderColor,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. PER-CLUSTER GOVERNOR & DYNAMIC CLOCKS CONFIG
        item {
            CardTunerContainer(title = "CLUSTER CONFIGURATOR") {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Cluster 0 (Efficiency cores)
                    ClusterTunerNode(
                        clusterId = 0,
                        title = "CLUSTER 0 (LITTLE Cores C0-C3)",
                        activeGovernor = cluster0Gov,
                        minFreq = cluster0Min,
                        maxFreq = cluster0Max,
                        isLocked = cluster0Locked,
                        freqBounds = 300f..1800f,
                        onGovernorChange = { 
                            viewModel.setClusterGovernor(0, it)
                            viewModel.loadGovernorTunables(it)
                        },
                        onMinFreqChange = { viewModel.setClusterMinFreq(0, it) },
                        onMaxFreqChange = { viewModel.setClusterMaxFreq(0, it) },
                        onLockToggle = { viewModel.toggleClusterFreqLock(0) },
                        tunables = governorTunables,
                        onExpand = { viewModel.loadGovernorTunables(cluster0Gov) },
                        onTunableChange = { name, value -> viewModel.setGovernorTunable(name, value) }
                    )

                    HorizontalDivider(color = BorderColor, thickness = 1.dp)

                    // Cluster 1 (Performance cores)
                    ClusterTunerNode(
                        clusterId = 1,
                        title = "CLUSTER 1 (BIG Cores C4-C7)",
                        activeGovernor = cluster1Gov,
                        minFreq = cluster1Min,
                        maxFreq = cluster1Max,
                        isLocked = cluster1Locked,
                        freqBounds = 800f..2800f,
                        onGovernorChange = { 
                            viewModel.setClusterGovernor(1, it)
                            viewModel.loadGovernorTunables(it)
                        },
                        onMinFreqChange = { viewModel.setClusterMinFreq(1, it) },
                        onMaxFreqChange = { viewModel.setClusterMaxFreq(1, it) },
                        onLockToggle = { viewModel.toggleClusterFreqLock(1) },
                        tunables = governorTunables,
                        onExpand = { viewModel.loadGovernorTunables(cluster1Gov) },
                        onTunableChange = { name, value -> viewModel.setGovernorTunable(name, value) }
                    )
                }
            }
        }

        // 3. PROCESS CPU AFFINITY MASKING (TASKSET)
        item {
            CardTunerContainer(title = "PROCESS CPU AFFINITY MASK (TASKSET)") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Restrict process scheduling target clusters to avoid CPU high-jitter cycles.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // Selection dropdown simulator (Chips)
                    val samplePids = listOf("system_server", "surfaceflinger", "com.android.systemui", "CUSTOM")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        samplePids.forEach { pid ->
                            val isSelected = selectedPid == pid
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
                                    .clickable {
                                        viewModel.setSelectedPid(pid)
                                        if (pid != "CUSTOM") keyboardController?.hide()
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (pid == "CUSTOM") "CUSTOM" else pid.take(10) + if(pid.length > 10) ".." else "",
                                    color = if (isSelected) Primary else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Custom PID input
                    if (selectedPid == "CUSTOM") {
                        OutlinedTextField(
                            value = customPid,
                            onValueChange = { viewModel.setCustomPid(it) },
                            placeholder = { Text("Enter numeric Process ID (e.g. 10091)", fontSize = 11.sp, color = TextSecondary) },
                            textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary, fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = Primary,
                                focusedContainerColor = SurfaceVariant,
                                unfocusedContainerColor = SurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("custom_pid_input")
                        )
                    }

                    // Affinity cores selection map
                    Text(
                        text = "CHOOSE INHERITED SYSTEM EXECUTION CORES",
                        color = TextPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (0..7).forEach { coreId ->
                            val isChosen = affinityCores.contains(coreId)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(
                                        if (isChosen) Secondary.copy(alpha = 0.2f) else SurfaceVariant,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isChosen) Secondary else BorderColor,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.toggleAffinityCore(coreId) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "C$coreId",
                                        color = if (isChosen) Secondary else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(if (isChosen) Secondary else Color.Transparent, CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.applyCpuAffinity()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("apply_affinity_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Affinity", modifier = Modifier.size(16.dp))
                            Text("LOCK SCHEDULER CPU BOUNDS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Terminal feedback
                    HardwareTerminalConsole(text = terminalOutput)
                }
            }
        }

        // 4. IRQ AFFINITY MAPPING INTERFACE
        item {
            CardTunerContainer(title = "IRQ SMP_AFFINITY ROUTER") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Migrate heavy hardware interrupts (IRQs) to dedicated performance clusters.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // IRQs List Selectors (Horizontal chips)
                    val irqDrivers = listOf("wlan0", "kgsl-3d0", "disp_sync", "audio_dsp")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        irqDrivers.forEach { irq ->
                            val isSelected = selectedIrq == irq
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
                                    .clickable { viewModel.setSelectedIrq(irq) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = irq,
                                    color = if (isSelected) Warning else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "CHOOSE ASSIGNED CORE GROUP MAPPING",
                        color = TextPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (0..7).forEach { coreId ->
                            val isChosen = irqCores.contains(coreId)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(
                                        if (isChosen) Warning.copy(alpha = 0.15f) else SurfaceVariant,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isChosen) Warning else BorderColor,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.toggleIrqCore(coreId) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "C$coreId",
                                        color = if (isChosen) Warning else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(if (isChosen) Warning else Color.Transparent, CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.applyIrqAffinity() },
                        colors = ButtonDefaults.buttonColors(containerColor = Warning),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("apply_irq_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Route", modifier = Modifier.size(16.dp))
                            Text("UPDATE SYSTEM IRQ MAPPINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HardwareTerminalConsole(text = irqTerminalOutput)
                }
            }
        }

        // Buffer bottom space
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CpuClusterControlSection(
    cores: List<CoreMetric>,
    onCoreToggle: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HARDWARE SYMMETRIC CORE MODULES",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "HOTPLUG ARMABLE",
                    color = Success,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Grid of CPU core units with switch nodes
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Top cluster (C0-C3), Lower cluster (C4-C7)
                for (clusterIndex in 0..1) {
                    val clusterLabel = if (clusterIndex == 0) "LITTLE CLUSTER (EFFICIENT)" else "BIG CLUSTER (PERFORMANCE)"
                    val clusterColor = if (clusterIndex == 0) Primary else Secondary
                    val startId = clusterIndex * 4
                    val endId = startId + 3

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = clusterLabel,
                            color = TextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (coreId in startId..endId) {
                                val core = cores.find { it.id == coreId }
                                val isOnline = core?.isOnline ?: true
                                val load = core?.loadPercent ?: 0
                                val freq = core?.freqMhz ?: 0

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isOnline) clusterColor.copy(alpha = 0.05f) else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isOnline) clusterColor.copy(alpha = 0.25f) else BorderColor,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onCoreToggle(coreId) }
                                        .padding(8.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "C$coreId",
                                                color = if (isOnline) TextPrimary else TextSecondary.copy(alpha = 0.5f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            // Tiny neon switch circle indicator
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(
                                                        if (isOnline) Success else Error,
                                                        CircleShape
                                                    )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        if (isOnline) {
                                            Text(
                                                text = "$load%",
                                                color = clusterColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "${freq}M",
                                                color = TextSecondary,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        } else {
                                            Text(
                                                text = "SLEEP",
                                                color = Error.copy(alpha = 0.6f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.SansSerif
                                            )
                                            Text(
                                                text = "OFFLINE",
                                                color = TextSecondary.copy(alpha = 0.4f),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Normal
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
    }
}

@Composable
fun ClusterTunerNode(
    clusterId: Int,
    title: String,
    activeGovernor: String,
    minFreq: Float,
    maxFreq: Float,
    isLocked: Boolean,
    freqBounds: ClosedRange<Float>,
    onGovernorChange: (String) -> Unit,
    onMinFreqChange: (Float) -> Unit,
    onMaxFreqChange: (Float) -> Unit,
    onLockToggle: () -> Unit,
    tunables: Map<String, String>,
    onExpand: () -> Unit,
    onTunableChange: (String, String) -> Unit
) {
    val clusterColor = if (clusterId == 0) Primary else Secondary

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = clusterColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            
            // Freq parameters lock
            IconButton(
                onClick = onLockToggle,
                modifier = Modifier
                    .background(if (isLocked) clusterColor.copy(alpha = 0.15f) else SurfaceVariant, CircleShape)
                    .border(1.dp, if (isLocked) clusterColor else BorderColor, CircleShape)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock frequency limits",
                    tint = if (isLocked) clusterColor else TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Governor select row
        Text(
            text = "SCALING CPU GOVERNOR POLICY",
            color = TextSecondary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )

        val governors = listOf("schedutil", "performance", "powersave", "ondemand")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            governors.forEach { gov ->
                val isSelected = activeGovernor == gov
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) clusterColor.copy(alpha = 0.15f) else SurfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) clusterColor else BorderColor,
                            RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onGovernorChange(gov) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = gov,
                        color = if (isSelected) clusterColor else TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Sliders sliders (Locks interactively disables sliders)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val stepsCount = 10
            
            // Min slide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Minimum frequency core limit",
                    color = TextSecondary,
                    fontSize = 9.sp
                )
                Text(
                    text = "${minFreq.toInt()} MHz",
                    color = clusterColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Slider(
                value = minFreq,
                onValueChange = { onMinFreqChange(it.coerceAtMost(maxFreq)) },
                valueRange = freqBounds.start..freqBounds.endInclusive,
                enabled = !isLocked,
                colors = SliderDefaults.colors(
                    activeTrackColor = clusterColor,
                    inactiveTrackColor = BorderColor,
                    thumbColor = clusterColor,
                    disabledActiveTrackColor = clusterColor.copy(alpha = 0.3f),
                    disabledThumbColor = clusterColor.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth().height(26.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Max slide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Maximum frequency core limit",
                    color = TextSecondary,
                    fontSize = 9.sp
                )
                Text(
                    text = "${maxFreq.toInt()} MHz",
                    color = clusterColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Slider(
                value = maxFreq,
                onValueChange = { onMaxFreqChange(it.coerceAtLeast(minFreq)) },
                valueRange = freqBounds.start..freqBounds.endInclusive,
                enabled = !isLocked,
                colors = SliderDefaults.colors(
                    activeTrackColor = clusterColor,
                    inactiveTrackColor = BorderColor,
                    thumbColor = clusterColor,
                    disabledActiveTrackColor = clusterColor.copy(alpha = 0.3f),
                    disabledThumbColor = clusterColor.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth().height(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Collapsible Tunables Parameters Editor Card
        var showTunables by remember { mutableStateOf(false) }
        
        LaunchedEffect(showTunables) {
            if (showTunables) {
                onExpand()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showTunables = !showTunables }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Tunables",
                        tint = clusterColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "CPU GOVERNOR TUNABLES (${activeGovernor.uppercase()})",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Icon(
                    imageVector = if (showTunables) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = showTunables,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val activeTunables = tunables.filter { entry ->
                        // Only show tunables relevant to current governor to avoid visual clutter
                        when (activeGovernor) {
                            "schedutil" -> entry.key in listOf("rate_limit_us", "hispeed_load")
                            "interactive" -> entry.key in listOf("timer_rate", "hispeed_freq", "go_hispeed_load")
                            "ondemand" -> entry.key in listOf("up_threshold", "sampling_rate")
                            else -> entry.key in listOf("sampling_rate")
                        }
                    }

                    if (activeTunables.isEmpty()) {
                        Text(
                            text = "No configurable tunables detected for $activeGovernor governor.",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    } else {
                        activeTunables.forEach { (name, value) ->
                            var tempValue by remember(value) { mutableStateOf(value) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        color = TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "sysfs tuning path parameter node",
                                        color = TextSecondary,
                                        fontSize = 8.sp
                                    )
                                }
                                OutlinedTextField(
                                    value = tempValue,
                                    onValueChange = { tempValue = it },
                                    textStyle = TextStyle(fontSize = 11.sp, color = TextPrimary, fontFamily = FontFamily.Monospace),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = clusterColor,
                                        unfocusedBorderColor = BorderColor,
                                        cursorColor = clusterColor,
                                        focusedContainerColor = Surface,
                                        unfocusedContainerColor = Surface
                                    ),
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(44.dp)
                                        .testTag("tunable_${name}_input")
                                )
                                AnimatedVisibility(
                                    visible = tempValue != value,
                                    enter = fadeIn() + scaleIn(),
                                    exit = fadeOut() + scaleOut()
                                ) {
                                    IconButton(
                                        onClick = { onTunableChange(name, tempValue) },
                                        modifier = Modifier
                                            .background(Success.copy(alpha = 0.15f), CircleShape)
                                            .border(1.dp, Success, CircleShape)
                                            .size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Save tunable",
                                            tint = Success,
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
}

@Composable
fun CardTunerContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            content()
        }
    }
}

@Composable
fun HardwareTerminalConsole(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.Black, RoundedCornerShape(10.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    text = text,
                    color = Success.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
