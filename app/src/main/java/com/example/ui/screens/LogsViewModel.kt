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
import com.example.domain.model.DateRangeFilter
import com.example.domain.model.FillType
import com.example.domain.model.FuelEntry
import com.example.domain.model.FuelType
import com.example.domain.model.UnitPreference
import com.example.domain.model.Vehicle
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Immutable
data class LogsUiState(
    val isLoading: Boolean = true,
    val activeVehicle: Vehicle? = null,
    val allVehicles: List<Vehicle> = emptyList(),
    val allCalculatedEntries: List<CalculatedFuelEntry> = emptyList(),
    val filteredEntries: List<CalculatedFuelEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedDateRange: DateRangeFilter = DateRangeFilter.ALL,
    val selectedFuelType: FuelType? = null,
    val selectedFillType: FillType? = null,
    val isMultiSelectMode: Boolean = false,
    val selectedEntryIds: Set<String> = emptySet(),
    val isFastLogSheetVisible: Boolean = false,
    val isVehicleSwitcherSheetVisible: Boolean = false,
    val isTaggingSheetVisible: Boolean = false,
    val selectedEntryForTagging: CalculatedFuelEntry? = null,
    val averageUnitPrice: Double = 1.62,
    val unitPreference: UnitPreference = UnitPreference.L_PER_100KM
) : UiState


sealed interface LogsUiAction : UiAction {
    data class Search(val query: String) : LogsUiAction
    data class SetDateRange(val dateRange: DateRangeFilter) : LogsUiAction
    data class SetFuelType(val fuelType: FuelType?) : LogsUiAction
    data class SetFillType(val fillType: FillType?) : LogsUiAction
    data class ToggleSelectEntry(val id: String) : LogsUiAction
    data object SelectAll : LogsUiAction
    data object ClearSelection : LogsUiAction
    data object ExitMultiSelect : LogsUiAction
    data object DeleteSelected : LogsUiAction
    data class BulkMoveToVehicle(val targetVehicleId: String) : LogsUiAction
    data object ExportSelectedToCsv : LogsUiAction
    data class DuplicateEntry(val id: String) : LogsUiAction
    data class DeleteEntryWithUndo(val id: String) : LogsUiAction
    data object UndoDelete : LogsUiAction
    data class OpenTaggingSheet(val entry: CalculatedFuelEntry) : LogsUiAction
    data object CloseTaggingSheet : LogsUiAction
    data class SaveTags(val entry: FuelEntry) : LogsUiAction
    data class ToggleFastLogSheet(val visible: Boolean) : LogsUiAction
    data class ToggleVehicleSwitcherSheet(val visible: Boolean) : LogsUiAction
    data class SelectVehicle(val vehicleId: String) : LogsUiAction
}

sealed interface LogsUiEvent : UiEvent {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : LogsUiEvent
    data class ShareCsv(val csvContent: String) : LogsUiEvent
}

