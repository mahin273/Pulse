package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monitor.CoreMetric
import com.example.monitor.SystemState
import com.example.root.RootMethod
import com.example.viewmodel.NotificationEvent
import com.example.viewmodel.PulseViewModel
import com.example.ui.components.LiveGauge
import com.example.ui.components.RollingChart
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: PulseViewModel,
    modifier: Modifier = Modifier
) {
    val systemState by viewModel.uiState.collectAsState()
    val chartHistory by viewModel.chartHistory.collectAsState()
    
    val isRootGranted by viewModel.isRootGranted.collectAsState()
    val rootMethod by viewModel.rootMethod.collectAsState()
    val dryRunMode by viewModel.dryRunMode.collectAsState()

    val isBoostActive by viewModel.isBoostActive.collectAsState()
    val isGameModeActive by viewModel.isGameModeActive.collectAsState()
    val isSaverActive by viewModel.isSaverActive.collectAsState()
    
    val revertTimerLeft by viewModel.revertTimerLeft.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. HEADER LOGO & SYSTEM IDENTIFICATION AREA
        item {
            Spacer(modifier = Modifier.height(12.dp))
            HeaderSection(
                isRootGranted = isRootGranted,
                rootMethod = rootMethod,
                dryRunMode = dryRunMode,
                tempC = systemState?.systemTempC,
                onRootCheck = { viewModel.refreshRootState() },
                onDryRunToggle = { viewModel.toggleDryRunMode() }
            )
        }

        // 2. SAFETY REVERT PRE-FLIGHT BOX (Floating Alert styled card)
        if (revertTimerLeft > 0) {
            item {
                SafetyRevertCard(
                    timerSeconds = revertTimerLeft,
                    onRollback = { viewModel.rollbackActiveMode() },
                    onLockIn = { viewModel.dismissRevertTimer() }
                )
            }
        }

        // 3. QUICK ACTION DASH BOARD SWITCHES
        item {
            QuickActionsGrid(
                isBoostActive = isBoostActive,
                isGameModeActive = isGameModeActive,
                isSaverActive = isSaverActive,
                onBoost = { viewModel.triggerBoost() },
                onGameMode = { viewModel.triggerGameMode() },
                onSaver = { viewModel.triggerBatterySaver() }
            )
        }

        // 4. REAL-TIME MULTI-RADIAL METRICS
        item {
            systemState?.let { state ->
                MetricsGaugeGrid(state = state)
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Surface, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        }

        // 5. PER-CLUSTER CPU CORES GRAPHICAL LOAD LIST
        item {
            systemState?.let { state ->
                CoresSymmetricGrid(cores = state.cores)
            }
        }

        // Geometric Balance Process Snapshot
        item {
            ProcessSnapshotCard()
        }

        // 6. HISTORICAL SCROLLING GRAPHS
        item {
            Text(
                text = "ROLLING HISTORIC MONITORS (60S)",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RollingChart(
                    title = "GLOBAL CPU STABILITY",
                    history = chartHistory.cpuHistory,
                    color = Primary,
                    yMax = 100f
                )
                RollingChart(
                    title = "VIRTUAL MEMORY (RAM) PRESSURE",
                    history = chartHistory.ramHistory,
                    color = Secondary,
                    yMax = 100f
                )
                RollingChart(
                    title = "INTEGRATED GPU SUB-PROCESSOR",
                    history = chartHistory.gpuHistory,
                    color = Success,
                    yMax = 100f
                )
                RollingChart(
                    title = "BATTERY DRAINAGE CURVE",
                    history = chartHistory.batteryHistory,
                    color = Warning,
                    yMax = 100f,
                    unit = "%"
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GeometricBattery(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier.size(width = 20.dp, height = 12.dp)) {
        val strokeWidth = 1.5.dp.toPx()
        val width = size.width
        val height = size.height
        
        // Draw battery outer border
        val mainWidth = width - 3.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, 0f),
            size = Size(mainWidth, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        
        // Draw battery cap on the right
        val capWidth = 2.dp.toPx()
        val capHeight = height * 0.4f
        val capY = (height - capHeight) / 2f
        drawRoundRect(
            color = color,
            topLeft = Offset(mainWidth + 1.dp.toPx(), capY),
            size = Size(capWidth, capHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )
        
        // Draw inside progress bars or just a block
        val pad = strokeWidth + 2.dp.toPx()
        val fillWidth = (mainWidth - pad * 2f) * 0.70f // Static beautiful fill
        if (fillWidth > 0) {
            drawRect(
                color = color,
                topLeft = Offset(pad, pad),
                size = Size(fillWidth, height - pad * 2f)
            )
        }
    }
}

@Composable
fun GeometricAppLogo(modifier: Modifier = Modifier, color: Color = Primary) {
    Box(
        modifier = modifier
            .size(38.dp)
            .background(SurfaceVariant, RoundedCornerShape(10.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val strokeWidth = 2.dp.toPx()
            val sizePx = size.width
            
            // Draw a squared chip with a notch at the top
            drawRoundRect(
                color = color,
                topLeft = Offset(0f, 2.dp.toPx()),
                size = Size(sizePx, sizePx - 2.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )
            
            // Central core node
            drawCircle(
                color = color,
                center = Offset(sizePx / 2f, sizePx / 2f + 1.dp.toPx()),
                radius = 3.dp.toPx()
            )
            
            // Top contact terminal
            drawRect(
                color = color,
                topLeft = Offset((sizePx - 8.dp.toPx()) / 2f, 0f),
                size = Size(8.dp.toPx(), 3.dp.toPx())
            )
        }
    }
}

@Composable
fun HeaderSection(
    isRootGranted: Boolean,
    rootMethod: RootMethod,
    dryRunMode: Boolean,
    tempC: Float?,
    onRootCheck: () -> Unit,
    onDryRunToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GeometricAppLogo(color = Primary)
            Column {
                Text(
                    text = "PULSE",
                    color = Primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pulse Green indicator node
                    val indicatorColor = if (isRootGranted) Success else if (dryRunMode) Warning else Error
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(indicatorColor, CircleShape)
                            .border(1.dp, indicatorColor.copy(alpha = 0.5f), CircleShape)
                    )
                    Text(
                        text = "${if (isRootGranted) rootMethod.name else "NO ROOT PRIVILEGES"} • ${if (dryRunMode) "SANDBOX RUN" else "PRODUCTION BOUND"}",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Thermal Info & Geometric Battery Container
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "THERMAL",
                    color = TextSecondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = String.format("%.0f°C", tempC ?: 32f),
                    color = if ((tempC ?: 32f) >= 45f) Warning else Success,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            // Geometric Battery shape card
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(SurfaceVariant, RoundedCornerShape(10.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                    .clickable { onDryRunToggle() },
                contentAlignment = Alignment.Center
            ) {
                GeometricBattery(color = if (dryRunMode) Warning else Primary)
            }

            // Refresh button
            IconButton(
                onClick = onRootCheck,
                modifier = Modifier
                    .background(SurfaceVariant, CircleShape)
                    .border(1.dp, BorderColor, CircleShape)
                    .size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Root Check",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SafetyRevertCard(
    timerSeconds: Int,
    onRollback: () -> Unit,
    onLockIn: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Error.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    tint = Error,
                    contentDescription = "Alert",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SAFETY AUTOREVERT TIMEOUT: ${timerSeconds}S",
                        color = Error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Tweak applied! Confirm if the system remains stable. Reverting shortly.",
                        color = TextPrimary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onRollback,
                    colors = ButtonDefaults.textButtonColors(contentColor = TextPrimary),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("REVERT NOW", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onLockIn,
                    colors = ButtonDefaults.buttonColors(containerColor = Error, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("KEEP TWEAK", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    isBoostActive: Boolean,
    isGameModeActive: Boolean,
    isSaverActive: Boolean,
    onBoost: () -> Unit,
    onGameMode: () -> Unit,
    onSaver: () -> Unit
) {
    Column {
        Text(
            text = "DASHBOARD PRESETS STATUS",
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickButton(
                title = "MEM BOOST",
                subtitle = "Flush Cache",
                isActive = isBoostActive,
                activeColor = Primary,
                iconType = Icons.Default.Refresh,
                onClick = onBoost,
                modifier = Modifier.weight(1f)
            )
            QuickButton(
                title = "GAME MODE",
                subtitle = "Max Cluster",
                isActive = isGameModeActive,
                activeColor = Secondary,
                iconType = Icons.Default.Build,
                onClick = onGameMode,
                modifier = Modifier.weight(1f)
            )
            QuickButton(
                title = "ECO PRO",
                subtitle = "Lower Drain",
                isActive = isSaverActive,
                activeColor = Success,
                iconType = Icons.Default.Notifications,
                onClick = onSaver,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickButton(
    title: String,
    subtitle: String,
    isActive: Boolean,
    activeColor: Color,
    iconType: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(74.dp)
            .background(
                if (isActive) activeColor else Surface,
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (isActive) activeColor else BorderColor,
                RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isActive) Color.Black.copy(alpha = 0.15f) else SurfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconType,
                    contentDescription = title,
                    tint = if (isActive) Color.Black else activeColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    color = if (isActive) Color.Black else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = subtitle,
                    color = if (isActive) Color.Black.copy(alpha = 0.7f) else TextSecondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MetricsGaugeGrid(state: SystemState) {
    Column {
        Text(
            text = "REALTIME HARDWARE SENSOR TELESCOPE (100ms)",
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LiveGauge(
                label = "CPU Load",
                value = state.overallCpuLoad,
                color = Primary,
                modifier = Modifier.weight(1f).height(180.dp)
            )
            val ramPct = (state.usedRamMb / state.totalRamMb) * 100f
            val ramLabel = String.format("%.1f/%.1f GB", state.usedRamMb / 1024f, state.totalRamMb / 1024f)
            LiveGauge(
                label = "RAM Use",
                value = ramPct,
                color = Secondary,
                secondaryLabel = ramLabel,
                modifier = Modifier.weight(1f).height(180.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LiveGauge(
                label = "GPU Logic",
                value = state.gpuLoad,
                color = Success,
                modifier = Modifier.weight(1f).height(180.dp)
            )
            val batLabel = String.format("%.0fmA", state.batteryCurrentMa)
            LiveGauge(
                label = "Battery",
                value = state.batteryPct.toFloat(),
                color = Warning,
                secondaryLabel = batLabel,
                unit = "%",
                modifier = Modifier.weight(1f).height(180.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val tempFloat = state.systemTempC
            val tempColor = when {
                tempFloat >= 75f -> Error
                tempFloat >= 45f -> Warning
                tempFloat >= 35f -> Success
                else -> Primary
            }
            LiveGauge(
                label = "Thermal SoC",
                value = (tempFloat / 100f) * 100f,
                color = tempColor,
                secondaryLabel = String.format("%.1f°C", tempFloat),
                unit = "°",
                modifier = Modifier.weight(1f).height(180.dp)
            )
            // Energy specs
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .background(Surface, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "ENERGY SPECS",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatRow(name = "Voltage", valStr = String.format("%.2f V", state.batteryVoltageV))
                        StatRow(name = "Current draw", valStr = String.format("%.1f mA", state.batteryCurrentMa))
                        StatRow(name = "Discharge Co.", valStr = String.format("%.0f mAh/h", state.sessionDrainRateMaH))
                        StatRow(name = "Battery heat", valStr = String.format("%.1f °C", state.batteryTemp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(name: String, valStr: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = TextSecondary, fontSize = 9.sp)
        Text(valStr, color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CoresSymmetricGrid(cores: List<CoreMetric>) {
    Column {
        Text(
            text = "PER-CORE FREQUENCY & UTILIZATION GRID",
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(12.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Symmetrically layout CPU Cores (typically 8 cores, let's group by rows of 4)
                val chunkedCores = cores.chunked(4)
                for (row in chunkedCores) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (core in row) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SingleCoreView(core = core)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SingleCoreView(core: CoreMetric) {
    val coreColor = when {
        !core.isOnline -> BorderColor
        core.loadPercent >= 80 -> Error
        core.loadPercent >= 50 -> Warning
        core.loadPercent >= 20 -> Success
        else -> Primary
    }

    // Scale breathing feedback representing CPU dynamics
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750 + (core.id * 80), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val scaleValue = if (core.isOnline && core.loadPercent > 10) {
        1f + (core.loadPercent / 100f) * 0.12f * pulseScale
    } else 1.0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(4.dp)
    ) {
        // Geometric outer core ring
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(coreColor.copy(alpha = 0.08f), CircleShape)
                .border(2.dp, coreColor.copy(alpha = if (core.isOnline) 0.80f else 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((22 * scaleValue).dp)
                    .background(coreColor.copy(alpha = if (core.isOnline) 0.90f else 0.20f), CircleShape)
            ) {
                if (core.isOnline) {
                    Text(
                        text = "${core.loadPercent}",
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Text(
                        text = "—",
                        color = TextSecondary.copy(alpha = 0.4f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "C${core.id}",
            color = TextPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (core.isOnline) "${core.freqMhz}M" else "OFFLINE",
            color = if (core.isOnline) Success else Error.copy(alpha = 0.6f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
        
        // Micro indicator bar at bottom
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(2.dp)
                .background(coreColor.copy(alpha = if (core.isOnline) 0.85f else 0.2f), RoundedCornerShape(1.dp))
        )
    }
}

@Composable
fun ProcessSnapshotCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Warning.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, Warning.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        color = Warning,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
                Column {
                    Text(
                        text = "System Optimizer Daemon",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "UID: 1000 • Priority: -20 (RT CLS)",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "14 MB",
                    color = Primary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "TOP TASK 1",
                    color = Success,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
