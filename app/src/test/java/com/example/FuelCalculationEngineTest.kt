package com.example

import com.example.domain.engine.FuelCalculationEngine
import com.example.domain.model.CurrencyPreference
import com.example.domain.model.FillType
import com.example.domain.model.FuelEntry
import com.example.domain.model.FuelType
import com.example.domain.model.UnitPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FuelCalculationEngineTest {

    @Test
    fun testFullToFullCalculation() {
        val entry1 = FuelEntry(
            id = "1",
            vehicleId = "car_1",
            timestamp = 1000L,
            odometerKm = 10000.0,
            fuelQuantityLiters = 50.0,
            unitPrice = 1.50,
            totalCost = 75.0,
            fillType = FillType.FULL,
            fuelType = FuelType.PETROL
        )
        val entry2 = FuelEntry(
            id = "2",
            vehicleId = "car_1",
            timestamp = 2000L,
            odometerKm = 10500.0,
            fuelQuantityLiters = 40.0,
            unitPrice = 1.50,
            totalCost = 60.0,
            fillType = FillType.FULL,
            fuelType = FuelType.PETROL
        )

        val processed = FuelCalculationEngine.processEntries(
            listOf(entry1, entry2),
            UnitPreference.L_PER_100KM
        )

        // Processed is sorted newest first
        assertEquals(2, processed.size)
        val latest = processed[0]
        assertEquals(500.0, latest.deltaDistanceKm!!, 0.01)
        // (40.0 / 500.0) * 100 = 8.0 L/100km
        assertEquals(8.0, latest.calculatedEfficiency!!, 0.01)
        // Cost per km = $60 / 500km = 0.12 $/km
        assertEquals(0.12, latest.costPerDistance!!, 0.01)
    }

    @Test
    fun testPartialFillUpAccumulationQueue() {
        // Base full tank
        val entry1 = FuelEntry(
            id = "1", vehicleId = "car_1", timestamp = 1000L, odometerKm = 10000.0,
            fuelQuantityLiters = 50.0, unitPrice = 1.5, totalCost = 75.0,
            fillType = FillType.FULL, fuelType = FuelType.PETROL
        )
        // Partial fill 1 (20L)
        val entry2 = FuelEntry(
            id = "2", vehicleId = "car_1", timestamp = 2000L, odometerKm = 10300.0,
            fuelQuantityLiters = 20.0, unitPrice = 1.5, totalCost = 30.0,
            fillType = FillType.PARTIAL, fuelType = FuelType.PETROL
        )
        // Full tank (30L) resolving the partial queue!
        // Total distance = 10600 - 10000 = 600 km
        // Total fuel = 20L (partial) + 30L (full) = 50L
        val entry3 = FuelEntry(
            id = "3", vehicleId = "car_1", timestamp = 3000L, odometerKm = 10600.0,
            fuelQuantityLiters = 30.0, unitPrice = 1.5, totalCost = 45.0,
            fillType = FillType.FULL, fuelType = FuelType.PETROL
        )

        val processed = FuelCalculationEngine.processEntries(
            listOf(entry1, entry2, entry3),
            UnitPreference.KM_PER_L
        )

        assertEquals(3, processed.size)
        val resolvedFull = processed[0]
        val pendingPartial = processed[1]

        assertTrue(pendingPartial.isPendingPartial)
        assertNull(pendingPartial.calculatedEfficiency)

        assertTrue(resolvedFull.isChainAnchor)
        assertEquals(600.0, resolvedFull.deltaDistanceKm!!, 0.01)
        // 600km / 50L = 12.0 km/L
        assertEquals(12.0, resolvedFull.calculatedEfficiency!!, 0.01)
    }

    @Test
    fun testMissedFillUpCircuitBreaker() {
        val entry1 = FuelEntry(
            id = "1", vehicleId = "car_1", timestamp = 1000L, odometerKm = 10000.0,
            fuelQuantityLiters = 50.0, unitPrice = 1.5, totalCost = 75.0,
            fillType = FillType.FULL, fuelType = FuelType.PETROL
        )
        // Missed fill-up boundary (unrecorded gap)
        val entry2 = FuelEntry(
            id = "2", vehicleId = "car_1", timestamp = 2000L, odometerKm = 11200.0,
            fuelQuantityLiters = 45.0, unitPrice = 1.5, totalCost = 67.5,
            fillType = FillType.MISSED_PREVIOUS, fuelType = FuelType.PETROL
        )
        // Next clean fill
        val entry3 = FuelEntry(
            id = "3", vehicleId = "car_1", timestamp = 3000L, odometerKm = 11700.0,
            fuelQuantityLiters = 40.0, unitPrice = 1.5, totalCost = 60.0,
            fillType = FillType.FULL, fuelType = FuelType.PETROL
        )

        val processed = FuelCalculationEngine.processEntries(
            listOf(entry1, entry2, entry3),
            UnitPreference.L_PER_100KM
        )

        assertEquals(3, processed.size)
        val latest = processed[0]
        val missed = processed[1]

        // Missed boundary should NOT have an efficiency calculation
        assertTrue(missed.isMissedBoundary)
        assertNull(missed.calculatedEfficiency)

        // Latest should accurately calculate delta from entry2 to entry3 (500 km, 40L = 8.0 L/100km)
        assertEquals(500.0, latest.deltaDistanceKm!!, 0.01)
        assertEquals(8.0, latest.calculatedEfficiency!!, 0.01)
    }

    @Test
    fun testUnitConversions() {
        // 100 km, 10 Liters -> 10 km/L or 10 L/100km or 23.52 MPG US
        val kmPerL = FuelCalculationEngine.formatEfficiency(100.0, 10.0, UnitPreference.KM_PER_L)
        val lPer100 = FuelCalculationEngine.formatEfficiency(100.0, 10.0, UnitPreference.L_PER_100KM)
        val mpgUs = FuelCalculationEngine.formatEfficiency(100.0, 10.0, UnitPreference.MPG_US)
        val mpgUk = FuelCalculationEngine.formatEfficiency(100.0, 10.0, UnitPreference.MPG_UK)

        assertEquals(10.0, kmPerL!!, 0.05)
        assertEquals(10.0, lPer100!!, 0.05)
        assertEquals(23.52, mpgUs!!, 0.1)
        assertEquals(28.25, mpgUk!!, 0.1)
    }
}
