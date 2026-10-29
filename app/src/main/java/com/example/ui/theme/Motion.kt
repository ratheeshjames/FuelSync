package com.example.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class ExpressiveMotionTokens(
    // Expressive Bouncy Spring for interactive chips, dials, and page indicators
    val springBouncy: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    ),
    // Snappy Spring for fast tab switches and bottom sheets
    val springSnappy: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    // Gentle Spring for wide layout expansions and large charts
    val springGentle: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    ),
    val emphasizedDecel: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f),
    val emphasizedAccel: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f),
    val standardEasing: Easing = FastOutSlowInEasing
)

val LocalExpressiveMotion = staticCompositionLocalOf { ExpressiveMotionTokens() }
