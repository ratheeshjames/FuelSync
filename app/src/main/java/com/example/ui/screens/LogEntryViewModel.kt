package com.example.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.example.core.architecture.BaseViewModel
import com.example.core.architecture.UiAction
import com.example.core.architecture.UiEvent
import com.example.core.architecture.UiState
import com.example.core.di.AppEnvironment
import com.example.domain.model.FillType
import com.example.domain.model.FuelEntry
import com.example.domain.model.FuelType
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

@Immutable
data class LogEntryUiState(
    val vehicleId: String = "",
    val vehicleName: String = "",
    val lastOdometerKm: Double = 0.0,
    val odometerInput: String = "",
    val tripInput: String = "",
    val quantityInput: String = "",
    val unitPriceInput: String = "",
    val totalCostInput: String = "",
    val selectedFillType: FillType = FillType.FULL,
    val selectedFuelType: FuelType = FuelType.PETROL,
    val stationNameInput: String = "",
    val notesInput: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
    val validationError: String? = null,
    val isSaving: Boolean = false
) : UiState


sealed interface LogEntryUiAction : UiAction {
    data class Initialize(val prefillPartial: Boolean) : LogEntryUiAction
    data class UpdateOdometer(val input: String) : LogEntryUiAction
    data class UpdateTrip(val input: String) : LogEntryUiAction
    data class UpdateQuantity(val input: String) : LogEntryUiAction
    data class UpdateUnitPrice(val input: String) : LogEntryUiAction
    data class UpdateTotalCost(val input: String) : LogEntryUiAction
    data class SelectFillType(val fillType: FillType) : LogEntryUiAction
    data class SelectFuelType(val fuelType: FuelType) : LogEntryUiAction
    data class SelectStation(val stationName: String) : LogEntryUiAction
    data class UpdateStation(val input: String) : LogEntryUiAction
    data class UpdateNotes(val input: String) : LogEntryUiAction
    data object SubmitLog : LogEntryUiAction
}

sealed interface LogEntryUiEvent : UiEvent {
    data class ShowSnackbar(val message: String) : LogEntryUiEvent
    data object NavigateBackOnSuccess : LogEntryUiEvent
}

