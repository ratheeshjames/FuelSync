package com.example.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.di.AppEnvironment
import com.example.core.di.FuelUnitSystem
import com.example.domain.engine.FuelCalculationEngine
import com.example.domain.model.CalculatedFuelEntry
import com.example.domain.model.CurrencyPreference
import com.example.domain.model.UnitPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Immutable
data class MileageTrendPoint(
    val entryId: String,
    val timestamp: Long,
    val dateLabel: String,
    val efficiency: Double,
    val movingAverage: Double,
    val odometerKm: Double,
    val totalCost: Double,
    val stationName: String?
)

@Immutable
data class MonthlyCostData(
    val monthLabel: String,
    val yearMonthKey: String,
    val totalCost: Double,
    val totalDistanceKm: Double,
    val fillCount: Int
)

@Immutable
data class ConditionSliceData(
    val label: String,
    val count: Int,
    val percentage: Float,
    val totalVolumeLiters: Double,
    val averageEfficiency: Double?
)

@Immutable
data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val calculatedEntries: List<CalculatedFuelEntry> = emptyList(),
    val unitPreference: UnitPreference = UnitPreference.L_PER_100KM,
    val currencyPreference: CurrencyPreference = CurrencyPreference.USD,
    val trendPoints: List<MileageTrendPoint> = emptyList(),
    val monthlyData: List<MonthlyCostData> = emptyList(),
    val conditionSlices: List<ConditionSliceData> = emptyList(),
    val bestTankEfficiency: Double? = null,
    val bestTankDate: String? = null,
    val worstTankEfficiency: Double? = null,
    val worstTankDate: String? = null,
    val projectedNextMonthCost: Double = 0.0,
    val efficiencyDegradationPercent: Double? = null, // Negative means improved, positive means higher consumption
    val selectedTrendPoint: MileageTrendPoint? = null,
    val selectedSliceIndex: Int = 0
)


sealed interface AnalyticsUiAction {
    data class SelectTrendPoint(val point: MileageTrendPoint?) : AnalyticsUiAction
    data class SelectConditionSlice(val index: Int) : AnalyticsUiAction
    data object Refresh : AnalyticsUiAction
}

