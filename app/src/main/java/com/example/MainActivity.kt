package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.core.di.AppEnvironment
import com.example.ui.scaffold.RootScaffold
import com.example.ui.theme.FuelSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Centralized App Environment (Dispatchers, Clock, DataStore Preferences)
        val appEnvironment = AppEnvironment.initialize(applicationContext)

        setContent {
            val isOledNightMode by appEnvironment.preferences.isOledNightModeEnabled
                .collectAsState(initial = false)
            val isDynamicColor by appEnvironment.preferences.isDynamicColorEnabled
                .collectAsState(initial = false)
            val accentTheme by appEnvironment.preferences.fuelAccentTheme
                .collectAsState(initial = com.example.ui.theme.FuelAccentTheme.ELECTRIC_CYAN)

            FuelSyncTheme(
                oledNightPumpMode = isOledNightMode,
                dynamicColor = isDynamicColor,
                accentTheme = accentTheme
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootScaffold(appEnvironment = appEnvironment)
                }
            }
        }
    }
}
