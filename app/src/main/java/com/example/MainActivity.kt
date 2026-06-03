package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DashboardScreen
import com.example.ui.CpuTunerScreen
import com.example.ui.RamTunerScreen
import com.example.ui.AppsTunerScreen
import com.example.ui.MoreTunerScreen
import com.example.ui.theme.*
import com.example.viewmodel.PulseViewModel
import com.example.viewmodel.NotificationEvent
import kotlinx.coroutines.flow.collectLatest

enum class NavigationTab {
    DASHBOARD, CPU, RAM, APPS, MORE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge drawing support
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: PulseViewModel = viewModel()
                var selectedTab by remember { mutableStateFlowOf(NavigationTab.DASHBOARD) }
                val snackbarHostState = remember { SnackbarHostState() }

                // Collect notifications from ViewModel at the root level
                LaunchedEffect(key1 = true) {
                    viewModel.notification.collectLatest { event ->
                        val prefix = when(event) {
                            is NotificationEvent.Success -> "🟢 [SUCCESS] "
                            is NotificationEvent.Warning -> "🟡 [WARNING] "
                            is NotificationEvent.Error -> "🔴 [ERROR] "
                        }
                        snackbarHostState.showSnackbar(
                            message = prefix + event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                Scaffold(
                    bottomBar = {
                        PulseBottomNavigation(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            val actionLabel = data.visuals.actionLabel
                            Snackbar(
                                containerColor = SurfaceVariant,
                                contentColor = TextPrimary,
                                actionContentColor = Primary,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
                                action = {
                                    if (actionLabel != null) {
                                        TextButton(onClick = { data.performAction() }) {
                                            Text(actionLabel, color = Primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Text(data.visuals.message, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    },
                    containerColor = Color.Black,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .padding(
                                top = innerPadding.calculateTopPadding(),
                                bottom = innerPadding.calculateBottomPadding()
                            ) // Safe area control for both top status bar and bottom nav
                    ) {
                        when (selectedTab) {
                            NavigationTab.DASHBOARD -> {
                                DashboardScreen(viewModel = viewModel)
                            }
                            NavigationTab.CPU -> {
                                CpuTunerScreen(viewModel = viewModel)
                            }
                            NavigationTab.RAM -> {
                                RamTunerScreen(viewModel = viewModel)
                            }
                            NavigationTab.APPS -> {
                                AppsTunerScreen(viewModel = viewModel)
                            }
                            NavigationTab.MORE -> {
                                MoreTunerScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PulseBottomNavigation(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    NavigationBar(
        containerColor = Surface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .background(Color.Black)
            .border(1.dp, BorderColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .windowInsetsPadding(WindowInsets.navigationBars) // Ensure gesture pill safety
    ) {
        NavigationBarItem(
            selected = selectedTab == NavigationTab.DASHBOARD,
            onClick = { onTabSelected(NavigationTab.DASHBOARD) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                indicatorColor = SurfaceVariant,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.CPU,
            onClick = { onTabSelected(NavigationTab.CPU) },
            icon = { Icon(Icons.Default.DeveloperBoard, contentDescription = "CPU Tuner") },
            label = { Text("CPU", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                indicatorColor = SurfaceVariant,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.RAM,
            onClick = { onTabSelected(NavigationTab.RAM) },
            icon = { Icon(Icons.Default.Memory, contentDescription = "Memory Tuner") },
            label = { Text("RAM", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                indicatorColor = SurfaceVariant,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.APPS,
            onClick = { onTabSelected(NavigationTab.APPS) },
            icon = { Icon(Icons.Default.Apps, contentDescription = "Apps Manager") },
            label = { Text("Apps", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                indicatorColor = SurfaceVariant,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.MORE,
            onClick = { onTabSelected(NavigationTab.MORE) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "More Options") },
            label = { Text("More", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                indicatorColor = SurfaceVariant,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
    }
}

@Composable
fun BlueprintPlaceholderScreen(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    details: List<String>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(16.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = Primary, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title.uppercase(),
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "SYSTEM SERVICE NODE ARMED",
                color = Success,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                details.forEach { detail ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡",
                            color = Primary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = detail,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pulse system modules are fully wired at root level. These controls will bind directly to system sysfs channels in Phase 2, 3, and 4 configurations.",
                color = TextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

// Helper composition state builder
private fun <T> mutableStateFlowOf(value: T): MutableState<T> = mutableStateOf(value)