class AnalyticsViewModel(
    private val appEnvironment: AppEnvironment
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                appEnvironment.repository.getAllEntries(),
                appEnvironment.preferences.preferredUnitSystem,
                appEnvironment.preferences.preferredCurrency
            ) { rawEntries, unitSystem, currency ->
                val unitPref = mapUnitSystem(unitSystem)
                val calculated = FuelCalculationEngine.processEntries(rawEntries, unitPref)
                Triple(calculated, unitPref, currency)
            }.collect { (calculated, unitPref, currency) ->
                computeAnalytics(calculated, unitPref, currency)
            }
        }
    }

    fun handleAction(action: AnalyticsUiAction) {
        when (action) {
            is AnalyticsUiAction.SelectTrendPoint -> {
                _uiState.update { it.copy(selectedTrendPoint = action.point) }
            }
            is AnalyticsUiAction.SelectConditionSlice -> {
                _uiState.update { it.copy(selectedSliceIndex = action.index) }
            }
            AnalyticsUiAction.Refresh -> {
                // Flow automatically refreshes on data emissions
            }
        }
    }

    private fun computeAnalytics(
        calculated: List<CalculatedFuelEntry>,
        unitPref: UnitPreference,
        currency: CurrencyPreference
    ) {
        if (calculated.isEmpty()) {
            _uiState.value = AnalyticsUiState(
                isLoading = false,
                unitPreference = unitPref,
                currencyPreference = currency
            )
            return
        }

        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        // Ascending chronological entries with valid efficiencies
        val entriesWithEfficiency = calculated
            .filter { it.calculatedEfficiency != null }
            .sortedBy { it.entry.timestamp }

        // Compute 5-fill rolling moving average
        val trendPoints = mutableListOf<MileageTrendPoint>()
        val window = ArrayDeque<Double>()

        for (item in entriesWithEfficiency) {
            val eff = item.calculatedEfficiency ?: continue
            window.addLast(eff)
            if (window.size > 5) {
                window.removeFirst()
            }
            val ma = roundToTwoDecimals(window.average())

            trendPoints.add(
                MileageTrendPoint(
                    entryId = item.entry.id,
                    timestamp = item.entry.timestamp,
                    dateLabel = dateFormat.format(Date(item.entry.timestamp)),
                    efficiency = eff,
                    movingAverage = ma,
                    odometerKm = item.entry.odometerKm,
                    totalCost = item.entry.totalCost,
                    stationName = item.entry.stationName
                )
            )
        }

        // Best and worst tanks
        // Note: In L/100km lower is better; in MPG and km/L higher is better
        val isLowerBetter = (unitPref == UnitPreference.L_PER_100KM)
        val bestEntry = if (isLowerBetter) {
            entriesWithEfficiency.minByOrNull { it.calculatedEfficiency ?: Double.MAX_VALUE }
        } else {
            entriesWithEfficiency.maxByOrNull { it.calculatedEfficiency ?: Double.MIN_VALUE }
        }

        val worstEntry = if (isLowerBetter) {
            entriesWithEfficiency.maxByOrNull { it.calculatedEfficiency ?: Double.MIN_VALUE }
        } else {
            entriesWithEfficiency.minByOrNull { it.calculatedEfficiency ?: Double.MAX_VALUE }
        }

        // Monthly cost & distance breakdown (last 6 months)
        val monthlyMap = mutableMapOf<String, MutableList<CalculatedFuelEntry>>()
        for (item in calculated) {
            val cal = Calendar.getInstance().apply { timeInMillis = item.entry.timestamp }
            val key = "${cal.get(Calendar.YEAR)}-${String.format(Locale.US, "%02d", cal.get(Calendar.MONTH))}"
            monthlyMap.getOrPut(key) { mutableListOf() }.add(item)
        }

        val monthlyData = monthlyMap.entries
            .sortedByDescending { it.key }
            .take(6)
            .reversed()
            .map { (key, entries) ->
                val sampleTimestamp = entries.first().entry.timestamp
                val label = monthFormat.format(Date(sampleTimestamp))
                val cost = entries.sumOf { it.entry.totalCost }
                val dist = entries.sumOf { it.deltaDistanceKm ?: 0.0 }
                MonthlyCostData(
                    monthLabel = label,
                    yearMonthKey = key,
                    totalCost = roundToTwoDecimals(cost),
                    totalDistanceKm = roundToTwoDecimals(dist),
                    fillCount = entries.size
                )
            }

        // Driving conditions split (Highway vs Mixed vs Heavy Traffic vs City)
        val conditionCounts = mutableMapOf<String, MutableList<CalculatedFuelEntry>>()
        for (item in calculated) {
            val rawTag = item.entry.drivingEnvironment?.takeIf { it.isNotBlank() } ?: "Mixed 50/50"
            conditionCounts.getOrPut(rawTag) { mutableListOf() }.add(item)
        }

        val totalLogs = calculated.size.toFloat()
        val conditionSlices = conditionCounts.map { (tag, entries) ->
            val sliceCount = entries.size
            val pct = (sliceCount / totalLogs) * 100f
            val vol = entries.sumOf { it.entry.fuelQuantityLiters }
            val avgEff = entries.mapNotNull { it.calculatedEfficiency }.average().takeIf { !it.isNaN() }
            ConditionSliceData(
                label = tag,
                count = sliceCount,
                percentage = roundToOneDecimal(pct),
                totalVolumeLiters = roundToTwoDecimals(vol),
                averageEfficiency = avgEff?.let { roundToTwoDecimals(it) }
            )
        }.sortedByDescending { it.percentage }

        // Next month cost projection based on rolling daily spend
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30L * 24L * 60L * 60L * 1000L)
        val recentFills = calculated.filter { it.entry.timestamp >= thirtyDaysAgo }
        val recentCost = recentFills.sumOf { it.entry.totalCost }
        val projectedCost = if (recentCost > 0) recentCost * 1.05 else (monthlyData.lastOrNull()?.totalCost ?: 150.0)

        // Efficiency degradation: compare recent 5 fills average to all-time average
        val allTimeAvg = entriesWithEfficiency.mapNotNull { it.calculatedEfficiency }.average()
        val recent5Avg = entriesWithEfficiency.takeLast(5).mapNotNull { it.calculatedEfficiency }.average()
        val degradationPercent = if (!allTimeAvg.isNaN() && !recent5Avg.isNaN() && allTimeAvg > 0) {
            val diff = ((recent5Avg - allTimeAvg) / allTimeAvg) * 100.0
            roundToOneDecimal(diff.toFloat()).toDouble()
        } else null

        _uiState.value = AnalyticsUiState(
            isLoading = false,
            calculatedEntries = calculated,
            unitPreference = unitPref,
            currencyPreference = currency,
            trendPoints = trendPoints,
            monthlyData = monthlyData,
            conditionSlices = conditionSlices,
            bestTankEfficiency = bestEntry?.calculatedEfficiency,
            bestTankDate = bestEntry?.let { dateFormat.format(Date(it.entry.timestamp)) },
            worstTankEfficiency = worstEntry?.calculatedEfficiency,
            worstTankDate = worstEntry?.let { dateFormat.format(Date(it.entry.timestamp)) },
            projectedNextMonthCost = roundToTwoDecimals(projectedCost),
            efficiencyDegradationPercent = degradationPercent,
            selectedTrendPoint = trendPoints.lastOrNull(),
            selectedSliceIndex = 0
        )
    }

    private fun mapUnitSystem(system: FuelUnitSystem): UnitPreference = when (system) {
        FuelUnitSystem.METRIC_L_PER_100KM -> UnitPreference.L_PER_100KM
        FuelUnitSystem.METRIC_KM_PER_L -> UnitPreference.KM_PER_L
        FuelUnitSystem.US_MPG -> UnitPreference.MPG_US
        FuelUnitSystem.UK_MPG -> UnitPreference.MPG_UK
    }

    private fun roundToTwoDecimals(value: Double): Double = (value * 100.0).roundToInt() / 100.0
    private fun roundToOneDecimal(value: Float): Float = (value * 10f).roundToInt() / 10f
}
