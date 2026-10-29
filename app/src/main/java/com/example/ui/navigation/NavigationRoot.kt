package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.core.di.AppEnvironment
import com.example.core.di.FuelUnitSystem
import kotlinx.coroutines.launch
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AnalyticsViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DashboardViewModel
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.LogEntryScreen
import com.example.ui.screens.LogEntryUiAction
import com.example.ui.screens.LogEntryUiEvent
import com.example.ui.screens.LogEntryViewModel
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SettingsViewModel

@Composable
fun NavigationRoot(
    navController: NavHostController,
    startDestination: String,
    onCompleteOnboarding: () -> Unit,
    currentUnitSystem: FuelUnitSystem,
    onUnitSystemChanged: (FuelUnitSystem) -> Unit,
    isNightPumpMode: Boolean,
    onNightPumpModeToggled: (Boolean) -> Unit,
    appEnvironment: AppEnvironment,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(220)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(240)
                )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(180)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(240)
                )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(240)
                )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(180)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(240)
                )
        }
    ) {
        composable(ROUTE_ONBOARDING) {
            OnboardingScreen(
                onCompleteOnboarding = onCompleteOnboarding
            )
        }

        composable(Screen.Dashboard.route) {
            val dashboardViewModel = remember { DashboardViewModel(appEnvironment) }
            val uiState by dashboardViewModel.uiState.collectAsState()
            val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

            DashboardScreen(
                uiState = uiState,
                onAction = dashboardViewModel::handleIntent,
                onNavigateToLogEntry = { prefillPartial ->
                    navController.navigate("$ROUTE_LOG_ENTRY?prefillPartial=$prefillPartial")
                },
                onSaveFastLog = { entry ->
                    coroutineScope.launch {
                        appEnvironment.repository.insertFuelEntry(entry)
                    }
                },
                onSaveTags = { entry ->
                    coroutineScope.launch {
                        appEnvironment.repository.updateFuelEntry(entry)
                    }
                }
            )
        }

        composable(Screen.Logs.route) {
            LogsScreen(appEnvironment = appEnvironment)
        }

        composable(
            route = "$ROUTE_LOG_ENTRY?prefillPartial={prefillPartial}",
            arguments = listOf(
                navArgument("prefillPartial") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val prefillPartial = backStackEntry.arguments?.getBoolean("prefillPartial") ?: false
            val logEntryViewModel = remember { LogEntryViewModel(appEnvironment) }
            val uiState by logEntryViewModel.uiState.collectAsState()

            LaunchedEffect(prefillPartial) {
                logEntryViewModel.handleIntent(LogEntryUiAction.Initialize(prefillPartial))
            }

            LaunchedEffect(logEntryViewModel) {
                logEntryViewModel.uiEvent.collect { event ->
                    when (event) {
                        is LogEntryUiEvent.NavigateBackOnSuccess -> {
                            navController.popBackStack()
                        }
                        is LogEntryUiEvent.ShowSnackbar -> {}
                    }
                }
            }

            LogEntryScreen(
                uiState = uiState,
                onAction = logEntryViewModel::handleIntent,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Garage.route) {
            GarageScreen()
        }

        composable(Screen.Analytics.route) {
            val analyticsViewModel = remember { AnalyticsViewModel(appEnvironment) }
            val uiState by analyticsViewModel.uiState.collectAsState()
            AnalyticsScreen(
                uiState = uiState,
                onAction = analyticsViewModel::handleAction
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel = remember { SettingsViewModel(appEnvironment) }
            val uiState by settingsViewModel.uiState.collectAsState()
            SettingsScreen(
                uiState = uiState,
                onAction = settingsViewModel::handleAction
            )
        }
    }
}
