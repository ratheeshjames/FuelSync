package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.FuelRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppDatabaseRobolectricTest {

    @Test
    fun databaseInitializesAndSeedsSuccessfully() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        assertNotNull("Database instance should not be null", db)

        val repository = FuelRepository(db)
        repository.seedDatabaseIfEmpty()

        val vehicles = repository.getAllVehicles().first()
        val entries = repository.getAllEntries().first()

        assertTrue("Default vehicles should be seeded", vehicles.isNotEmpty())
        assertTrue("Seed fuel logs should be generated", entries.isNotEmpty())
    }

    @Test
    fun migration1To2AddsNewColumnsSuccessfully() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("test_migration.db")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS `vehicles` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `make` TEXT NOT NULL, `model` TEXT NOT NULL, `year` INTEGER NOT NULL, `tankCapacityLiters` REAL NOT NULL, `fuelType` TEXT NOT NULL, `licensePlate` TEXT NOT NULL, `initialOdometerKm` REAL NOT NULL, `isPrimary` INTEGER NOT NULL, `vin` TEXT NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `fuel_entries` (`id` TEXT NOT NULL, `vehicleId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `odometerKm` REAL NOT NULL, `tripDistanceKm` REAL, `fuelQuantityLiters` REAL NOT NULL, `unitPrice` REAL NOT NULL, `totalCost` REAL NOT NULL, `fillType` TEXT NOT NULL, `fuelType` TEXT NOT NULL, `stationName` TEXT, `notes` TEXT, PRIMARY KEY(`id`))")
                    db.execSQL("INSERT INTO fuel_entries VALUES ('test-1', 'v1', 1700000000, 1000.0, 50.0, 40.0, 1.5, 60.0, 'FULL', 'PETROL', 'Shell', 'Initial entry')")
                }
                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory().create(config)
        val supportDb = helper.writableDatabase

        AppDatabase.MIGRATION_1_2.migrate(supportDb)

        val cursor = supportDb.query("SELECT drivingEnvironment, isAirConditioningOn FROM fuel_entries WHERE id = 'test-1'")
        assertTrue("Query should find record", cursor.moveToFirst())
        assertTrue("drivingEnvironment should be null", cursor.isNull(0))
        org.junit.Assert.assertEquals(0, cursor.getInt(1))
        cursor.close()
        supportDb.close()
    }
}
