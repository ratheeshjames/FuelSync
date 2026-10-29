# FuelSync (MileLog) — Release Notes

## Version: `v1.0.0-PROD` (Build 1)
**Release Date:** October 2026  
**Target Platform:** Android 7.0+ (API 24 to 36)  
**Distribution:** Android App Bundle (`.aab`) & Release APK (`.apk`)

---

### 🚀 Overview & Executive Summary

We are proud to announce the initial production release of **FuelSync (MileLog)** — a flagship, privacy-centric fuel consumption, mileage, and vehicle telemetry tracking application. Built entirely with modern Jetpack Compose, Material 3 Expressive (M3E) components, and an offline-first Room SQLite persistence engine, FuelSync combines laboratory-grade mathematical rigor with fluid 120 FPS responsive interactions.

---

### 📦 Shipped Modules & Capabilities

#### Module A: High-Precision Data Capture & Fuel Log Engine
- **Odometer vs. Trip Meter:** Dual entry modes supporting cumulative odometer tracking or trip odometer resets.
- **Full vs. Partial Fill Detection:** Intelligently tags entries as full tank or partial fill, buffering unmeasured volume until a subsequent full-tank boundary to guarantee mathematically sound fuel economy.
- **Missed Fill Boundary Handling:** Allows users to flag missed fill-ups so historical averages are not contaminated by unlogged consumption.
- **Multiple Fuel Grades:** Native support for Regular 87/91/95/98 Octane, Premium Unleaded, Diesel, Biodiesel, LPG, CNG, and EV kWh equivalents.

#### Module B: Multi-Vehicle Garage Management
- **Vehicle Profiles:** Detailed records for vehicle make, model, year, license plate, VIN, and primary fuel type.
- **Tank Capacity & Warning Thresholds:** Configurable usable fuel capacity and low-fuel range warnings.
- **Odometer Offsets & Calibration:** Accommodates tire-size changes or dashboard cluster swaps with automatic linear offset calibration.
- **Instant Switcher:** Rapid bottom sheet for seamless switching between garage vehicles.

#### Module C: Expense, Cost Tracking & Multi-Currency Engine
- **Dynamic Fuel Pricing:** Computes running price-per-unit (e.g., $/L, €/gal) and total transaction expenditures.
- **Cost-Per-Distance Metric:** Real-time computation of running vehicle cost per kilometer and per mile.
- **Receipt & Notes Attachment:** Store notes, fuel station brands, and transaction tags with each fill-up.

#### Module D: Driving Conditions & Telemetry Attribution
- **Environment Split:** Categorize trips by driving environment (Highway, City, Heavy Traffic, Mixed).
- **Auxiliary Loads:** Toggle Air Conditioning (AC On / Off) and aggressive driving flags to isolate factors causing economy dips.

#### Module E: Material 3 Expressive (M3E) Shell & Adaptive Navigation
- **Adaptive Navigation Suite:** Automatically adjusts between Bottom Navigation on compact handheld phones, Navigation Rail on foldables and tablets, and Navigation Drawer on desktop/expanded viewports.
- **M3E Motion & Shape Tokens:** Expressive spring animations, curved bottom sheets, and high-contrast color styling.
- **Strict Layout Stability:** Eliminates word-wrap breaks, button squishing, and uneven card heights with intrinsic measurements.

#### Module F: Pure Compose Canvas Analytics & Data Visualizations
- **Cubic Bezier Mileage Trendline:** Smooth, anti-aliased Canvas charts visualizing fuel economy progression over time with rolling moving averages.
- **Grouped Cost Breakdown:** Bar charts comparing total money spent against distance driven month-over-month.
- **Interactive Scrubber Donut Chart:** Interactive slice inspection displaying proportional driving conditions (Highway vs. City vs. Traffic).

#### Module G: Resilient Offline-First Data Architecture
- **Room SQLite Persistence:** Zero network dependency. Instant boot and zero latency for all operations.
- **Automated Schema Migrations:** Production-tested migration path from Database Version 1 to 2 (`MIGRATION_1_2`) with verified foreign key integrity and table reconstructions.

---

### 🧮 Mathematical Engine Specifications

1. **Consecutive Full Tank Delta:**
   ```
   Distance = Odometer[N] - Odometer[N-1]
   Mileage (km/L) = Distance / FuelAmount[N]
   L/100km = 100 / (Distance / FuelAmount[N])
   ```

2. **Partial Fill Sequence Aggregation:**
   ```
   AccumulatedVolume = Sum(FuelAmount[i] for i from LastFull+1 to CurrentFull)
   Distance = Odometer[CurrentFull] - Odometer[LastFull]
   Mileage = Distance / AccumulatedVolume
   ```

3. **Units of Measurement Supported:**
   - Distance: Kilometers (km), Miles (mi)
   - Fuel Volume: Liters (L), Gallons (US gal), Gallons (UK gal)
   - Economy: km/L, L/100km, MPG (US), MPG (Imperial)
   - Currencies: USD ($), EUR (€), GBP (£), INR (₹), JPY (¥), AUD ($), CAD ($), etc.

---

### 🔐 Privacy & Security Architecture

- **No Remote Telemetry or Ad Trackers:** Zero third-party tracker SDKs included.
- **Isolated Sandbox:** App uses Android private internal storage (`/data/data/com.example/databases/`).
- **Google Play Compliance:** No broad storage permissions required; follows zero-permission modern Android storage and lifecycle guidelines.

---

### 🛠️ Build Artifacts & Checksums

- **Debug APK:** `app-debug.apk` (Signed with local debug keystore, `applicationIdSuffix = ".debug"`)
- **Release APK:** `app-release.apk` (Minified, R8 shrunk, optimized)
- **Release Bundle:** `app-release.aab` (Google Play App Bundle with dynamic feature slicing and density resource split)
