package com.example.ui.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.di.AppEnvironment
import com.example.core.di.FuelUnitSystem
import com.example.ui.navigation.AdaptiveNavigationType
import com.example.ui.navigation.NavigationRoot
import com.example.ui.navigation.ROUTE_LOG_ENTRY
import com.example.ui.navigation.ROUTE_ONBOARDING
import com.example.ui.navigation.Screen
import com.example.ui.navigation.calculateAdaptiveNavigationType
import com.example.ui.theme.LocalExpressiveShapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScaffold(
    appEnvironment: AppEnvironment,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val coroutineScope = rememberCoroutineScope()
    val shapes = LocalExpressiveShapes.current

    val isOnboardingCompleted by appEnvironment.preferences.isOnboardingCompleted
        .collectAsState(initial = false)
    val preferredUnitSystem by appEnvironment.preferences.preferredUnitSystem
        .collectAsState(initial = FuelUnitSystem.METRIC_L_PER_100KM)
    val isOledNightMode by appEnvironment.preferences.isOledNightModeEnabled
        .collectAsState(initial = false)

    val startDestination = if (isOnboardingCompleted) {
        Screen.Dashboard.route
    } else {
        ROUTE_ONBOARDING
    }

    val isTopLevelDestination = Screen.bottomNavItems.any { it.route == currentRoute }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val adaptiveNavType = calculateAdaptiveNavigationType(maxWidth)

        when (adaptiveNavType) {
            AdaptiveNavigationType.BOTTOM_NAVIGATION -> {
                // Compact Screen Layout (Phones)
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (isTopLevelDestination) {
                            FuelSyncTopAppBar(
                                currentRoute = currentRoute,
                                onQuickAddClick = {
                                    navController.navigate("$ROUTE_LOG_ENTRY?prefillPartial=false")
                                }
                            )
                        }
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = isTopLevelDestination,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .testTag("compact_bottom_navigation_bar"),
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 3.dp
                            ) {
                                Screen.bottomNavItems.forEach { screen ->
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
                                            Icon(
                                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = screen.contentDescription,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.title,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("nav_item_${screen.route}")
                                    )
                                }
                            }
                        }
                    },
                    floatingActionButton = {
                        if (isTopLevelDestination) {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    navController.navigate("$ROUTE_LOG_ENTRY?prefillPartial=false")
                                },
                                shape = shapes.pill,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .testTag("quick_log_fab")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Log Fuel",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Log Fuel",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavigationRoot(
                        navController = navController,
                        startDestination = startDestination,
                        onCompleteOnboarding = {
                            coroutineScope.launch {
                                appEnvironment.preferences.setOnboardingCompleted(true)
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                                }
                            }
                        },
                        currentUnitSystem = preferredUnitSystem,
                        onUnitSystemChanged = { unit ->
                            coroutineScope.launch {
                                appEnvironment.preferences.setPreferredUnitSystem(unit)
                            }
                        },
                        isNightPumpMode = isOledNightMode,
                        onNightPumpModeToggled = { enabled ->
                            coroutineScope.launch {
                                appEnvironment.preferences.setOledNightMode(enabled)
                            }
                        },
                        appEnvironment = appEnvironment,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            AdaptiveNavigationType.NAVIGATION_RAIL -> {
                // Medium Screen Layout (Foldables / Tablets Portrait)
                Row(modifier = Modifier.fillMaxSize()) {
                    if (isTopLevelDestination) {
                        NavigationRail(
                            modifier = Modifier
                                .fillMaxHeight()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .testTag("medium_navigation_rail"),
                            containerColor = MaterialTheme.colorScheme.surface,
                            header = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalGasStation,
                                            contentDescription = "FuelSync",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    FloatingActionButton(
                                        onClick = { navController.navigate("$ROUTE_LOG_ENTRY?prefillPartial=false") },
                                        shape = shapes.pill,
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Log Fuel")
                                    }
                                }
                            }
                        ) {
                            Screen.bottomNavItems.forEach { screen ->
                                val isSelected = currentRoute == screen.route
                                NavigationRailItem(
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
                                        Icon(
                                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.contentDescription
                                        )
                                    },
                                    label = { Text(screen.title) },
                                    colors = NavigationRailItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("rail_item_${screen.route}")
                                )
                            }
                        }
                    }

                    Scaffold(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        topBar = {
                            if (isTopLevelDestination) {
                                FuelSyncTopAppBar(
                                    currentRoute = currentRoute,
                                    onQuickAddClick = { navController.navigate("$ROUTE_LOG_ENTRY?prefillPartial=false") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavigationRoot(
                            navController = navController,
                            startDestination = startDestination,
                            onCompleteOnboarding = {
                                coroutineScope.launch {
                                    appEnvironment.preferences.setOnboardingCompleted(true)
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                                    }
                                }
                            },
                            currentUnitSystem = preferredUnitSystem,
                            onUnitSystemChanged = { unit ->
                                coroutineScope.launch {
                                    appEnvironment.preferences.setPreferredUnitSystem(unit)
                                }
                            },
                            isNightPumpMode = isOledNightMode,
                            onNightPumpModeToggled = { enabled ->
                                coroutineScope.launch {
                                    appEnvironment.preferences.setOledNightMode(enabled)
                                }
                            },
                            appEnvironment = appEnvironment,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }

            AdaptiveNavigationType.PERMANENT_DRAWER -> {
                // Expanded Screen Layout (Tablets Landscape / Desktop)
                PermanentNavigationDrawer(
                    drawerContent = {
                        if (isTopLevelDestination) {
                            PermanentDrawerSheet(
                                modifier = Modifier
                                    .width(280.dp)
                                    .fillMaxHeight()
                                    .testTag("expanded_navigation_drawer"),
                                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                                drawerContainerColor = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(16.dp)
                                ) {
                                    // Brand Header
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.LocalGasStation,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "FuelSync",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Fleet & Telemetry",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Action Button
                                    ExtendedFloatingActionButton(
                                        onClick = { navController.navigate("$ROUTE_LOG_ENTRY?prefillPartial=false") },
                                        shape = shapes.pill,
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Log New Fill-Up")
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Drawer Destinations
                                    Screen.bottomNavItems.forEach { screen ->
                                        val isSelected = currentRoute == screen.route
                                        NavigationDrawerItem(
                                            label = { Text(screen.title, fontWeight = FontWeight.SemiBold) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                    contentDescription = null
                                                )
                                            },
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
                                            shape = shapes.pill,
                                            colors = NavigationDrawerItemDefaults.colors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            if (isTopLevelDestination) {
                                FuelSyncTopAppBar(
                                    currentRoute = currentRoute,
                                    onQuickAddClick = { navController.navigate("$ROUTE_LOG_ENTRY?prefillPartial=false") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavigationRoot(
                            navController = navController,
                            startDestination = startDestination,
                            onCompleteOnboarding = {
                                coroutineScope.launch {
                                    appEnvironment.preferences.setOnboardingCompleted(true)
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                                    }
                                }
                            },
                            currentUnitSystem = preferredUnitSystem,
                            onUnitSystemChanged = { unit ->
                                coroutineScope.launch {
                                    appEnvironment.preferences.setPreferredUnitSystem(unit)
                                }
                            },
                            isNightPumpMode = isOledNightMode,
                            onNightPumpModeToggled = { enabled ->
                                coroutineScope.launch {
                                    appEnvironment.preferences.setOledNightMode(enabled)
                                }
                            },
                            appEnvironment = appEnvironment,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FuelSyncTopAppBar(
    currentRoute: String?,
    onQuickAddClick: () -> Unit
) {
    val shapes = LocalExpressiveShapes.current

    CenterAlignedTopAppBar(
        title = {
            Surface(
                shape = shapes.pill,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = "Active vehicle",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "2024 Mazda CX-5",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalGasStation,
                        contentDescription = "FuelSync",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FuelSync",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(
                onClick = onQuickAddClick,
                modifier = Modifier.testTag("top_bar_quick_add")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add log",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
