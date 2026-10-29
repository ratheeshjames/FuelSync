package com.example.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.example.core.architecture.BaseViewModel
import com.example.core.architecture.UiAction
import com.example.core.architecture.UiEvent
import com.example.core.architecture.UiState
import com.example.core.di.AppEnvironment
import com.example.core.di.FuelUnitSystem
import com.example.domain.model.CalculatedFuelEntry
import com.example.domain.model.CurrencyPreference
import com.example.domain.model.DashboardMetrics
import com.example.domain.model.UnitPreference
import com.example.domain.model.Vehicle
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Immutable
data class DashboardUiState(
    val isLoading: Boolean = true,
    val activeVehicle: Vehicle? = null,
    val allVehicles: List<Vehicle> = emptyList(),
    val metrics: DashboardMetrics? = null,
    val recentEntries: List<CalculatedFuelEntry> = emptyList(),
    val isQuickLogExpanded: Boolean = false,
    val unitPreference: UnitPreference = UnitPreference.L_PER_100KM,
    val currencyPreference: CurrencyPreference = CurrencyPreference.USD
) : UiState


sealed interface DashboardUiAction : UiAction {
    data class SelectVehicle(val vehicleId: String) : DashboardUiAction
    data object ToggleQuickLogFab : DashboardUiAction
    data class DeleteEntry(val entryId: String) : DashboardUiAction
    data object Refresh : DashboardUiAction
}

sealed interface DashboardUiEvent : UiEvent {
    data class ShowSnackbar(val message: String) : DashboardUiEvent
    data class NavigateToLogEntry(val prefillPartial: Boolean = false) : DashboardUiEvent
}

class DashboardViewModel(
    private val appEnvironment: AppEnvironment
) : BaseViewModel<DashboardUiState, DashboardUiAction, DashboardUiEvent>(DashboardUiState()) {

    private val repository = appEnvironment.repository
    private val preferences = appEnvironment.preferences

    init {
        observeData()
    }

    private fun observeData() {
        combine(
            repository.getAllVehicles(),
            preferences.selectedVehicleId,
            preferences.preferredUnitSystem
        ) { vehicles, selectedId, preferredUnit ->
            val mappedUnit = when (preferredUnit) {
                FuelUnitSystem.METRIC_L_PER_100KM -> UnitPreference.L_PER_100KM
                FuelUnitSystem.METRIC_KM_PER_L -> UnitPreference.KM_PER_L
                FuelUnitSystem.US_MPG -> UnitPreference.MPG_US
                FuelUnitSystem.UK_MPG -> UnitPreference.MPG_UK
            }

            val active = vehicles.find { it.id == selectedId } ?: vehicles.firstOrNull()

            updateState { state ->
                state.copy(
                    activeVehicle = active,
                    allVehicles = vehicles,
                    unitPreference = mappedUnit
                )
            }

            if (active != null) {
                observeEntriesAndMetrics(active.id, mappedUnit)
            }
        }.launchIn(viewModelScope)
    }

    private fun observeEntriesAndMetrics(vehicleId: String, unitPref: UnitPreference) {
        combine(
            repository.getCalculatedEntries(vehicleId, unitPref),
            repository.getDashboardMetrics(vehicleId, unitPref, CurrencyPreference.USD)
        ) { entries, metrics ->
            updateState { state ->
                state.copy(
                    isLoading = false,
                    recentEntries = entries,
                    metrics = metrics
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun handleIntent(action: DashboardUiAction) {
        when (action) {
            is DashboardUiAction.SelectVehicle -> {
                viewModelScope.launch {
                    preferences.setSelectedVehicleId(action.vehicleId)
                }
            }
            is DashboardUiAction.ToggleQuickLogFab -> {
                updateState { it.copy(isQuickLogExpanded = !it.isQuickLogExpanded) }
            }
            is DashboardUiAction.DeleteEntry -> {
                viewModelScope.launch {
                    repository.deleteFuelEntry(action.entryId)
                    sendEvent(DashboardUiEvent.ShowSnackbar("Fuel log deleted"))
                }
            }
            DashboardUiAction.Refresh -> {
                viewModelScope.launch {
                    repository.seedDatabaseIfEmpty()
                }
            }
        }
    }
}