class LogsViewModel(
    private val appEnvironment: AppEnvironment
) : BaseViewModel<LogsUiState, LogsUiAction, LogsUiEvent>(LogsUiState()) {

    private val repository = appEnvironment.repository
    private val preferences = appEnvironment.preferences

    private var recentlyDeletedEntry: FuelEntry? = null

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
                observeVehicleEntries(active.id, mappedUnit)
            }
        }.launchIn(viewModelScope)
    }

    private fun observeVehicleEntries(vehicleId: String, unitPref: UnitPreference) {
        viewModelScope.launch {
            val avgPrice = repository.getAverageUnitPrice(vehicleId)
            updateState { it.copy(averageUnitPrice = avgPrice) }
        }

        repository.getCalculatedEntries(vehicleId, unitPref).onEach { entries ->
            updateState { state ->
                state.copy(
                    isLoading = false,
                    allCalculatedEntries = entries,
                    filteredEntries = applyFilters(
                        entries = entries,
                        query = state.searchQuery,
                        dateRange = state.selectedDateRange,
                        fuelType = state.selectedFuelType,
                        fillType = state.selectedFillType
                    )
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun handleIntent(action: LogsUiAction) {
        when (action) {
            is LogsUiAction.Search -> {
                updateState { state ->
                    state.copy(
                        searchQuery = action.query,
                        filteredEntries = applyFilters(
                            entries = state.allCalculatedEntries,
                            query = action.query,
                            dateRange = state.selectedDateRange,
                            fuelType = state.selectedFuelType,
                            fillType = state.selectedFillType
                        )
                    )
                }
            }

            is LogsUiAction.SetDateRange -> {
                updateState { state ->
                    state.copy(
                        selectedDateRange = action.dateRange,
                        filteredEntries = applyFilters(
                            entries = state.allCalculatedEntries,
                            query = state.searchQuery,
                            dateRange = action.dateRange,
                            fuelType = state.selectedFuelType,
                            fillType = state.selectedFillType
                        )
                    )
                }
            }

            is LogsUiAction.SetFuelType -> {
                updateState { state ->
                    state.copy(
                        selectedFuelType = action.fuelType,
                        filteredEntries = applyFilters(
                            entries = state.allCalculatedEntries,
                            query = state.searchQuery,
                            dateRange = state.selectedDateRange,
                            fuelType = action.fuelType,
                            fillType = state.selectedFillType
                        )
                    )
                }
            }

            is LogsUiAction.SetFillType -> {
                updateState { state ->
                    state.copy(
                        selectedFillType = action.fillType,
                        filteredEntries = applyFilters(
                            entries = state.allCalculatedEntries,
                            query = state.searchQuery,
                            dateRange = state.selectedDateRange,
                            fuelType = state.selectedFuelType,
                            fillType = action.fillType
                        )
                    )
                }
            }

            is LogsUiAction.ToggleSelectEntry -> {
                updateState { state ->
                    val newSelection = if (state.selectedEntryIds.contains(action.id)) {
                        state.selectedEntryIds - action.id
                    } else {
                        state.selectedEntryIds + action.id
                    }
                    state.copy(
                        selectedEntryIds = newSelection,
                        isMultiSelectMode = newSelection.isNotEmpty()
                    )
                }
            }

            LogsUiAction.SelectAll -> {
                updateState { state ->
                    state.copy(
                        selectedEntryIds = state.filteredEntries.map { it.entry.id }.toSet(),
                        isMultiSelectMode = true
                    )
                }
            }

            LogsUiAction.ClearSelection -> {
                updateState { it.copy(selectedEntryIds = emptySet()) }
            }

            LogsUiAction.ExitMultiSelect -> {
                updateState { it.copy(isMultiSelectMode = false, selectedEntryIds = emptySet()) }
            }

            LogsUiAction.DeleteSelected -> {
                val toDelete = uiState.value.selectedEntryIds.toList()
                if (toDelete.isNotEmpty()) {
                    viewModelScope.launch {
                        repository.bulkDeleteEntries(toDelete)
                        updateState { it.copy(selectedEntryIds = emptySet(), isMultiSelectMode = false) }
                        sendEvent(LogsUiEvent.ShowSnackbar("Deleted ${toDelete.size} fill-up logs"))
                    }
                }
            }

            is LogsUiAction.BulkMoveToVehicle -> {
                val toMove = uiState.value.selectedEntryIds.toList()
                if (toMove.isNotEmpty()) {
                    viewModelScope.launch {
                        repository.bulkChangeVehicle(toMove, action.targetVehicleId)
                        updateState { it.copy(selectedEntryIds = emptySet(), isMultiSelectMode = false) }
                        sendEvent(LogsUiEvent.ShowSnackbar("Moved ${toMove.size} logs to target vehicle"))
                    }
                }
            }

            LogsUiAction.ExportSelectedToCsv -> {
                val ids = uiState.value.selectedEntryIds
                val entriesToExport = if (ids.isNotEmpty()) {
                    uiState.value.allCalculatedEntries.filter { ids.contains(it.entry.id) }
                } else {
                    uiState.value.filteredEntries
                }

                val csv = buildCsvString(entriesToExport)
                sendEvent(LogsUiEvent.ShareCsv(csv))
                sendEvent(LogsUiEvent.ShowSnackbar("Exported ${entriesToExport.size} entries to CSV"))
            }

            is LogsUiAction.DuplicateEntry -> {
                viewModelScope.launch {
                    val dup = repository.duplicateFuelEntry(action.id)
                    if (dup != null) {
                        sendEvent(LogsUiEvent.ShowSnackbar("Duplicated fill-up at ${dup.odometerKm.toInt()} km"))
                    }
                }
            }

            is LogsUiAction.DeleteEntryWithUndo -> {
                val entryToDelete = uiState.value.allCalculatedEntries.find { it.entry.id == action.id }?.entry
                if (entryToDelete != null) {
                    recentlyDeletedEntry = entryToDelete
                    viewModelScope.launch {
                        repository.deleteFuelEntry(action.id)
                        sendEvent(LogsUiEvent.ShowSnackbar("Deleted fill-up", actionLabel = "Undo"))
                    }
                }
            }

            LogsUiAction.UndoDelete -> {
                val entryToRestore = recentlyDeletedEntry
                if (entryToRestore != null) {
                    viewModelScope.launch {
                        repository.insertFuelEntry(entryToRestore)
                        recentlyDeletedEntry = null
                        sendEvent(LogsUiEvent.ShowSnackbar("Restored fill-up record"))
                    }
                }
            }

            is LogsUiAction.OpenTaggingSheet -> {
                updateState {
                    it.copy(
                        selectedEntryForTagging = action.entry,
                        isTaggingSheetVisible = true
                    )
                }
            }

            LogsUiAction.CloseTaggingSheet -> {
                updateState { it.copy(isTaggingSheetVisible = false, selectedEntryForTagging = null) }
            }

            is LogsUiAction.SaveTags -> {
                viewModelScope.launch {
                    repository.updateFuelEntry(action.entry)
                    updateState { it.copy(isTaggingSheetVisible = false, selectedEntryForTagging = null) }
                    sendEvent(LogsUiEvent.ShowSnackbar("Updated driving tags and memo"))
                }
            }

            is LogsUiAction.ToggleFastLogSheet -> {
                updateState { it.copy(isFastLogSheetVisible = action.visible) }
            }

            is LogsUiAction.ToggleVehicleSwitcherSheet -> {
                updateState { it.copy(isVehicleSwitcherSheetVisible = action.visible) }
            }

            is LogsUiAction.SelectVehicle -> {
                viewModelScope.launch {
                    preferences.setSelectedVehicleId(action.vehicleId)
                    updateState { it.copy(isVehicleSwitcherSheetVisible = false) }
                }
            }
        }
    }

    private fun applyFilters(
        entries: List<CalculatedFuelEntry>,
        query: String,
        dateRange: DateRangeFilter,
        fuelType: FuelType?,
        fillType: FillType?
    ): List<CalculatedFuelEntry> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val dateThreshold = when (dateRange) {
            DateRangeFilter.ALL -> 0L
            DateRangeFilter.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.timeInMillis
            }
            DateRangeFilter.LAST_3_MONTHS -> {
                now - (90L * 24L * 60L * 60L * 1000L)
            }
            DateRangeFilter.YEAR_TO_DATE -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.timeInMillis
            }
        }

        return entries.filter { calculated ->
            val entry = calculated.entry

            // Date match
            val matchesDate = entry.timestamp >= dateThreshold

            // Fuel Type match
            val matchesFuel = fuelType == null || entry.fuelType == fuelType

            // Fill Type match
            val matchesFill = fillType == null || entry.fillType == fillType

            // Query match (station name, notes, driving tag)
            val matchesQuery = query.isBlank() ||
                (entry.stationName?.contains(query, ignoreCase = true) == true) ||
                (entry.notes?.contains(query, ignoreCase = true) == true) ||
                (entry.drivingEnvironment?.contains(query, ignoreCase = true) == true)

            matchesDate && matchesFuel && matchesFill && matchesQuery
        }
    }

    private fun buildCsvString(entries: List<CalculatedFuelEntry>): String {
        val sb = StringBuilder()
        sb.append("Date,Odometer(km),Trip(km),Volume(L),UnitPrice,TotalCost,FillType,FuelType,Station,Efficiency,DrivingEnv,AC,Notes\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        for (item in entries) {
            val e = item.entry
            sb.append("${dateFormat.format(Date(e.timestamp))},")
            sb.append("${e.odometerKm},")
            sb.append("${e.tripDistanceKm ?: ""},")
            sb.append("${e.fuelQuantityLiters},")
            sb.append("${e.unitPrice},")
            sb.append("${e.totalCost},")
            sb.append("${e.fillType.name},")
            sb.append("${e.fuelType.name},")
            sb.append("\"${e.stationName ?: ""}\",")
            sb.append("${item.calculatedEfficiency ?: ""},")
            sb.append("\"${e.drivingEnvironment ?: ""}\",")
            sb.append("${e.isAirConditioningOn},")
            sb.append("\"${e.notes ?: ""}\"\n")
        }
        return sb.toString()
    }
}
