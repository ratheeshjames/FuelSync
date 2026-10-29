package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AdaptiveNavigationType {
    BOTTOM_NAVIGATION,    // Compact screens (phones in portrait, < 600dp width)
    NAVIGATION_RAIL,      // Medium screens (foldables, tablets in portrait, 600dp - 839dp)
    PERMANENT_DRAWER      // Expanded screens (tablets in landscape, desktop/DeX, >= 840dp)
}

@Composable
fun calculateAdaptiveNavigationType(availableWidth: Dp): AdaptiveNavigationType {
    return when {
        availableWidth < 600.dp -> AdaptiveNavigationType.BOTTOM_NAVIGATION
        availableWidth < 840.dp -> AdaptiveNavigationType.NAVIGATION_RAIL
        else -> AdaptiveNavigationType.PERMANENT_DRAWER
    }
}
