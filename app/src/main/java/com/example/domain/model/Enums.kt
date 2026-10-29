package com.example.domain.model

import androidx.compose.runtime.Immutable

@Immutable
enum class FuelType(val displayName: String, val subtitle: String) {
    PETROL("Petrol / Gasoline", "RON 91 - 98"),
    DIESEL("Diesel", "Ultra-Low Sulfur"),
    CNG("CNG", "Compressed Natural Gas"),
    LPG("LPG / Autogas", "Liquefied Petroleum Gas"),
    HYBRID("Hybrid (HEV/PHEV)", "Dual Powertrain"),
    EV("Electric (EV)", "Battery Electric Vehicle")
}

@Immutable
enum class UnitPreference(val displayName: String, val unitTag: String) {
    KM_PER_L("Kilometers per Liter", "km/L"),
    L_PER_100KM("Liters per 100 Kilometers", "L/100km"),
    MPG_US("Miles per Gallon (US)", "MPG US"),
    MPG_UK("Miles per Gallon (Imperial)", "MPG UK")
}

@Immutable
enum class CurrencyPreference(val symbol: String, val code: String) {
    USD("$", "USD"),
    EUR("€", "EUR"),
    GBP("£", "GBP"),
    CAD("CA$", "CAD"),
    AUD("AU$", "AUD"),
    INR("₹", "INR"),
    JPY("¥", "JPY")
}

@Immutable
enum class FillType(val displayName: String, val description: String) {
    FULL("Full Tank", "Filled to automatic nozzle shutoff; establishes exact efficiency benchmark"),
    PARTIAL("Partial Tank", "Added fuel without topping off; queued until next full tank"),
    MISSED_PREVIOUS("Missed Fill-Up", "One or more fill-ups were skipped; resets calculation baseline")
}

