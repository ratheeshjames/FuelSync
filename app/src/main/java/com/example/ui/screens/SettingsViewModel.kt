package com.example.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.di.AppEnvironment
import com.example.core.di.FuelUnitSystem
import com.example.domain.model.CurrencyPreference
import com.example.ui.theme.FuelAccentTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Immutable
data class MaintenanceItemState(
    val title: String,
    val intervalKm: Double,
    val lastServiceOdoKm: Double,
    val currentOdoKm: Double,
    val remainingKm: Double,
    val progress: Float, // 0f to 1f (1f means service due or overdue)
    val isDue: Boolean
)

@Immutable
data class SettingsUiState(
    val currentUnitSystem: FuelUnitSystem = FuelUnitSystem.METRIC_L_PER_100KM,
    val currentCurrency: CurrencyPreference = CurrencyPreference.USD,
    val isNightPumpMode: Boolean = false,
    val isDynamicColor: Boolean = false,
    val currentAccentTheme: FuelAccentTheme = FuelAccentTheme.ELECTRIC_CYAN,
    val odometerOffsetKm: Double = 0.0,
    val tireCorrectionPercent: Double = 0.0,
    val latestOdometerKm: Double = 32800.0,
    val oilMaintenance: MaintenanceItemState = MaintenanceItemState(
        title = "Engine Oil & Filter",
        intervalKm = 8000.0,
        lastServiceOdoKm = 28000.0,
        currentOdoKm = 32800.0,
        remainingKm = 3200.0,
        progress = 0.6f,
        isDue = false
    ),
    val tireRotationMaintenance: MaintenanceItemState = MaintenanceItemState(
        title = "Tire Rotation & Balance",
        intervalKm = 10000.0,
        lastServiceOdoKm = 25000.0,
        currentOdoKm = 32800.0,
        remainingKm = 2200.0,
        progress = 0.78f,
        isDue = false
    ),
    val brakePadsMaintenance: MaintenanceItemState = MaintenanceItemState(
        title = "Brake Pads & Rotors",
        intervalKm = 25000.0,
        lastServiceOdoKm = 15000.0,
        currentOdoKm = 32800.0,
        remainingKm = 7200.0,
        progress = 0.71f,
        isDue = false
    ),
    val snackbarMessage: String? = null
)


sealed interface SettingsUiAction {
    data class SetUnitSystem(val unit: FuelUnitSystem) : SettingsUiAction
    data class SetCurrency(val currency: CurrencyPreference) : SettingsUiAction
    data class SetNightPumpMode(val enabled: Boolean) : SettingsUiAction
    data class SetDynamicColor(val enabled: Boolean) : SettingsUiAction
    data class SetAccentTheme(val accent: FuelAccentTheme) : SettingsUiAction
    data class SetOdometerOffset(val offsetKm: Double) : SettingsUiAction
    data class SetTireCorrection(val percent: Double) : SettingsUiAction
    data object ResetOilService : SettingsUiAction
    data object ResetTireRotationService : SettingsUiAction
    data object ResetBrakeService : SettingsUiAction
    data object ClearSnackbar : SettingsUiAction
}

