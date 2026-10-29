package com.example.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.FillType
import com.example.domain.model.FuelEntry
import com.example.domain.model.Vehicle
import com.example.ui.theme.LocalExpressiveShapes
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastLogBottomSheet(
    sheetState: SheetState,
    vehicle: Vehicle?,
    latestOdometerKm: Double,
    averageUnitPrice: Double,
    onDismiss: () -> Unit,
    onSaveFastLog: (FuelEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val shapes = LocalExpressiveShapes.current

    val initialOdo = if (latestOdometerKm > 0) (latestOdometerKm + 500.0) else 12500.0
    var odometerText by remember { mutableStateOf(String.format(Locale.US, "%.0f", initialOdo)) }
    var amountPaidText by remember { mutableStateOf("75.00") }
    var stationText by remember { mutableStateOf("Fast Pump") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val amountPaid = amountPaidText.toDoubleOrNull() ?: 0.0
    val resolvedUnitPrice = if (averageUnitPrice > 0) averageUnitPrice else 1.65
    val derivedVolumeLiters = if (amountPaid > 0 && resolvedUnitPrice > 0) {
        amountPaid / resolvedUnitPrice
    } else 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = shapes.telemetryCard,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("fast_log_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Fast-Log Title & Speed Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Fast-Log at Pump",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = vehicle?.name ?: "Active Vehicle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = shapes.pill,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "5-Second Entry",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (errorText != null) {
                Surface(
                    shape = shapes.segmentedBadge,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Odometer Input
            OutlinedTextField(
                value = odometerText,
                onValueChange = {
                    odometerText = it
                    errorText = null
                },
                label = { Text("Current Odometer (km)") },
                leadingIcon = {
                    Icon(Icons.Filled.Speed, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fast_log_odometer_input")
            )

            // Amount Paid Input ($)
            OutlinedTextField(
                value = amountPaidText,
                onValueChange = {
                    amountPaidText = it
                    errorText = null
                },
                label = { Text("Total Amount Paid ($)") },
                leadingIcon = {
                    Icon(Icons.Filled.AttachMoney, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fast_log_amount_input")
            )

            // Derived Volume Summary Card
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
                    Column {
                        Text(
                            text = "ESTIMATED VOLUME",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", derivedVolumeLiters)} Liters",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "APPLIED PRICE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", resolvedUnitPrice)} / L",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Save in 5s Action Button
            Button(
                onClick = {
                    val odo = odometerText.toDoubleOrNull()
                    val cost = amountPaidText.toDoubleOrNull()
                    if (odo == null || odo <= 0.0) {
                        errorText = "Enter a valid odometer reading."
                        return@Button
                    }
                    if (latestOdometerKm > 0 && odo < latestOdometerKm) {
                        errorText = "Odometer cannot be lower than previous entry (${latestOdometerKm.toInt()} km)."
                        return@Button
                    }
                    if (cost == null || cost <= 0.0) {
                        errorText = "Enter a valid amount paid."
                        return@Button
                    }

                    val entry = FuelEntry(
                        id = UUID.randomUUID().toString(),
                        vehicleId = vehicle?.id ?: "car_mazda_cx5",
                        timestamp = System.currentTimeMillis(),
                        odometerKm = odo,
                        tripDistanceKm = if (latestOdometerKm > 0) (odo - latestOdometerKm) else 500.0,
                        fuelQuantityLiters = (derivedVolumeLiters * 10.0).toInt() / 10.0,
                        unitPrice = resolvedUnitPrice,
                        totalCost = cost,
                        fillType = FillType.FULL,
                        fuelType = vehicle?.fuelType ?: com.example.domain.model.FuelType.PETROL,
                        stationName = stationText,
                        notes = "Fast-logged at pump"
                    )
                    onSaveFastLog(entry)
                },
                shape = shapes.pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("fast_log_submit_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Text(
                        text = "Save in 5s (Full Tank)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
