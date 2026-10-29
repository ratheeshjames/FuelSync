package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FuelEntryEntity
import com.example.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY isPrimary DESC, name ASC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getVehicleById(id: String): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE isPrimary = 1 LIMIT 1")
    fun getPrimaryVehicle(): Flow<VehicleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<VehicleEntity>)

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)
}

@Dao
interface FuelEntryDao {
    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY odometerKm DESC, timestamp DESC")
    fun getEntriesForVehicle(vehicleId: String): Flow<List<FuelEntryEntity>>

    @Query("SELECT * FROM fuel_entries ORDER BY odometerKm DESC, timestamp DESC")
    fun getAllEntries(): Flow<List<FuelEntryEntity>>

    @Query("SELECT * FROM fuel_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: String): FuelEntryEntity?

    @Query("SELECT COUNT(*) FROM fuel_entries")
    suspend fun getEntryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FuelEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<FuelEntryEntity>)

    @Update
    suspend fun updateEntry(entry: FuelEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: FuelEntryEntity)

    @Query("DELETE FROM fuel_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query("DELETE FROM fuel_entries WHERE id IN (:ids)")
    suspend fun deleteEntriesByIds(ids: List<String>)

    @Query("UPDATE fuel_entries SET vehicleId = :targetVehicleId WHERE id IN (:ids)")
    suspend fun updateVehicleForEntries(ids: List<String>, targetVehicleId: String)
}
