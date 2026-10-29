package com.example.domain.engine

import com.example.domain.model.CalculatedFuelEntry
import com.example.domain.model.CurrencyPreference
import com.example.domain.model.DashboardMetrics
import com.example.domain.model.FillType
import com.example.domain.model.FuelEntry
import com.example.domain.model.UnitPreference
import kotlin.math.roundToInt

object FuelCalculationEngine {

    const val KM_TO_MILES = 0.621371192
    const val LITERS_TO_GALLONS_US = 0.264172052
    const val LITERS_TO_GALLONS_UK = 0.219969248

    /**
     * Converts a raw efficiency (Distance in km, Volume in Liters) to target UnitPreference.
     */
    fun formatEfficiency(
        distanceKm: Double,
        volumeLiters: Double,
        unitPreference: UnitPreference
    ): Double? {
        if (distanceKm <= 0.0 || volumeLiters <= 0.0) return null
        return when (unitPreference) {
            UnitPreference.KM_PER_L -> {
                roundToTwoDecimals(distanceKm / volumeLiters)
            }
            UnitPreference.L_PER_100KM -> {
                roundToTwoDecimals((volumeLiters / distanceKm) * 100.0)
            }
            UnitPreference.MPG_US -> {
                val miles = distanceKm * KM_TO_MILES
                val gallonsUs = volumeLiters * LITERS_TO_GALLONS_US
                roundToTwoDecimals(miles / gallonsUs)
            }
            UnitPreference.MPG_UK -> {
                val miles = distanceKm * KM_TO_MILES
                val gallonsUk = volumeLiters * LITERS_TO_GALLONS_UK
                roundToTwoDecimals(miles / gallonsUk)
            }
        }
    }

    /**
     * Processes a chronological list of FuelEntries into CalculatedFuelEntries.
     * Accurately handles:
     * - Partial fill queues (accumulates volume until the next full tank)
     * - Missed fill-ups (breaks the chain, excludes segment from average, resets baseline)
     * - Odometer progression
     */
    fun processEntries(
        rawEntries: List<FuelEntry>,
        unitPreference: UnitPreference
    ): List<CalculatedFuelEntry> {
        if (rawEntries.isEmpty()) return emptyList()

        // Ensure chronological ascending order for calculation
        val sortedAscending = rawEntries.sortedWith(
            compareBy<FuelEntry> { it.odometerKm }.thenBy { it.timestamp }
        )

        val results = mutableListOf<CalculatedFuelEntry>()
        var lastFullAnchor: FuelEntry? = null
        val partialQueue = mutableListOf<FuelEntry>()

        for (entry in sortedAscending) {
            when (entry.fillType) {
                FillType.MISSED_PREVIOUS -> {
                    // Circuit breaker: Missed previous fill-up.
                    // Cannot resolve distance/burn accurately for this gap.
                    partialQueue.clear()
                    lastFullAnchor = entry // Establish new baseline

                    results.add(
                        CalculatedFuelEntry(
                            entry = entry,
                            deltaDistanceKm = null,
                            calculatedEfficiency = null,
                            costPerDistance = null,
                            isChainAnchor = false,
                            isPendingPartial = false,
                            isMissedBoundary = true
                        )
                    )
                }

                FillType.PARTIAL -> {
                    // Accumulate partial fill-up in queue
                    partialQueue.add(entry)
                    val cumulativeQueued = partialQueue.sumOf { it.fuelQuantityLiters }

                    results.add(
                        CalculatedFuelEntry(
                            entry = entry,
                            deltaDistanceKm = null,
                            calculatedEfficiency = null,
                            costPerDistance = null,
                            isChainAnchor = false,
                            isPendingPartial = true,
                            isMissedBoundary = false,
                            cumulativeVolumeQueued = roundToTwoDecimals(cumulativeQueued)
                        )
                    )
                }

                FillType.FULL -> {
                    if (lastFullAnchor != null) {
                        val deltaKm = entry.odometerKm - lastFullAnchor.odometerKm
                        val queuedLiters = partialQueue.sumOf { it.fuelQuantityLiters }
                        val totalLiters = entry.fuelQuantityLiters + queuedLiters
                        val queuedCost = partialQueue.sumOf { it.totalCost }
                        val totalCost = entry.totalCost + queuedCost

                        if (deltaKm > 0.0 && totalLiters > 0.0) {
                            val efficiency = formatEfficiency(deltaKm, totalLiters, unitPreference)
                            val distanceForCost = if (unitPreference == UnitPreference.MPG_US || unitPreference == UnitPreference.MPG_UK) {
                                deltaKm * KM_TO_MILES
                            } else {
                                deltaKm
                            }
                            val costPerDist = if (distanceForCost > 0.0) {
                                roundToTwoDecimals(totalCost / distanceForCost)
                            } else null

                            results.add(
                                CalculatedFuelEntry(
                                    entry = entry,
                                    deltaDistanceKm = roundToTwoDecimals(deltaKm),
                                    calculatedEfficiency = efficiency,
                                    costPerDistance = costPerDist,
                                    isChainAnchor = true,
                                    isPendingPartial = false,
                                    isMissedBoundary = false
                                )
                            )
                        } else {
                            // Non-positive delta or zero liters
                            results.add(
                                CalculatedFuelEntry(
                                    entry = entry,
                                    deltaDistanceKm = null,
                                    calculatedEfficiency = null,
                                    costPerDistance = null,
                                    isChainAnchor = true,
                                    isPendingPartial = false,
                                    isMissedBoundary = false
                                )
                            )
                        }

                        // Reset queue and update full anchor
                        partialQueue.clear()
                        lastFullAnchor = entry
                    } else {
                        // Very first entry in database establishes the baseline anchor
                        lastFullAnchor = entry
                        partialQueue.clear()
                        results.add(
                            CalculatedFuelEntry(
                                entry = entry,
                                deltaDistanceKm = null,
                                calculatedEfficiency = null,
                                costPerDistance = null,
                                isChainAnchor = true,
                                isPendingPartial = false,
                                isMissedBoundary = false
                            )
                        )
                    }
                }
            }
        }

        // Return descending (newest first) for UI feeds
        return results.reversed()
    }

