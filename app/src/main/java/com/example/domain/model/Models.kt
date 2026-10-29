package com.example.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Vehicle(
    val id: String,
    val name: String,
    val make: String,
    val model: String,
    val year: Int,
    val tankCapacityLiters: Double,
    val fuelType: FuelType,
    val licensePlate: String,
    val initialOdometerKm: Double,
    val isPrimary: Boolean,
    val vin: String = ""
)

@Immutable
data class FuelEntry(
    val id: String,
    val vehicleId: String,
    val timestamp: Long,
    val odometerKm: Double,
    val tripDistanceKm: Double? = null,
    val fuelQuantityLiters: Double,
    val unitPrice: Double,
    val totalCost: Double,
    val fillType: FillType,
    val fuelType: FuelType,
    val stationName: String? = null,
    val notes: String? = null,
    val drivingEnvironment: String? = null,
    val isAirConditioningOn: Boolean = false
)

@Immutable
enum class DateRangeFilter(val displayName: String) {
    ALL("All Time"),
    THIS_MONTH("This Month"),
    LAST_3_MONTHS("Last 3 Months"),
    YEAR_TO_DATE("Year to Date")
}

@Immutable
data class CalculatedFuelEntry(
    val entry: FuelEntry,
    val deltaDistanceKm: Double?,
    val calculatedEfficiency: Double?, // In active unit preference
    val costPerDistance: Double?,      // In active currency per km or mi
    val isChainAnchor: Boolean,        // Full fill that resolved a calculation chain
    val isPendingPartial: Boolean,     // Partial fill waiting for next full tank
    val isMissedBoundary: Boolean,     // Skipped entry boundary
    val cumulativeVolumeQueued: Double? = null
)

@Immutable
data class DashboardMetrics(
    val latestEfficiency: Double?,
    val averageEfficiency: Double?,
    val rolling30DayExpenditure: Double,
    val totalDistanceDrivenKm: Double,
    val totalFuelConsumedLiters: Double,
    val costPerDistanceAverage: Double?,
    val unitPreference: UnitPreference,
    val currencyPreference: CurrencyPreference,
    val totalFillCount: Int,
    val lastOdometerKm: Double
)

