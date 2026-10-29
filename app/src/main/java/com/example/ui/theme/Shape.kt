package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

val FuelSyncShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Immutable
data class ExpressiveCustomShapes(
    val pill: Shape = RoundedCornerShape(percent = 50),
    val dialFrame: Shape = RoundedCornerShape(16.dp),
    val telemetryCard: Shape = RoundedCornerShape(22.dp),
    val asymmetricalTopCard: Shape = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 8.dp,
        bottomStart = 8.dp,
        bottomEnd = 28.dp
    ),
    val segmentedBadge: Shape = RoundedCornerShape(10.dp)
)

val LocalExpressiveShapes = staticCompositionLocalOf { ExpressiveCustomShapes() }