    /**
     * Computes high-level dashboard summaries from the calculated entries.
     */
    fun computeDashboardMetrics(
        calculatedEntriesDesc: List<CalculatedFuelEntry>,
        unitPreference: UnitPreference,
        currencyPreference: CurrencyPreference,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): DashboardMetrics {
        if (calculatedEntriesDesc.isEmpty()) {
            return DashboardMetrics(
                latestEfficiency = null,
                averageEfficiency = null,
                rolling30DayExpenditure = 0.0,
                totalDistanceDrivenKm = 0.0,
                totalFuelConsumedLiters = 0.0,
                costPerDistanceAverage = null,
                unitPreference = unitPreference,
                currencyPreference = currencyPreference,
                totalFillCount = 0,
                lastOdometerKm = 0.0
            )
        }

        val latestWithEfficiency = calculatedEntriesDesc.firstOrNull { it.calculatedEfficiency != null }
        val latestEfficiency = latestWithEfficiency?.calculatedEfficiency

        // Aggregate resolved chains for average efficiency
        val resolvedChains = calculatedEntriesDesc.filter { it.isChainAnchor && it.deltaDistanceKm != null }
        val totalDeltaKm = resolvedChains.sumOf { it.deltaDistanceKm ?: 0.0 }
        val totalFuelConsumed = calculatedEntriesDesc.sumOf { it.entry.fuelQuantityLiters }
        val totalCost = calculatedEntriesDesc.sumOf { it.entry.totalCost }

        val averageEfficiency = if (totalDeltaKm > 0.0 && totalFuelConsumed > 0.0) {
            formatEfficiency(totalDeltaKm, totalFuelConsumed, unitPreference)
        } else {
            latestEfficiency
        }

        // 30-Day Rolling Expenditure
        val thirtyDaysAgo = currentTimeMillis - (30L * 24L * 60L * 60L * 1000L)
        val rolling30DayExpenditure = calculatedEntriesDesc
            .filter { it.entry.timestamp >= thirtyDaysAgo }
            .sumOf { it.entry.totalCost }

        val distanceForCost = if (unitPreference == UnitPreference.MPG_US || unitPreference == UnitPreference.MPG_UK) {
            totalDeltaKm * KM_TO_MILES
        } else {
            totalDeltaKm
        }

        val costPerDistanceAverage = if (distanceForCost > 0.0 && totalCost > 0.0) {
            roundToTwoDecimals(totalCost / distanceForCost)
        } else null

        val lastOdo = calculatedEntriesDesc.maxOfOrNull { it.entry.odometerKm } ?: 0.0

        return DashboardMetrics(
            latestEfficiency = latestEfficiency,
            averageEfficiency = averageEfficiency,
            rolling30DayExpenditure = roundToTwoDecimals(rolling30DayExpenditure),
            totalDistanceDrivenKm = roundToTwoDecimals(totalDeltaKm),
            totalFuelConsumedLiters = roundToTwoDecimals(totalFuelConsumed),
            costPerDistanceAverage = costPerDistanceAverage,
            unitPreference = unitPreference,
            currencyPreference = currencyPreference,
            totalFillCount = calculatedEntriesDesc.size,
            lastOdometerKm = lastOdo
        )
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }
}
