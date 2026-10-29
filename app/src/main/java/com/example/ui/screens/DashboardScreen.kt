package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CalculatedFuelEntry
import com.example.domain.model.DashboardMetrics
import com.example.domain.model.FillType
import com.example.domain.model.FuelEntry
import com.example.ui.sheets.FastLogBottomSheet
import com.example.ui.sheets.ReceiptAndTaggingBottomSheet
import com.example.ui.sheets.VehicleSwitcherBottomSheet
import com.example.ui.theme.LocalExpressiveShapes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onAction: (DashboardUiAction) -> Unit,
    onNavigateToLogEntry: (prefillPartial: Boolean) -> Unit,
    onSaveFastLog: (FuelEntry) -> Unit = {},
    onSaveTags: (FuelEntry) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val shapes = LocalExpressiveShapes.current

    val vehicleSwitcherSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val fastLogSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val taggingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var showVehicleSwitcherSheet by remember { mutableStateOf(false) }
    var showFastLogSheet by remember { mutableStateOf(false) }
    var selectedEntryForTagging by remember { mutableStateOf<CalculatedFuelEntry?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vehicle Header Selector Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = shapes.telemetryCard,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable { showVehicleSwitcherSheet = true }
                                    .testTag("dashboard_vehicle_switcher_trigger")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE VEHICLE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "▼",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Text(
                                    text = uiState.activeVehicle?.name ?: "Select Vehicle",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${uiState.activeVehicle?.fuelType?.displayName ?: ""} • Tank: ${uiState.activeVehicle?.tankCapacityLiters?.toInt() ?: 0}L",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DirectionsCar,
                                    contentDescription = "Vehicle",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Odometer & Estimated Range
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val currentOdo = uiState.metrics?.lastOdometerKm ?: uiState.activeVehicle?.initialOdometerKm ?: 0.0
                            Column {
                                Text(
                                    text = "LATEST ODOMETER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%,.0f", currentOdo)} km",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "ESTIMATED RANGE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "~540 km",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Hero Metric Card: Latest Mileage, Average Mileage & 30-Day Fuel Expenditure
            item {
                HeroMetricCard(metrics = uiState.metrics)
            }

            // Recent Fill-ups Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Fill-ups",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${uiState.recentEntries.size} recorded",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Recent Fill-ups Feed with Expressive Status Indicators
            if (uiState.recentEntries.isEmpty()) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = shapes.telemetryCard
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalGasStation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No fuel logs recorded yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap '+ Log Fuel' below to record your first fill-up",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.recentEntries, key = { it.entry.id }) { calculatedEntry ->
                    val onCardClick = remember(calculatedEntry.entry.id) {
                        { selectedEntryForTagging = calculatedEntry }
                    }
                    CalculatedFuelItemCard(
                        calculated = calculatedEntry,
                        onClick = onCardClick
                    )
                }
            }


            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // M3E Animated Expandable Quick-Log FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedVisibility(
                    visible = uiState.isQuickLogExpanded,
                    enter = fadeIn() + scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut() + scaleOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 5-Second Rapid Pump Log
                        ExtendedFloatingActionButton(
                            onClick = {
                                onAction(DashboardUiAction.ToggleQuickLogFab)
                                showFastLogSheet = true
                            },
                            shape = shapes.pill,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.testTag("fast_log_fab_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("5s Fast Pump Log")
                        }

                        // Partial Splash Fill
                        ExtendedFloatingActionButton(
                            onClick = {
                                onAction(DashboardUiAction.ToggleQuickLogFab)
                                onNavigateToLogEntry(true)
                            },
                            shape = shapes.pill,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.testTag("quick_log_partial_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HourglassTop,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Partial Splash Fill")
                        }

                        // Full Tank Fill-Up
                        ExtendedFloatingActionButton(
                            onClick = {
                                onAction(DashboardUiAction.ToggleQuickLogFab)
                                onNavigateToLogEntry(false)
                            },
                            shape = shapes.pill,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.testTag("quick_log_full_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalGasStation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Full Tank Fill-Up")
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { onAction(DashboardUiAction.ToggleQuickLogFab) },
                    shape = shapes.pill,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("expandable_quick_log_fab")
                ) {
                    Icon(
                        imageVector = if (uiState.isQuickLogExpanded) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = "Quick Log"
                    )
                }
            }
        }
    }

    // Vehicle Switcher Sheet
    if (showVehicleSwitcherSheet) {
        VehicleSwitcherBottomSheet(
            sheetState = vehicleSwitcherSheetState,
            vehicles = uiState.allVehicles,
            activeVehicleId = uiState.activeVehicle?.id,
            onVehicleSelected = { vehicleId ->
                onAction(DashboardUiAction.SelectVehicle(vehicleId))
            },
            onDismiss = { showVehicleSwitcherSheet = false }
        )
    }

    // Fast-Log Sheet
    if (showFastLogSheet) {
        FastLogBottomSheet(
            sheetState = fastLogSheetState,
            vehicle = uiState.activeVehicle,
            latestOdometerKm = uiState.metrics?.lastOdometerKm ?: uiState.activeVehicle?.initialOdometerKm ?: 0.0,
            averageUnitPrice = 1.62,
            onDismiss = { showFastLogSheet = false },
            onSaveFastLog = { entry ->
                onSaveFastLog(entry)
                showFastLogSheet = false
            }
        )
    }

    // Receipt & Tagging Sheet
    if (selectedEntryForTagging != null) {
        ReceiptAndTaggingBottomSheet(
            sheetState = taggingSheetState,
            calculatedEntry = selectedEntryForTagging,
            onDismiss = { selectedEntryForTagging = null },
            onSaveTags = { updatedEntry ->
                onSaveTags(updatedEntry)
                selectedEntryForTagging = null
            }
        )
    }
}

