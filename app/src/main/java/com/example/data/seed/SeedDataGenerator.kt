package com.example.data.seed

import com.example.domain.model.FillType
import com.example.domain.model.FuelEntry
import com.example.domain.model.FuelType
import com.example.domain.model.Vehicle
import java.util.UUID

object SeedDataGenerator {

    val defaultVehicles = listOf(
        Vehicle(
            id = "car_mazda_cx5",
            name = "2024 Mazda CX-5 Turbo",
            make = "Mazda",
            model = "CX-5 Turbo AWD",
            year = 2024,
            tankCapacityLiters = 58.0,
            fuelType = FuelType.PETROL,
            licensePlate = "7XYZ892",
            initialOdometerKm = 12000.0,
            isPrimary = true,
            vin = "JM3KFBDM8R0194821"
        ),
        Vehicle(
            id = "car_rav4_hybrid",
            name = "2021 Toyota RAV4 Hybrid",
            make = "Toyota",
            model = "RAV4 Hybrid XSE",
            year = 2021,
            tankCapacityLiters = 55.0,
            fuelType = FuelType.HYBRID,
            licensePlate = "8ABC441",
            initialOdometerKm = 48500.0,
            isPrimary = false,
            vin = "2T3C1RFV5MC092143"
        )
    )

    /**
     * Generates 12 months of realistic fuel fill-ups with progressive odometers,
     * variable prices, partial fills queue resolution, and a missed fill-up circuit breaker.
     */
    fun generateTwelveMonthSeedLogs(
        vehicleId: String = "car_mazda_cx5",
        baseTimestampMillis: Long = System.currentTimeMillis()
    ): List<FuelEntry> {
        val stations = listOf(
            "Shell V-Power #104",
            "Costco Wholesale Fuel",
            "BP Connect Highway 1",
            "Mobil 1 Express",
            "Chevron with Techron"
        )

        // Seed logs generated chronologically backward from baseTimestampMillis
        // 18 records across 12 months (roughly every 18-20 days, ~550km per tank)
        data class SeedTemplate(
            val daysAgo: Int,
            val odoKm: Double,
            val tripKm: Double,
            val liters: Double,
            val pricePerLiter: Double,
            val fillType: FillType,
            val stationIndex: Int,
            val notes: String?
        )

        val templates = listOf(
            SeedTemplate(360, 12000.0, 0.0, 50.0, 1.48, FillType.FULL, 0, "Initial garage baseline fill"),
            SeedTemplate(342, 12560.0, 560.0, 44.2, 1.52, FillType.FULL, 1, "Commute to work"),
            SeedTemplate(324, 13140.0, 580.0, 46.1, 1.49, FillType.FULL, 2, "Highway weekend drive"),
            SeedTemplate(305, 13730.0, 590.0, 47.0, 1.54, FillType.FULL, 3, null),
            SeedTemplate(286, 14310.0, 580.0, 45.8, 1.58, FillType.FULL, 0, "Winter weather commute"),
            SeedTemplate(268, 14880.0, 570.0, 46.5, 1.62, FillType.FULL, 1, "Sub-zero cold start week"),
            // Partial Fill-up 1 (Quick top-up on road trip)
            SeedTemplate(252, 15190.0, 310.0, 22.0, 1.65, FillType.PARTIAL, 2, "Quick splash top-up on turnpike"),
            // Full Fill-up resolving Partial Fill-up 1
            SeedTemplate(240, 15540.0, 350.0, 28.5, 1.61, FillType.FULL, 4, "Full tank resolving previous partial"),
            SeedTemplate(220, 16120.0, 580.0, 45.2, 1.59, FillType.FULL, 0, null),
            SeedTemplate(200, 16700.0, 580.0, 44.9, 1.55, FillType.FULL, 1, "Spring tire swap"),
            // Missed fill-up scenario (Driver forgot to log on cross-country leg)
            SeedTemplate(175, 17850.0, 1150.0, 48.0, 1.57, FillType.MISSED_PREVIOUS, 3, "Forgot receipt in hotel; flagged gap"),
            SeedTemplate(155, 18420.0, 570.0, 45.0, 1.63, FillType.FULL, 2, "Clean chain baseline established"),
            SeedTemplate(135, 19010.0, 590.0, 46.8, 1.68, FillType.FULL, 4, "Summer road trip leg 1"),
            // Partial Fill-up 2
            SeedTemplate(120, 19280.0, 270.0, 19.5, 1.72, FillType.PARTIAL, 0, "Top-up before national park ascent"),
            // Full Fill-up resolving Partial Fill-up 2
            SeedTemplate(105, 19650.0, 370.0, 31.0, 1.70, FillType.FULL, 1, "Full fill-up after mountain descent"),
            SeedTemplate(85, 20240.0, 590.0, 46.2, 1.66, FillType.FULL, 3, null),
            SeedTemplate(65, 20830.0, 590.0, 45.7, 1.62, FillType.FULL, 0, null),
            SeedTemplate(45, 21410.0, 580.0, 45.1, 1.59, FillType.FULL, 2, null),
            SeedTemplate(25, 21990.0, 580.0, 44.8, 1.57, FillType.FULL, 1, "Routine city fill"),
            SeedTemplate(5, 22580.0, 590.0, 45.5, 1.55, FillType.FULL, 0, "Latest fill-up at Shell")
        )

        val oneDayMillis = 24L * 60L * 60L * 1000L

        return templates.map { template ->
            val timestamp = baseTimestampMillis - (template.daysAgo * oneDayMillis)
            val totalCost = (template.liters * template.pricePerLiter * 100.0).toInt() / 100.0
            FuelEntry(
                id = UUID.nameUUIDFromBytes("seed_${template.daysAgo}_${template.odoKm}".toByteArray()).toString(),
                vehicleId = vehicleId,
                timestamp = timestamp,
                odometerKm = template.odoKm,
                tripDistanceKm = template.tripKm,
                fuelQuantityLiters = template.liters,
                unitPrice = template.pricePerLiter,
                totalCost = totalCost,
                fillType = template.fillType,
                fuelType = FuelType.PETROL,
                stationName = stations[template.stationIndex],
                notes = template.notes
            )
        }
    }
}
