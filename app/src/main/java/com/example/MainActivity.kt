package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BacklogScreen
import com.example.ui.screens.HomeworkScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HighPriorityOrange
import com.example.ui.theme.PrepPulseTheme
import com.example.ui.viewmodel.PrepViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Today : Screen("today", "Today", Icons.Default.Home)
    object Timetable : Screen("timetable", "Timetable", Icons.Default.CalendarToday)
    object Backlog : Screen("backlog", "Backlog", Icons.Default.Warning)
    object Homework : Screen("homework", "Homework", Icons.Default.Assignment)
    object Timer : Screen("timer", "Timer", Icons.Default.Timer)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
}

class MainActivity : ComponentActivity() {

    private val viewModel: PrepViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PrepPulseTheme {
                MainAppStructure(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppStructure(viewModel: PrepViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Today.route

    val backlogTasks by viewModel.backlogTasks.collectAsStateWithLifecycle()

    val screens = listOf(
        Screen.Today,
        Screen.Timetable,
        Screen.Backlog,
        Screen.Homework,
        Screen.Timer,
        Screen.Analytics
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ElectricCyan,
                tonalElevation = 8.dp
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            if (screen == Screen.Backlog && backlogTasks.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = HighPriorityOrange,
                                            contentColor = MaterialTheme.colorScheme.background
                                        ) {
                                            Text(
                                                text = backlogTasks.size.toString(),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    tint = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = ElectricCyan.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Today.route) {
                TodayScreen(
                    viewModel = viewModel,
                    onNavigateToBacklog = { navController.navigate(Screen.Backlog.route) }
                )
            }
            composable(Screen.Timetable.route) {
                TimetableScreen(viewModel = viewModel)
            }
            composable(Screen.Backlog.route) {
                BacklogScreen(
                    viewModel = viewModel,
                    onStartTimerForTask = { navController.navigate(Screen.Timer.route) }
                )
            }
            composable(Screen.Homework.route) {
                HomeworkScreen(
                    viewModel = viewModel,
                    onStartTimerForTask = { navController.navigate(Screen.Timer.route) }
                )
            }
            composable(Screen.Timer.route) {
                TimerScreen(viewModel = viewModel)
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel = viewModel)
            }
        }
    }
}