@Composable
private fun HeroMetricCard(metrics: DashboardMetrics?) {
    val shapes = LocalExpressiveShapes.current
    val unitTag = metrics?.unitPreference?.unitTag ?: "L/100km"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.telemetryCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "PERFORMANCE METRICS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Uniform Card Heights using IntrinsicSize.Min on parent Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Latest Mileage
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = shapes.telemetryCard,
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "LATEST",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = metrics?.latestEfficiency?.let { "$it" } ?: "--",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                        Text(
                            text = unitTag,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Average Mileage
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = shapes.telemetryCard,
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "LIFETIME AVG",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = metrics?.averageEfficiency?.let { "$it" } ?: "--",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                        Text(
                            text = unitTag,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 30-Day Fuel Spend
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = shapes.telemetryCard,
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "30-DAY SPEND",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$${metrics?.rolling30DayExpenditure?.toInt() ?: 0}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                        Text(
                            text = "Fuel Cost",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatedFuelItemCard(
    calculated: CalculatedFuelEntry,
    onClick: () -> Unit = {}
) {
    val shapes = LocalExpressiveShapes.current
    val entry = calculated.entry
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }
    val formattedDate = remember(entry.timestamp) { dateFormat.format(Date(entry.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.telemetryCard)
            .clickable(onClick = onClick),
        shape = shapes.telemetryCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Station & Total Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when (entry.fillType) {
                                    FillType.FULL -> MaterialTheme.colorScheme.primaryContainer
                                    FillType.PARTIAL -> MaterialTheme.colorScheme.secondaryContainer
                                    FillType.MISSED_PREVIOUS -> MaterialTheme.colorScheme.errorContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (entry.fillType) {
                                FillType.FULL -> Icons.Filled.LocalGasStation
                                FillType.PARTIAL -> Icons.Filled.HourglassTop
                                FillType.MISSED_PREVIOUS -> Icons.Filled.Warning
                            },
                            contentDescription = null,
                            tint = when (entry.fillType) {
                                FillType.FULL -> MaterialTheme.colorScheme.onPrimaryContainer
                                FillType.PARTIAL -> MaterialTheme.colorScheme.onSecondaryContainer
                                FillType.MISSED_PREVIOUS -> MaterialTheme.colorScheme.onErrorContainer
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = entry.stationName ?: "Fuel Station",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$formattedDate • ${String.format(Locale.US, "%,.0f", entry.odometerKm)} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "$${String.format(Locale.US, "%.2f", entry.totalCost)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Row: Volume, Unit Price, and Calculated Efficiency Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", entry.fuelQuantityLiters)} L @ $${String.format(Locale.US, "%.2f", entry.unitPrice)}/L",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Expressive Status Badge
                when {
                    calculated.isPendingPartial -> {
                        Surface(
                            shape = shapes.pill,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Partial (Queued)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    calculated.isMissedBoundary -> {
                        Surface(
                            shape = shapes.pill,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "Missed Fill Gap",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    calculated.calculatedEfficiency != null -> {
                        Surface(
                            shape = shapes.pill,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${calculated.calculatedEfficiency} (${calculated.deltaDistanceKm?.toInt()} km)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    else -> {
                        Surface(
                            shape = shapes.pill,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Baseline Anchor",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Notes if present
            if (!entry.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Memo: ${entry.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

