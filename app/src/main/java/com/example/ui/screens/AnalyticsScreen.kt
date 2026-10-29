package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.derivedStateOf

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CurrencyPreference
import com.example.domain.model.UnitPreference
import com.example.ui.theme.FuelGradeDiesel
import com.example.ui.theme.FuelGradeLpg
import com.example.ui.theme.FuelGradePremium
import com.example.ui.theme.FuelGradeRegular
import com.example.ui.theme.LocalExpressiveShapes
import kotlin.math.atan2

@Composable
fun AnalyticsScreen(
    uiState: AnalyticsUiState,
    onAction: (AnalyticsUiAction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val shapes = LocalExpressiveShapes.current

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("analytics_loading_indicator")
            )
        }
        return
    }

    if (uiState.trendPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("analytics_empty_state"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoGraph,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Text(
                    text = "Awaiting Telemetry Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Log at least two consecutive fill-ups to render cubic Bezier efficiency curves, monthly expense projections, and condition splits.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Efficiency & Telemetry Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Pure Compose Canvas charts with moving average & condition scrubber",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section 1: Cubic Bezier Trend Chart with Scrubber
        item {
            MileageTrendCard(
                points = uiState.trendPoints,
                unitTag = uiState.unitPreference.unitTag,
                currencySymbol = uiState.currencyPreference.symbol,
                selectedPoint = uiState.selectedTrendPoint,
                onSelectPoint = { onAction(AnalyticsUiAction.SelectTrendPoint(it)) }
            )
        }

        // Section 2: Best Tank, Worst Tank & Cost Projection Row (Uniform Height via IntrinsicSize.Min)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Best Tank Card
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("analytics_best_tank_card"),
                    shape = shapes.telemetryCard
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EnergySavingsLeaf,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "BEST TANK",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.bestTankEfficiency?.let { "$it ${uiState.unitPreference.unitTag}" } ?: "--",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                        Text(
                            text = uiState.bestTankDate ?: "All-time optimal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 30-Day Cost Projection Card
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("analytics_projection_card"),
                    shape = shapes.telemetryCard
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "PROJECTED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.currencyPreference.symbol}${String.format(java.util.Locale.US, "%.2f", uiState.projectedNextMonthCost)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                        Text(
                            text = "Next 30-Day Forecast",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }


        // Section 3: Engine Health & Efficiency Degradation Indicator
        if (uiState.efficiencyDegradationPercent != null) {
            item {
                EfficiencyDegradationBanner(
                    degradationPercent = uiState.efficiencyDegradationPercent,
                    unitPreference = uiState.unitPreference
                )
            }
        }

        // Section 4: Monthly Cost vs. Distance Grouped Bar Chart
        if (uiState.monthlyData.isNotEmpty()) {
            item {
                MonthlyCostBarChartCard(
                    monthlyData = uiState.monthlyData,
                    currencySymbol = uiState.currencyPreference.symbol
                )
            }
        }

        // Section 5: Driving Conditions Donut Chart with Scrubber
        if (uiState.conditionSlices.isNotEmpty()) {
            item {
                DrivingConditionsDonutCard(
                    slices = uiState.conditionSlices,
                    selectedSliceIndex = uiState.selectedSliceIndex,
                    unitTag = uiState.unitPreference.unitTag,
                    onSelectSlice = { onAction(AnalyticsUiAction.SelectConditionSlice(it)) }
                )
            }
        }
    }
}

/**
 * Pure Canvas Bezier Trend Chart with 5-fill moving average overlay and drag scrubber
 */
@Composable
private fun MileageTrendCard(
    points: List<MileageTrendPoint>,
    unitTag: String,
    currencySymbol: String,
    selectedPoint: MileageTrendPoint?,
    onSelectPoint: (MileageTrendPoint?) -> Unit
) {
    val shapes = LocalExpressiveShapes.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val movingAverageColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val surfaceColor = MaterialTheme.colorScheme.surface

    var scrubberNormalizedX by remember { mutableFloatStateOf(-1f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mileage_trend_chart_card"),
        shape = shapes.telemetryCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header info with selected detail inspection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "ROLLING EFFICIENCY & MOVING AVG",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = selectedPoint?.let { "${it.efficiency} $unitTag" } ?: "Interactive Scrubber",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                }

                Surface(
                    shape = shapes.pill,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = selectedPoint?.dateLabel ?: "Tap/drag points",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Legend indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(10.dp, 3.dp).background(primaryColor, RoundedCornerShape(2.dp)))
                    Text("Tank Efficiency", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(10.dp, 2.dp).background(movingAverageColor, RoundedCornerShape(2.dp)))
                    Text("5-Fill Moving Avg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Canvas Chart Area with hardware-accelerated graphicsLayer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { clip = true }

                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val index = ((offset.x / size.width) * (points.size - 1))
                                    .coerceIn(0f, (points.size - 1).toFloat())
                                    .toInt()
                                onSelectPoint(points.getOrNull(index))
                                scrubberNormalizedX = offset.x / size.width
                            }
                        }
                        .pointerInput(points) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val index = ((offset.x / size.width) * (points.size - 1))
                                        .coerceIn(0f, (points.size - 1).toFloat())
                                        .toInt()
                                    onSelectPoint(points.getOrNull(index))
                                    scrubberNormalizedX = offset.x / size.width
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val index = ((change.position.x / size.width) * (points.size - 1))
                                        .coerceIn(0f, (points.size - 1).toFloat())
                                        .toInt()
                                    onSelectPoint(points.getOrNull(index))
                                    scrubberNormalizedX = (change.position.x / size.width).coerceIn(0f, 1f)
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    if (points.isEmpty()) return@Canvas

                    val minEff = (points.minOf { it.efficiency } * 0.9).coerceAtLeast(0.0)
                    val maxEff = points.maxOf { it.efficiency } * 1.1
                    val effRange = (maxEff - minEff).coerceAtLeast(1.0)

                    // Draw Horizontal Grid Reference Lines
                    val gridSteps = 3
                    for (i in 0..gridSteps) {
                        val y = height * (i.toFloat() / gridSteps)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Compute pixel coordinates for each point
                    val coords = points.mapIndexed { index, point ->
                        val x = if (points.size > 1) {
                            (index.toFloat() / (points.size - 1)) * width
                        } else width / 2f
                        val y = height - (((point.efficiency - minEff) / effRange).toFloat() * height)
                        Offset(x, y.coerceIn(8.dp.toPx(), height - 8.dp.toPx()))
                    }

                    val maCoords = points.mapIndexed { index, point ->
                        val x = if (points.size > 1) {
                            (index.toFloat() / (points.size - 1)) * width
                        } else width / 2f
                        val y = height - (((point.movingAverage - minEff) / effRange).toFloat() * height)
                        Offset(x, y.coerceIn(8.dp.toPx(), height - 8.dp.toPx()))
                    }

                    // 1. Shaded Gradient under Cubic Bezier Curve
                    if (coords.size >= 2) {
                        val fillPath = Path().apply {
                            moveTo(coords.first().x, height)
                            lineTo(coords.first().x, coords.first().y)
                            for (i in 1 until coords.size) {
                                val prev = coords[i - 1]
                                val curr = coords[i]
                                val midX = (prev.x + curr.x) / 2f
                                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                            lineTo(coords.last().x, height)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.28f),
                                    primaryColor.copy(alpha = 0.02f)
                                ),
                                startY = 0f,
                                endY = height
                            )
                        )
                    }

                    // 2. 5-Fill Moving Average Dashed Line
                    if (maCoords.size >= 2) {
                        val maPath = Path().apply {
                            moveTo(maCoords.first().x, maCoords.first().y)
                            for (i in 1 until maCoords.size) {
                                lineTo(maCoords[i].x, maCoords[i].y)
                            }
                        }
                        drawPath(
                            path = maPath,
                            color = movingAverageColor,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                            )
                        )
                    }

                    // 3. Main Cubic Bezier Curve Stroke
                    if (coords.size >= 2) {
                        val curvePath = Path().apply {
                            moveTo(coords.first().x, coords.first().y)
                            for (i in 1 until coords.size) {
                                val prev = coords[i - 1]
                                val curr = coords[i]
                                val midX = (prev.x + curr.x) / 2f
                                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                        }
                        drawPath(
                            path = curvePath,
                            color = primaryColor,
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // 4. Data Point Markers
                    coords.forEachIndexed { index, offset ->
                        val isSelected = (selectedPoint != null && points[index].entryId == selectedPoint.entryId)
                        if (isSelected) {
                            // Scrubber Vertical Line
                            drawLine(
                                color = primaryColor.copy(alpha = 0.6f),
                                start = Offset(offset.x, 0f),
                                end = Offset(offset.x, height),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 7.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = surfaceColor,
                                radius = 3.5.dp.toPx(),
                                center = offset
                            )
                        } else {
                            drawCircle(
                                color = primaryColor,
                                radius = 3.dp.toPx(),
                                center = offset
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inspection banner when point is selected
            if (selectedPoint != null) {
                Surface(
                    shape = shapes.telemetryCard,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Odometer: ${String.format(java.util.Locale.US, "%,.0f", selectedPoint.odometerKm)} km",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = selectedPoint.stationName ?: "Standard Station",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$currencySymbol${String.format(java.util.Locale.US, "%.2f", selectedPoint.totalCost)} Paid",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "MA: ${selectedPoint.movingAverage} $unitTag",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Month-over-Month Grouped Bar Chart comparing Spend ($) vs Distance Traveled (km)
 */
@Composable
private fun MonthlyCostBarChartCard(
    monthlyData: List<MonthlyCostData>,
    currencySymbol: String
) {
    val shapes = LocalExpressiveShapes.current
    val spendColor = MaterialTheme.colorScheme.primary
    val distColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    var selectedMonthIndex by remember { mutableStateOf(monthlyData.lastIndex) }
    val activeMonth = monthlyData.getOrNull(selectedMonthIndex) ?: monthlyData.last()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("monthly_cost_bar_chart_card"),
        shape = shapes.telemetryCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MONTHLY SPEND VS DISTANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currencySymbol${String.format(java.util.Locale.US, "%.2f", activeMonth.totalCost)} • ${String.format(java.util.Locale.US, "%,.0f", activeMonth.totalDistanceKm)} km",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = shapes.pill,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = activeMonth.monthLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Grouped Bar Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .pointerInput(monthlyData) {
                        detectTapGestures { offset ->
                            val slotWidth = size.width / monthlyData.size
                            val clickedIndex = (offset.x / slotWidth).toInt().coerceIn(0, monthlyData.lastIndex)
                            selectedMonthIndex = clickedIndex
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                val maxCost = (monthlyData.maxOfOrNull { it.totalCost } ?: 100.0).coerceAtLeast(50.0) * 1.15
                val maxDist = (monthlyData.maxOfOrNull { it.totalDistanceKm } ?: 500.0).coerceAtLeast(100.0) * 1.15

                val slotWidth = width / monthlyData.size
                val barWidth = (slotWidth * 0.32f).coerceAtMost(24.dp.toPx())

                // Baseline line
                drawLine(
                    color = gridColor,
                    start = Offset(0f, height - 1.dp.toPx()),
                    end = Offset(width, height - 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )

                monthlyData.forEachIndexed { index, month ->
                    val isSelected = (index == selectedMonthIndex)
                    val centerX = (index * slotWidth) + (slotWidth / 2f)

                    val costHeight = ((month.totalCost / maxCost).toFloat() * (height - 16.dp.toPx())).coerceAtLeast(4.dp.toPx())
                    val distHeight = ((month.totalDistanceKm / maxDist).toFloat() * (height - 16.dp.toPx())).coerceAtLeast(4.dp.toPx())

                    val costBarLeft = centerX - barWidth - 2.dp.toPx()
                    val distBarLeft = centerX + 2.dp.toPx()

                    // Spend Bar (Primary)
                    drawRoundRect(
                        color = if (isSelected) spendColor else spendColor.copy(alpha = 0.5f),
                        topLeft = Offset(costBarLeft, height - costHeight),
                        size = Size(barWidth, costHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Distance Bar (Tertiary)
                    drawRoundRect(
                        color = if (isSelected) distColor else distColor.copy(alpha = 0.5f),
                        topLeft = Offset(distBarLeft, height - distHeight),
                        size = Size(barWidth, distHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Month Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                monthlyData.forEachIndexed { index, month ->
                    Text(
                        text = month.monthLabel.split(" ").firstOrNull() ?: month.monthLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (index == selectedMonthIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == selectedMonthIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Driving Conditions Donut Chart with Scrubber
 */
@Composable
private fun DrivingConditionsDonutCard(
    slices: List<ConditionSliceData>,
    selectedSliceIndex: Int,
    unitTag: String,
    onSelectSlice: (Int) -> Unit
) {
    val shapes = LocalExpressiveShapes.current
    val palette = listOf(
        FuelGradeRegular,
        FuelGradeDiesel,
        FuelGradePremium,
        FuelGradeLpg
    )

    val activeSlice = slices.getOrNull(selectedSliceIndex) ?: slices.first()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driving_conditions_donut_card"),
        shape = shapes.telemetryCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "DRIVING ENVIRONMENT SPLIT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "City vs Highway vs Traffic Distribution",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Interactive Donut Canvas
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(slices) {
                                detectTapGestures { offset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val angleRad = atan2(offset.y - center.y, offset.x - center.x)
                                    var deg = Math.toDegrees(angleRad.toDouble()).toFloat()
                                    if (deg < 0) deg += 360f

                                    // Rotate to align with -90 start
                                    val shifted = (deg + 90f) % 360f

                                    var currentAngle = 0f
                                    for (i in slices.indices) {
                                        val sweep = (slices[i].percentage / 100f) * 360f
                                        if (shifted in currentAngle..(currentAngle + sweep)) {
                                            onSelectSlice(i)
                                            break
                                        }
                                        currentAngle += sweep
                                    }
                                }
                            }
                    ) {
                        val strokeWidth = 26.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                        val arcSize = Size(diameter, diameter)

                        var startAngle = -90f
                        slices.forEachIndexed { index, slice ->
                            val sweepAngle = (slice.percentage / 100f) * 360f
                            val isSelected = (index == selectedSliceIndex)
                            val color = palette[index % palette.size]

                            drawArc(
                                color = if (isSelected) color else color.copy(alpha = 0.6f),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle - 2f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(
                                    width = if (isSelected) strokeWidth * 1.15f else strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Center text overlay
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${activeSlice.percentage.toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "of total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Slices Details Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slices.forEachIndexed { index, slice ->
                        val isSelected = (index == selectedSliceIndex)
                        val color = palette[index % palette.size]

                        Surface(
                            shape = shapes.pill,
                            color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shapes.pill)
                                .clickable { onSelectSlice(index) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Text(
                                        text = slice.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = slice.averageEfficiency?.let { "$it $unitTag" } ?: "${slice.count} fills",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Efficiency Degradation Alert & Engine Health Banner
 */
@Composable
private fun EfficiencyDegradationBanner(
    degradationPercent: Double,
    unitPreference: UnitPreference
) {
    val shapes = LocalExpressiveShapes.current
    // For L/100km, positive degradation means worse (burn increased). For MPG, negative means worse.
    val isWorse = if (unitPreference == UnitPreference.L_PER_100KM) degradationPercent > 0.5 else degradationPercent < -0.5
    val containerColor = if (isWorse) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
    val contentColor = if (isWorse) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
    val icon = if (isWorse) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

    Surface(
        shape = shapes.telemetryCard,
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("efficiency_degradation_banner")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isWorse) "Efficiency Degradation Warning" else "Optimal Powertrain Health",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                val sign = if (degradationPercent > 0) "+" else ""
                Text(
                    text = if (isWorse) {
                        "Last 5 fills consume ${sign}${degradationPercent}% fuel versus lifetime baseline. Check tire pressure or air filter."
                    } else {
                        "Recent driving shows ${sign}${degradationPercent}% efficiency compared to all-time average."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}