class SettingsViewModel(
    private val appEnvironment: AppEnvironment
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private data class GeneralPrefs(
        val unit: FuelUnitSystem,
        val currency: CurrencyPreference,
        val oled: Boolean,
        val dynamicColor: Boolean
    )

    private data class CalibrationData(
        val accent: FuelAccentTheme,
        val odoOffset: Double,
        val tireCorr: Double,
        val entries: List<com.example.domain.model.FuelEntry>
    )

    private fun observeSettings() {
        viewModelScope.launch {
            val generalPrefsFlow = combine(
                appEnvironment.preferences.preferredUnitSystem,
                appEnvironment.preferences.preferredCurrency,
                appEnvironment.preferences.isOledNightModeEnabled,
                appEnvironment.preferences.isDynamicColorEnabled
            ) { unit, currency, oled, dynamicColor ->
                GeneralPrefs(unit, currency, oled, dynamicColor)
            }

            val calibrationFlow = combine(
                appEnvironment.preferences.fuelAccentTheme,
                appEnvironment.preferences.odometerOffsetKm,
                appEnvironment.preferences.tireCorrectionPercent,
                appEnvironment.repository.getAllEntries()
            ) { accent, odoOffset, tireCorr, entries ->
                CalibrationData(accent, odoOffset, tireCorr, entries)
            }

            combine(generalPrefsFlow, calibrationFlow) { general, calib ->
                val lastOdo = calib.entries.maxOfOrNull { it.odometerKm } ?: 32800.0
                val correctedOdo = (lastOdo + calib.odoOffset) * (1.0 + (calib.tireCorr / 100.0))

                _uiState.update { current ->
                    current.copy(
                        currentUnitSystem = general.unit,
                        currentCurrency = general.currency,
                        isNightPumpMode = general.oled,
                        isDynamicColor = general.dynamicColor,
                        currentAccentTheme = calib.accent,
                        odometerOffsetKm = calib.odoOffset,
                        tireCorrectionPercent = calib.tireCorr,
                        latestOdometerKm = roundToTwoDecimals(correctedOdo),
                        oilMaintenance = calculateMaintenance(
                            title = "Engine Oil & Filter",
                            intervalKm = 8000.0,
                            lastServiceKm = current.oilMaintenance.lastServiceOdoKm,
                            currentOdoKm = correctedOdo
                        ),
                        tireRotationMaintenance = calculateMaintenance(
                            title = "Tire Rotation & Balance",
                            intervalKm = 10000.0,
                            lastServiceKm = current.tireRotationMaintenance.lastServiceOdoKm,
                            currentOdoKm = correctedOdo
                        ),
                        brakePadsMaintenance = calculateMaintenance(
                            title = "Brake Pads & Rotors",
                            intervalKm = 25000.0,
                            lastServiceKm = current.brakePadsMaintenance.lastServiceOdoKm,
                            currentOdoKm = correctedOdo
                        )
                    )
                }
            }.collect {}
        }
    }

    fun handleAction(action: SettingsUiAction) {
        viewModelScope.launch {
            when (action) {
                is SettingsUiAction.SetUnitSystem -> {
                    appEnvironment.preferences.setPreferredUnitSystem(action.unit)
                    _uiState.update { it.copy(snackbarMessage = "Efficiency unit updated to ${action.unit.label}") }
                }
                is SettingsUiAction.SetCurrency -> {
                    appEnvironment.preferences.setPreferredCurrency(action.currency)
                    _uiState.update { it.copy(snackbarMessage = "Currency set to ${action.currency.code} (${action.currency.symbol})") }
                }
                is SettingsUiAction.SetNightPumpMode -> {
                    appEnvironment.preferences.setOledNightMode(action.enabled)
                }
                is SettingsUiAction.SetDynamicColor -> {
                    appEnvironment.preferences.setDynamicColor(action.enabled)
                }
                is SettingsUiAction.SetAccentTheme -> {
                    appEnvironment.preferences.setFuelAccentTheme(action.accent)
                    _uiState.update { it.copy(snackbarMessage = "Theme switched to ${action.accent.displayName}") }
                }
                is SettingsUiAction.SetOdometerOffset -> {
                    appEnvironment.preferences.setOdometerOffsetKm(action.offsetKm)
                    _uiState.update { it.copy(snackbarMessage = "Odometer offset saved: ${action.offsetKm} km") }
                }
                is SettingsUiAction.SetTireCorrection -> {
                    appEnvironment.preferences.setTireCorrectionPercent(action.percent)
                    _uiState.update { it.copy(snackbarMessage = "Tire diameter correction: ${action.percent}%") }
                }
                SettingsUiAction.ResetOilService -> {
                    val currentOdo = _uiState.value.latestOdometerKm
                    appEnvironment.preferences.setLastOilServiceOdoKm(currentOdo)
                    _uiState.update { current ->
                        current.copy(
                            oilMaintenance = calculateMaintenance(
                                title = "Engine Oil & Filter",
                                intervalKm = 8000.0,
                                lastServiceKm = currentOdo,
                                currentOdoKm = currentOdo
                            ),
                            snackbarMessage = "Engine oil interval reset to full service life!"
                        )
                    }
                }
                SettingsUiAction.ResetTireRotationService -> {
                    val currentOdo = _uiState.value.latestOdometerKm
                    appEnvironment.preferences.setLastTireRotationOdoKm(currentOdo)
                    _uiState.update { current ->
                        current.copy(
                            tireRotationMaintenance = calculateMaintenance(
                                title = "Tire Rotation & Balance",
                                intervalKm = 10000.0,
                                lastServiceKm = currentOdo,
                                currentOdoKm = currentOdo
                            ),
                            snackbarMessage = "Tire rotation interval reset!"
                        )
                    }
                }
                SettingsUiAction.ResetBrakeService -> {
                    val currentOdo = _uiState.value.latestOdometerKm
                    appEnvironment.preferences.setLastBrakeServiceOdoKm(currentOdo)
                    _uiState.update { current ->
                        current.copy(
                            brakePadsMaintenance = calculateMaintenance(
                                title = "Brake Pads & Rotors",
                                intervalKm = 25000.0,
                                lastServiceKm = currentOdo,
                                currentOdoKm = currentOdo
                            ),
                            snackbarMessage = "Brake pads inspection interval reset!"
                        )
                    }
                }
                SettingsUiAction.ClearSnackbar -> {
                    _uiState.update { it.copy(snackbarMessage = null) }
                }
            }
        }
    }

    private fun calculateMaintenance(
        title: String,
        intervalKm: Double,
        lastServiceKm: Double,
        currentOdoKm: Double
    ): MaintenanceItemState {
        val drivenSinceService = (currentOdoKm - lastServiceKm).coerceAtLeast(0.0)
        val remaining = (intervalKm - drivenSinceService)
        val progress = (drivenSinceService / intervalKm).coerceIn(0.0, 1.0).toFloat()
        val isDue = remaining <= 0.0

        return MaintenanceItemState(
            title = title,
            intervalKm = intervalKm,
            lastServiceOdoKm = lastServiceKm,
            currentOdoKm = currentOdoKm,
            remainingKm = roundToTwoDecimals(remaining),
            progress = progress,
            isDue = isDue
        )
    }

    private fun roundToTwoDecimals(v: Double): Double = (v * 100.0).roundToInt() / 100.0
}
