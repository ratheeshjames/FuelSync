package com.example.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.CalculatedFuelEntry
import com.example.domain.model.FuelEntry
import com.example.ui.theme.LocalExpressiveShapes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptAndTaggingBottomSheet(
    sheetState: SheetState,
    calculatedEntry: CalculatedFuelEntry?,
    onDismiss: () -> Unit,
    onSaveTags: (FuelEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    if (calculatedEntry == null) return
    val shapes = LocalExpressiveShapes.current
    val entry = calculatedEntry.entry

    val environmentOptions = listOf(
        "Highway 100%",
        "Mixed 50/50",
        "City / Stop & Go",
        "Mountain Ascent"
    )

    var selectedEnvironment by remember(entry.id) {
        mutableStateOf(entry.drivingEnvironment ?: "Mixed 50/50")
    }
    var isAcOn by remember(entry.id) {
        mutableStateOf(entry.isAirConditioningOn)
    }
    var notesText by remember(entry.id) {
        mutableStateOf(entry.notes ?: "")
    }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US) }
    val formattedDate = remember(entry.timestamp) { dateFormat.format(Date(entry.timestamp)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = shapes.telemetryCard,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("receipt_and_tagging_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Receipt & Driving Tags",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${entry.stationName ?: "Station"} • $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$${String.format(Locale.US, "%.2f", entry.totalCost)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Section 1: Driving Environment Tag Chips
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.telemetryCard,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Loyalty,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "DRIVING ENVIRONMENT SPLIT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        environmentOptions.take(2).forEach { env ->
                            FilterChip(
                                selected = selectedEnvironment == env,
                                onClick = { selectedEnvironment = env },
                                label = { Text(env) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        environmentOptions.drop(2).forEach { env ->
                            FilterChip(
                                selected = selectedEnvironment == env,
                                onClick = { selectedEnvironment = env },
                                label = { Text(env) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // Section 2: Climate Control (A/C On/Off)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.telemetryCard,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AcUnit,
                            contentDescription = null,
                            tint = if (isAcOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Column {
                            Text(
                                text = "A/C & Climate Active",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAcOn) "Continuous A/C (~5-8% drag)" else "A/C turned off",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isAcOn,
                        onCheckedChange = { isAcOn = it }
                    )
                }
            }

            // Section 3: Receipt Notes Text Field
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Receipt Memo / Notes") },
                placeholder = { Text("e.g., Highway toll receipt #44, tire pressure at 34 PSI") },
                leadingIcon = {
                    Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("receipt_notes_input")
            )

            // Save Action
            Button(
                onClick = {
                    val updated = entry.copy(
                        drivingEnvironment = selectedEnvironment,
                        isAirConditioningOn = isAcOn,
                        notes = notesText.ifBlank { null }
                    )
                    onSaveTags(updated)
                    onDismiss()
                },
                shape = shapes.pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_tags_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Text(
                        text = "Save Tags & Memo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
