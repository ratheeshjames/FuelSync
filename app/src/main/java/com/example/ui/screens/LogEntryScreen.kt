package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.FillType
import com.example.domain.model.FuelType
import com.example.ui.theme.LocalExpressiveShapes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEntryScreen(
    uiState: LogEntryUiState,
    onAction: (LogEntryUiAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shapes = LocalExpressiveShapes.current
    val scrollState = rememberScrollState()
    val stationHorizontalScroll = rememberScrollState()
    val fillStatusScrollState = rememberScrollState()
    val fuelGradeScrollState = rememberScrollState()

    val quickStations = listOf("Shell", "BP", "Costco", "Chevron", "Mobil 1", "Exxon")


    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Record Fill-Up",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("log_entry_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vehicle Context Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.telemetryCard,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = uiState.vehicleName.ifBlank { "Primary Vehicle" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Previous Verified Odometer: ${String.format(Locale.US, "%,.0f", uiState.lastOdometerKm)} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Validation Error Banner
            if (uiState.validationError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = shapes.telemetryCard,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.validationError,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Section 1: Distance & Odometer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.telemetryCard,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ODOMETER & DISTANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.odometerInput,
                            onValueChange = { onAction(LogEntryUiAction.UpdateOdometer(it)) },
                            label = { Text("Odometer (km)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Filled.Speed, contentDescription = null, modifier = Modifier.size(20.dp))
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("input_odometer")
                        )

                        OutlinedTextField(
                            value = uiState.tripInput,
                            onValueChange = { onAction(LogEntryUiAction.UpdateTrip(it)) },
                            label = { Text("Trip (km)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_trip")
                        )
                    }
                }
            }

            // Section 2: Fuel Quantity, Unit Price, Total Cost
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.telemetryCard,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "FUEL VOLUME & EXPENDITURE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = uiState.quantityInput,
                        onValueChange = { onAction(LogEntryUiAction.UpdateQuantity(it)) },
                        label = { Text("Quantity (Liters)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Filled.LocalGasStation, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_quantity")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.unitPriceInput,
                            onValueChange = { onAction(LogEntryUiAction.UpdateUnitPrice(it)) },
                            label = { Text("Price per Liter ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Filled.AttachMoney, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_unit_price")
                        )

                        OutlinedTextField(
                            value = uiState.totalCostInput,
                            onValueChange = { onAction(LogEntryUiAction.UpdateTotalCost(it)) },
                            label = { Text("Total Cost ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_total_cost")
                        )
                    }

                    Text(
                        text = "Tip: Modifying Quantity and Price auto-computes Total. Modifying Total auto-computes Price.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Section 3: Tank Fill Status (Full vs Partial vs Missed)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.telemetryCard,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TANK FILL STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(fillStatusScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        FilterChip(
                            selected = uiState.selectedFillType == FillType.FULL,
                            onClick = { onAction(LogEntryUiAction.SelectFillType(FillType.FULL)) },
                            label = { Text("Full Tank") },
                            leadingIcon = if (uiState.selectedFillType == FillType.FULL) {
                                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("chip_fill_full")
                        )

                        FilterChip(
                            selected = uiState.selectedFillType == FillType.PARTIAL,
                            onClick = { onAction(LogEntryUiAction.SelectFillType(FillType.PARTIAL)) },
                            label = { Text("Partial Tank") },
                            leadingIcon = if (uiState.selectedFillType == FillType.PARTIAL) {
                                { Icon(Icons.Filled.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.testTag("chip_fill_partial")
                        )

                        FilterChip(
                            selected = uiState.selectedFillType == FillType.MISSED_PREVIOUS,
                            onClick = { onAction(LogEntryUiAction.SelectFillType(FillType.MISSED_PREVIOUS)) },
                            label = { Text("Missed Fill") },
                            leadingIcon = if (uiState.selectedFillType == FillType.MISSED_PREVIOUS) {
                                { Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.testTag("chip_fill_missed")
                        )
                    }

                    // Calculation explainer note
                    Surface(
                        shape = shapes.segmentedBadge,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.selectedFillType.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            // Section 4: Fuel Grade & Gas Station
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.telemetryCard,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "FUEL GRADE & STATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Fuel Grade Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(fuelGradeScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        listOf(FuelType.PETROL, FuelType.DIESEL, FuelType.LPG, FuelType.CNG).forEach { grade ->
                            FilterChip(
                                selected = uiState.selectedFuelType == grade,
                                onClick = { onAction(LogEntryUiAction.SelectFuelType(grade)) },
                                label = { Text(grade.name) }
                            )
                        }
                    }

                    // Quick Station Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(stationHorizontalScroll),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickStations.forEach { brand ->
                            FilterChip(
                                selected = uiState.stationNameInput.startsWith(brand),
                                onClick = { onAction(LogEntryUiAction.SelectStation(brand)) },
                                label = { Text(brand) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.stationNameInput,
                        onValueChange = { onAction(LogEntryUiAction.UpdateStation(it)) },
                        label = { Text("Station Name / Location") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_station_name")
                    )

                    OutlinedTextField(
                        value = uiState.notesInput,
                        onValueChange = { onAction(LogEntryUiAction.UpdateNotes(it)) },
                        label = { Text("Notes (A/C load, weather, tire pressure)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_notes")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = { onAction(LogEntryUiAction.SubmitLog) },
                enabled = !uiState.isSaving,
                shape = shapes.pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_fuel_log_button")
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                        Text(
                            text = "Save Fill-Up Record",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
