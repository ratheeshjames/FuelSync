package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.FuelEntryEntity
import com.example.data.local.entity.VehicleEntity
import com.example.data.seed.SeedDataGenerator
import com.example.domain.engine.FuelCalculationEngine
import com.example.domain.model.CalculatedFuelEntry
import com.example.domain.model.CurrencyPreference
import com.example.domain.model.DashboardMetrics
import com.example.domain.model.FuelEntry
import com.example.domain.model.UnitPreference
import com.example.domain.model.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FuelRepository(
    private val database: AppDatabase
) {
    private val vehicleDao = database.vehicleDao()
    private val fuelEntryDao = database.fuelEntryDao()

    init {
        // Automatically seed database on first launch if empty
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
        }
    }

    suspend fun seedDatabaseIfEmpty() {
        val count = fuelEntryDao.getEntryCount()
        if (count == 0) {
            val vehicles = SeedDataGenerator.defaultVehicles
            vehicleDao.insertVehicles(vehicles.map { VehicleEntity.fromDomain(it) })

            val seedLogs = SeedDataGenerator.generateTwelveMonthSeedLogs(
                vehicleId = vehicles.first().id
            )
            fuelEntryDao.insertEntries(seedLogs.map { FuelEntryEntity.fromDomain(it) })
        }
    }

    fun getAllEntries(): Flow<List<FuelEntry>> {
        return fuelEntryDao.getAllEntries().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    fun getAllVehicles(): Flow<List<Vehicle>> {
        return vehicleDao.getAllVehicles().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    fun getPrimaryVehicle(): Flow<Vehicle?> {
        return vehicleDao.getPrimaryVehicle().map { it?.toDomain() }.flowOn(Dispatchers.IO)
    }

    suspend fun getVehicleById(id: String): Vehicle? {
        return withContext(Dispatchers.IO) {
            vehicleDao.getVehicleById(id)?.toDomain()
        }
    }

    suspend fun insertVehicle(vehicle: Vehicle) {
        withContext(Dispatchers.IO) {
            vehicleDao.insertVehicle(VehicleEntity.fromDomain(vehicle))
        }
    }

    fun getCalculatedEntries(
        vehicleId: String,
        unitPreference: UnitPreference
    ): Flow<List<CalculatedFuelEntry>> {
        return fuelEntryDao.getEntriesForVehicle(vehicleId).map { entities ->
            val raw = entities.map { it.toDomain() }
            FuelCalculationEngine.processEntries(raw, unitPreference)
        }.flowOn(Dispatchers.IO)
    }

    fun getDashboardMetrics(
        vehicleId: String,
        unitPreference: UnitPreference,
        currencyPreference: CurrencyPreference
    ): Flow<DashboardMetrics> {
        return getCalculatedEntries(vehicleId, unitPreference).map { calculatedEntries ->
            FuelCalculationEngine.computeDashboardMetrics(
                calculatedEntriesDesc = calculatedEntries,
                unitPreference = unitPreference,
                currencyPreference = currencyPreference
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getLatestOdometer(vehicleId: String): Double {
        return withContext(Dispatchers.IO) {
            val entries = fuelEntryDao.getEntriesForVehicle(vehicleId).firstOrNull() ?: emptyList()
            entries.maxOfOrNull { it.odometerKm } ?: 0.0
        }
    }

    suspend fun insertFuelEntry(entry: FuelEntry) {
        withContext(Dispatchers.IO) {
            fuelEntryDao.insertEntry(FuelEntryEntity.fromDomain(entry))
        }
    }

    suspend fun updateFuelEntry(entry: FuelEntry) {
        withContext(Dispatchers.IO) {
            fuelEntryDao.updateEntry(FuelEntryEntity.fromDomain(entry))
        }
    }

    suspend fun duplicateFuelEntry(id: String): FuelEntry? {
        return withContext(Dispatchers.IO) {
            val originalEntity = fuelEntryDao.getEntryById(id) ?: return@withContext null
            val original = originalEntity.toDomain()
            val latestOdo = getLatestOdometer(original.vehicleId)
            val newOdo = if (latestOdo > original.odometerKm) latestOdo + 480.0 else original.odometerKm + 480.0

            val duplicated = original.copy(
                id = java.util.UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                odometerKm = newOdo,
                tripDistanceKm = 480.0,
                notes = if (original.notes != null) "Duplicated: ${original.notes}" else "Duplicated entry"
            )
            fuelEntryDao.insertEntry(FuelEntryEntity.fromDomain(duplicated))
            duplicated
        }
    }

    suspend fun bulkDeleteEntries(ids: List<String>) {
        withContext(Dispatchers.IO) {
            fuelEntryDao.deleteEntriesByIds(ids)
        }
    }

    suspend fun bulkChangeVehicle(ids: List<String>, targetVehicleId: String) {
        withContext(Dispatchers.IO) {
            fuelEntryDao.updateVehicleForEntries(ids, targetVehicleId)
        }
    }

    suspend fun getAverageUnitPrice(vehicleId: String): Double {
        return withContext(Dispatchers.IO) {
            val entries = fuelEntryDao.getEntriesForVehicle(vehicleId).firstOrNull() ?: emptyList()
            if (entries.isEmpty()) 1.62 else entries.map { it.unitPrice }.average()
        }
    }

    suspend fun deleteFuelEntry(id: String) {
        withContext(Dispatchers.IO) {
            fuelEntryDao.deleteEntryById(id)
        }
    }
}
