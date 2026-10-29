package com.example.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.CurrencyPreference
import com.example.ui.theme.FuelAccentTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.fuelSyncDataStore: DataStore<Preferences> by preferencesDataStore(name = "fuelsync_preferences")

enum class FuelUnitSystem(val label: String, val unitTag: String) {
    METRIC_L_PER_100KM("Liters / 100 km", "L/100km"),
    METRIC_KM_PER_L("Kilometers / Liter", "km/L"),
    US_MPG("Miles / Gallon (US)", "MPG US"),
    UK_MPG("Miles / Gallon (Imperial)", "MPG UK")
}

interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

class StandardDispatcherProvider : DispatcherProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val main: CoroutineDispatcher = Dispatchers.Main
}

interface ClockProvider {
    fun currentTimeMillis(): Long
    fun currentEpochSeconds(): Long = currentTimeMillis() / 1000L
}

class SystemClockProvider : ClockProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}

interface PreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)

    val selectedVehicleId: Flow<String>
    suspend fun setSelectedVehicleId(vehicleId: String)

    val preferredUnitSystem: Flow<FuelUnitSystem>
    suspend fun setPreferredUnitSystem(unitSystem: FuelUnitSystem)

    val isOledNightModeEnabled: Flow<Boolean>
    suspend fun setOledNightMode(enabled: Boolean)

    val isDynamicColorEnabled: Flow<Boolean>
    suspend fun setDynamicColor(enabled: Boolean)

    val fuelAccentTheme: Flow<FuelAccentTheme>
    suspend fun setFuelAccentTheme(accent: FuelAccentTheme)

    val preferredCurrency: Flow<CurrencyPreference>
    suspend fun setPreferredCurrency(currency: CurrencyPreference)

    val odometerOffsetKm: Flow<Double>
    suspend fun setOdometerOffsetKm(offset: Double)

    val tireCorrectionPercent: Flow<Double>
    suspend fun setTireCorrectionPercent(percent: Double)

    val lastOilServiceOdoKm: Flow<Double>
    suspend fun setLastOilServiceOdoKm(odoKm: Double)

    val lastTireRotationOdoKm: Flow<Double>
    suspend fun setLastTireRotationOdoKm(odoKm: Double)

    val lastBrakeServiceOdoKm: Flow<Double>
    suspend fun setLastBrakeServiceOdoKm(odoKm: Double)
}

