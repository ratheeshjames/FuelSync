package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.FillType
import com.example.domain.model.FuelType
import com.example.domain.model.Vehicle

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val make: String,
    val model: String,
    val year: Int,
    val tankCapacityLiters: Double,
    val fuelType: String,
    val licensePlate: String,
    val initialOdometerKm: Double,
    val isPrimary: Boolean,
    val vin: String
) {
    fun toDomain(): Vehicle {
        return Vehicle(
            id = id,
            name = name,
            make = make,
            model = model,
            year = year,
            tankCapacityLiters = tankCapacityLiters,
            fuelType = runCatching { FuelType.valueOf(fuelType) }.getOrDefault(FuelType.PETROL),
            licensePlate = licensePlate,
            initialOdometerKm = initialOdometerKm,
            isPrimary = isPrimary,
            vin = vin
        )
    }

    companion object {
        fun fromDomain(vehicle: Vehicle): VehicleEntity {
            return VehicleEntity(
                id = vehicle.id,
                name = vehicle.name,
                make = vehicle.make,
                model = vehicle.model,
                year = vehicle.year,
                tankCapacityLiters = vehicle.tankCapacityLiters,
                fuelType = vehicle.fuelType.name,
                licensePlate = vehicle.licensePlate,
                initialOdometerKm = vehicle.initialOdometerKm,
                isPrimary = vehicle.isPrimary,
                vin = vehicle.vin
            )
        }
    }
}

@Entity(tableName = "fuel_entries")
data class FuelEntryEntity(
    @PrimaryKey
    val id: String,
    val vehicleId: String,
    val timestamp: Long,
    val odometerKm: Double,
    val tripDistanceKm: Double?,
    val fuelQuantityLiters: Double,
    val unitPrice: Double,
    val totalCost: Double,
    val fillType: String,
    val fuelType: String,
    val stationName: String?,
    val notes: String?,
    val drivingEnvironment: String? = null,
    val isAirConditioningOn: Boolean = false
) {
    fun toDomain(): com.example.domain.model.FuelEntry {
        return com.example.domain.model.FuelEntry(
            id = id,
            vehicleId = vehicleId,
            timestamp = timestamp,
            odometerKm = odometerKm,
            tripDistanceKm = tripDistanceKm,
            fuelQuantityLiters = fuelQuantityLiters,
            unitPrice = unitPrice,
            totalCost = totalCost,
            fillType = runCatching { FillType.valueOf(fillType) }.getOrDefault(FillType.FULL),
            fuelType = runCatching { FuelType.valueOf(fuelType) }.getOrDefault(FuelType.PETROL),
            stationName = stationName,
            notes = notes,
            drivingEnvironment = drivingEnvironment,
            isAirConditioningOn = isAirConditioningOn
        )
    }

    companion object {
        fun fromDomain(entry: com.example.domain.model.FuelEntry): FuelEntryEntity {
            return FuelEntryEntity(
                id = entry.id,
                vehicleId = entry.vehicleId,
                timestamp = entry.timestamp,
                odometerKm = entry.odometerKm,
                tripDistanceKm = entry.tripDistanceKm,
                fuelQuantityLiters = entry.fuelQuantityLiters,
                unitPrice = entry.unitPrice,
                totalCost = entry.totalCost,
                fillType = entry.fillType.name,
                fuelType = entry.fuelType.name,
                stationName = entry.stationName,
                notes = entry.notes,
                drivingEnvironment = entry.drivingEnvironment,
                isAirConditioningOn = entry.isAirConditioningOn
            )
        }
    }
}