class LogEntryViewModel(
    private val appEnvironment: AppEnvironment
) : BaseViewModel<LogEntryUiState, LogEntryUiAction, LogEntryUiEvent>(LogEntryUiState()) {

    private val repository = appEnvironment.repository
    private val preferences = appEnvironment.preferences

    fun initForm(prefillPartial: Boolean) {
        viewModelScope.launch {
            val selectedVehicleId = preferences.selectedVehicleId.firstOrNull() ?: "car_mazda_cx5"
            val vehicle = repository.getVehicleById(selectedVehicleId)
                ?: repository.getAllVehicles().firstOrNull()?.firstOrNull()

            val lastOdo = vehicle?.let { repository.getLatestOdometer(it.id) } ?: 0.0
            val initialOdo = if (lastOdo > 0) (lastOdo + 520.0) else 12500.0

            updateState { state ->
                state.copy(
                    vehicleId = vehicle?.id ?: "",
                    vehicleName = vehicle?.name ?: "Primary Vehicle",
                    lastOdometerKm = lastOdo,
                    odometerInput = String.format(Locale.US, "%.0f", initialOdo),
                    tripInput = if (lastOdo > 0) "520" else "0",
                    quantityInput = "45.0",
                    unitPriceInput = "1.65",
                    totalCostInput = "74.25",
                    selectedFillType = if (prefillPartial) FillType.PARTIAL else FillType.FULL,
                    selectedFuelType = vehicle?.fuelType ?: FuelType.PETROL,
                    stationNameInput = "Shell V-Power"
                )
            }
        }
    }

    override fun handleIntent(action: LogEntryUiAction) {
        when (action) {
            is LogEntryUiAction.Initialize -> initForm(action.prefillPartial)

            is LogEntryUiAction.UpdateOdometer -> {
                val odo = action.input.toDoubleOrNull()
                val lastOdo = uiState.value.lastOdometerKm
                val derivedTrip = if (odo != null && lastOdo > 0 && odo >= lastOdo) {
                    String.format(Locale.US, "%.1f", odo - lastOdo)
                } else {
                    uiState.value.tripInput
                }
                updateState {
                    it.copy(
                        odometerInput = action.input,
                        tripInput = derivedTrip,
                        validationError = null
                    )
                }
            }

            is LogEntryUiAction.UpdateTrip -> {
                val trip = action.input.toDoubleOrNull()
                val lastOdo = uiState.value.lastOdometerKm
                val derivedOdo = if (trip != null && lastOdo > 0) {
                    String.format(Locale.US, "%.0f", lastOdo + trip)
                } else {
                    uiState.value.odometerInput
                }
                updateState {
                    it.copy(
                        tripInput = action.input,
                        odometerInput = derivedOdo,
                        validationError = null
                    )
                }
            }

            is LogEntryUiAction.UpdateQuantity -> {
                val qty = action.input.toDoubleOrNull()
                val unitPrice = uiState.value.unitPriceInput.toDoubleOrNull()
                val derivedTotal = if (qty != null && unitPrice != null && qty > 0) {
                    String.format(Locale.US, "%.2f", qty * unitPrice)
                } else {
                    uiState.value.totalCostInput
                }
                updateState {
                    it.copy(
                        quantityInput = action.input,
                        totalCostInput = derivedTotal,
                        validationError = null
                    )
                }
            }

            is LogEntryUiAction.UpdateUnitPrice -> {
                val price = action.input.toDoubleOrNull()
                val qty = uiState.value.quantityInput.toDoubleOrNull()
                val derivedTotal = if (price != null && qty != null && price > 0) {
                    String.format(Locale.US, "%.2f", qty * price)
                } else {
                    uiState.value.totalCostInput
                }
                updateState {
                    it.copy(
                        unitPriceInput = action.input,
                        totalCostInput = derivedTotal,
                        validationError = null
                    )
                }
            }

            is LogEntryUiAction.UpdateTotalCost -> {
                val total = action.input.toDoubleOrNull()
                val qty = uiState.value.quantityInput.toDoubleOrNull()
                val derivedPrice = if (total != null && qty != null && qty > 0) {
                    String.format(Locale.US, "%.3f", total / qty)
                } else {
                    uiState.value.unitPriceInput
                }
                updateState {
                    it.copy(
                        totalCostInput = action.input,
                        unitPriceInput = derivedPrice,
                        validationError = null
                    )
                }
            }

            is LogEntryUiAction.SelectFillType -> {
                updateState { it.copy(selectedFillType = action.fillType, validationError = null) }
            }

            is LogEntryUiAction.SelectFuelType -> {
                updateState { it.copy(selectedFuelType = action.fuelType) }
            }

            is LogEntryUiAction.SelectStation -> {
                updateState { it.copy(stationNameInput = action.stationName) }
            }

            is LogEntryUiAction.UpdateStation -> {
                updateState { it.copy(stationNameInput = action.input) }
            }

            is LogEntryUiAction.UpdateNotes -> {
                updateState { it.copy(notesInput = action.input) }
            }

            LogEntryUiAction.SubmitLog -> validateAndSave()
        }
    }

    private fun validateAndSave() {
        val state = uiState.value
        val odo = state.odometerInput.toDoubleOrNull()
        val qty = state.quantityInput.toDoubleOrNull()
        val price = state.unitPriceInput.toDoubleOrNull()
        val total = state.totalCostInput.toDoubleOrNull()
        val trip = state.tripInput.toDoubleOrNull()

        // Validation Checks
        if (odo == null || odo <= 0.0) {
            updateState { it.copy(validationError = "Please enter a valid positive odometer reading.") }
            return
        }

        if (state.lastOdometerKm > 0.0 && odo < state.lastOdometerKm && state.selectedFillType != FillType.MISSED_PREVIOUS) {
            updateState {
                it.copy(validationError = "Odometer (${odo.toInt()} km) cannot be lower than previous entry (${state.lastOdometerKm.toInt()} km). If you skipped a fill-up, select 'Missed Fill-Up'.")
            }
            return
        }

        if (qty == null || qty <= 0.0) {
            updateState { it.copy(validationError = "Fuel quantity must be greater than zero.") }
            return
        }

        if (total == null || total <= 0.0) {
            updateState { it.copy(validationError = "Total cost must be greater than zero.") }
            return
        }

        val resolvedUnitPrice = price ?: if (qty > 0) total / qty else 0.0

        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }

            val entry = FuelEntry(
                id = UUID.randomUUID().toString(),
                vehicleId = state.vehicleId,
                timestamp = state.timestampMillis,
                odometerKm = odo,
                tripDistanceKm = trip,
                fuelQuantityLiters = qty,
                unitPrice = resolvedUnitPrice,
                totalCost = total,
                fillType = state.selectedFillType,
                fuelType = state.selectedFuelType,
                stationName = state.stationNameInput.ifBlank { "Fuel Station" },
                notes = state.notesInput.ifBlank { null }
            )

            repository.insertFuelEntry(entry)
            updateState { it.copy(isSaving = false) }
            sendEvent(LogEntryUiEvent.ShowSnackbar("Fuel fill-up recorded successfully!"))
            sendEvent(LogEntryUiEvent.NavigateBackOnSuccess)
        }
    }
}