class DataStorePreferencesRepository(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("pref_onboarding_completed")
        val SELECTED_VEHICLE_ID = stringPreferencesKey("pref_selected_vehicle_id")
        val UNIT_SYSTEM = stringPreferencesKey("pref_unit_system")
        val OLED_NIGHT_MODE = booleanPreferencesKey("pref_oled_night_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("pref_dynamic_color")
        val ACCENT_THEME = stringPreferencesKey("pref_accent_theme")
        val CURRENCY = stringPreferencesKey("pref_currency")
        val ODOMETER_OFFSET = doublePreferencesKey("pref_odometer_offset")
        val TIRE_CORRECTION = doublePreferencesKey("pref_tire_correction")
        val LAST_OIL_SERVICE = doublePreferencesKey("pref_last_oil_service")
        val LAST_TIRE_ROTATION = doublePreferencesKey("pref_last_tire_rotation")
        val LAST_BRAKE_SERVICE = doublePreferencesKey("pref_last_brake_service")
    }

    override val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.ONBOARDING_COMPLETED] ?: false
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    override val selectedVehicleId: Flow<String> = dataStore.data.map { preferences ->
        preferences[Keys.SELECTED_VEHICLE_ID] ?: "default_car"
    }

    override suspend fun setSelectedVehicleId(vehicleId: String) {
        dataStore.edit { preferences ->
            preferences[Keys.SELECTED_VEHICLE_ID] = vehicleId
        }
    }

    override val preferredUnitSystem: Flow<FuelUnitSystem> = dataStore.data.map { preferences ->
        val savedName = preferences[Keys.UNIT_SYSTEM]
        savedName?.let { runCatching { FuelUnitSystem.valueOf(it) }.getOrNull() }
            ?: FuelUnitSystem.METRIC_L_PER_100KM
    }

    override suspend fun setPreferredUnitSystem(unitSystem: FuelUnitSystem) {
        dataStore.edit { preferences ->
            preferences[Keys.UNIT_SYSTEM] = unitSystem.name
        }
    }

    override val isOledNightModeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.OLED_NIGHT_MODE] ?: false
    }

    override suspend fun setOledNightMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.OLED_NIGHT_MODE] = enabled
        }
    }

    override val isDynamicColorEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.DYNAMIC_COLOR] ?: false
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.DYNAMIC_COLOR] = enabled
        }
    }

    override val fuelAccentTheme: Flow<FuelAccentTheme> = dataStore.data.map { preferences ->
        val name = preferences[Keys.ACCENT_THEME]
        name?.let { runCatching { FuelAccentTheme.valueOf(it) }.getOrNull() }
            ?: FuelAccentTheme.ELECTRIC_CYAN
    }

    override suspend fun setFuelAccentTheme(accent: FuelAccentTheme) {
        dataStore.edit { preferences ->
            preferences[Keys.ACCENT_THEME] = accent.name
        }
    }

    override val preferredCurrency: Flow<CurrencyPreference> = dataStore.data.map { preferences ->
        val name = preferences[Keys.CURRENCY]
        name?.let { runCatching { CurrencyPreference.valueOf(it) }.getOrNull() }
            ?: CurrencyPreference.USD
    }

    override suspend fun setPreferredCurrency(currency: CurrencyPreference) {
        dataStore.edit { preferences ->
            preferences[Keys.CURRENCY] = currency.name
        }
    }

    override val odometerOffsetKm: Flow<Double> = dataStore.data.map { preferences ->
        preferences[Keys.ODOMETER_OFFSET] ?: 0.0
    }

    override suspend fun setOdometerOffsetKm(offset: Double) {
        dataStore.edit { preferences ->
            preferences[Keys.ODOMETER_OFFSET] = offset
        }
    }

    override val tireCorrectionPercent: Flow<Double> = dataStore.data.map { preferences ->
        preferences[Keys.TIRE_CORRECTION] ?: 0.0
    }

    override suspend fun setTireCorrectionPercent(percent: Double) {
        dataStore.edit { preferences ->
            preferences[Keys.TIRE_CORRECTION] = percent
        }
    }

    override val lastOilServiceOdoKm: Flow<Double> = dataStore.data.map { preferences ->
        preferences[Keys.LAST_OIL_SERVICE] ?: 28000.0
    }

    override suspend fun setLastOilServiceOdoKm(odoKm: Double) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_OIL_SERVICE] = odoKm
        }
    }

    override val lastTireRotationOdoKm: Flow<Double> = dataStore.data.map { preferences ->
        preferences[Keys.LAST_TIRE_ROTATION] ?: 25000.0
    }

    override suspend fun setLastTireRotationOdoKm(odoKm: Double) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_TIRE_ROTATION] = odoKm
        }
    }

    override val lastBrakeServiceOdoKm: Flow<Double> = dataStore.data.map { preferences ->
        preferences[Keys.LAST_BRAKE_SERVICE] ?: 15000.0
    }

    override suspend fun setLastBrakeServiceOdoKm(odoKm: Double) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_BRAKE_SERVICE] = odoKm
        }
    }
}

/**
 * Centralized Application Environment facilitating Clean Architecture dependency injection
 */
class AppEnvironment private constructor(
    val dispatchers: DispatcherProvider,
    val clock: ClockProvider,
    val preferences: PreferencesRepository,
    val database: com.example.data.local.AppDatabase,
    val repository: com.example.data.repository.FuelRepository
) {
    companion object {
        @Volatile
        private var instance: AppEnvironment? = null

        fun initialize(context: Context): AppEnvironment {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val db = com.example.data.local.AppDatabase.getInstance(context)
                    val repo = com.example.data.repository.FuelRepository(db)
                    AppEnvironment(
                        dispatchers = StandardDispatcherProvider(),
                        clock = SystemClockProvider(),
                        preferences = DataStorePreferencesRepository(context.applicationContext.fuelSyncDataStore),
                        database = db,
                        repository = repo
                    ).also { instance = it }
                }
            }
        }

        fun get(): AppEnvironment {
            return checkNotNull(instance) { "AppEnvironment must be initialized in Application or MainActivity!" }
        }
    }
}
