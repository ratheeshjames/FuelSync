# FuelSync (MileLog) 🚗⚡

[![Android CI/CD](https://github.com/fuel-mileage/fuelsync/actions/workflows/build_and_release.yml/badge.svg)](https://github.com/fuel-mileage/fuelsync/actions/workflows/build_and_release.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-M3E-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![MinSdk](https://img.shields.io/badge/Min%20SDK-24-green.svg)](https://developer.android.com/about/dashboards)
[![TargetSdk](https://img.shields.io/badge/Target%20SDK-36-teal.svg)](https://developer.android.com/about/versions/16)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](LICENSE)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline%20First-success.svg)](RELEASE_NOTES.md)

**FuelSync (MileLog)** is a flagship, privacy-first, offline-first Android vehicle telemetry and fuel economy tracker. Built from the ground up using **Kotlin**, **Jetpack Compose**, and **Material 3 Expressive (M3E)** design tokens, FuelSync delivers high-precision fuel calculations, comprehensive multi-vehicle garage management, and pure Compose Canvas interactive analytics.

---

## 🌟 Key Highlights

- **🔒 100% Offline-First & Zero Trackers:** Your driving habits, receipts, and odometer data never leave your device. Backed by encrypted local Room SQLite with fully tested schema migrations.
- **📐 Mathematical Rigor:** Exact algorithms for full-to-full tank calculations, partial fill accumulations, odometer calibration, and missed fill-up gap reconciliation.
- **🏎️ Multi-Vehicle Garage:** Manage multiple cars, motorcycles, hybrids, and EVs with custom tank capacities, fuel grades, and odometer offsets.
- **📊 Pure Compose Canvas Analytics:** 120 FPS hardware-accelerated Bezier mileage trendlines, monthly expenditure comparisons, and interactive touch-scrubber donut charts for driving conditions.
- **🎨 Material 3 Expressive Design:** Adaptive navigation (Navigation Bar on mobile, Navigation Rail on foldables/tablets), fluid spring physics, and dynamic color theming.

---

## 🏗️ Architecture Blueprint

FuelSync follows strict **Clean Architecture** and **Unidirectional Data Flow (MVI / reactive StateFlow)** patterns.

```
┌─────────────────────────────────────────────────────────────┐
│                       Presentation Layer                    │
│   Jetpack Compose UI  •  M3E Tokens  •  Adaptive Navigation │
└──────────────────────────────┬──────────────────────────────┘
                               │ Observes UiState / Emits Intent
┌──────────────────────────────▼──────────────────────────────┐
│                        ViewModel Layer                      │
│   StateFlow Engine  •  Derived Analytics  •  Validation     │
└──────────────────────────────┬──────────────────────────────┘
                               │ Invokes Use Cases / Queries Repo
┌──────────────────────────────▼──────────────────────────────┐
│                         Domain Layer                        │
│   Entities  •  Fuel Math Calculations  •  Unit Converters    │
└──────────────────────────────┬──────────────────────────────┘
                               │ Reads / Persists
┌──────────────────────────────▼──────────────────────────────┐
│                          Data Layer                         │
│   Room Database  •  Entities & DAOs  •  DataStore Prefs     │
│   Automated SQLite Migrations (v1 ➜ v2)                     │
└─────────────────────────────────────────────────────────────┘
```

### Module Breakdown

| Module | Core Responsibility |
|---|---|
| **Module A: Log Engine** | Odometer tracking, trip meters, partial fills, fuel grades (Petrol, Diesel, CNG, LPG, EV). |
| **Module B: Garage Engine** | Vehicle profiles, tank thresholds, odometer offsets, and maintenance thresholds. |
| **Module C: Cost Engine** | Fuel price tracking, total cost per kilometer/mile, multi-currency support. |
| **Module D: Telemetry** | Driving environment tagging (City vs. Highway vs. Heavy Traffic, AC On/Off). |
| **Module E: M3E Shell** | Expressive corner shapes, adaptive navigation rail, bottom sheets, gesture controls. |
| **Module F: Analytics** | Canvas-drawn cubic Bezier curves, stacked cost bars, and interactive donut scrubbers. |
| **Module G: Data Engine** | Room database persistence, type converters, and migration verification. |

---

## 🧮 Precision Fuel Math Engine

Fuel consumption calculations strictly adhere to international engineering standards:

### 1. Full-to-Full Tank Delta
When two consecutive fill-ups are marked as **Full Tank**:
$$\text{Distance} = \text{Odometer}_{\text{current}} - \text{Odometer}_{\text{previous}}$$
$$\text{Mileage (km/L)} = \frac{\text{Distance}}{\text{Fuel Volume (L)}}$$
$$\text{Consumption (L/100km)} = \frac{\text{Fuel Volume (L)} \times 100}{\text{Distance}}$$

### 2. Partial Fill Accumulation
Partial fills are buffered without false mileage spikes until the next full tank event occurs:
$$\text{Total Fuel} = \sum \text{Fuel}_{\text{partial}} + \text{Fuel}_{\text{final\_full}}$$
$$\text{Mileage} = \frac{\text{Odometer}_{\text{final\_full}} - \text{Odometer}_{\text{initial\_full}}}{\text{Total Fuel}}$$

### 3. Metric & Imperial Conversions
- **1 km/L** $\approx 2.35215\text{ MPG (US)}$
- **1 km/L** $\approx 2.82481\text{ MPG (Imperial)}$
- **L/100km** $= \frac{100}{\text{km/L}}$

---

## 🛠️ Building & Development

### Prerequisites
- **Android Studio** Ladybug (2024.2.1+) or newer
- **JDK 17** or **JDK 21**
- **Android SDK Platform 36**

### Cloning & Building

```bash
# Clone the repository
git clone https://github.com/fuel-mileage/fuelsync.git
cd fuelsync

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Assemble Debug APK (includes .debug applicationId suffix)
gradle :app:assembleDebug

# Assemble Release APK & Android App Bundle (AAB)
gradle :app:assembleRelease :app:bundleRelease
```

### ProGuard & R8 Optimization
Release builds are minified, shrunk, and obfuscated using optimized R8 rules in `app/proguard-rules.pro`. Reflection targets, Room DAOs/Entities, Compose runtimes, and Moshi adapters are explicitly preserved to guarantee zero runtime crashes.

---

## 🧪 Testing & Quality Assurance

Fast, deterministic local testing using **Robolectric** ensures zero regression across schema evolutions and calculation pipelines:

```bash
# Execute local JVM tests
gradle :app:testDebugUnitTest --info
```

Verified Test Suites:
- `AppDatabaseRobolectricTest`: Verifies SQLite schema creation, initial state seeding, and automated migration from database version 1 to 2.
- Fuel math boundary tests for missed fill-ups, partial tank sequences, and odometer rolls.

---

## 🚀 CI/CD Pipeline

The project includes an enterprise-grade GitHub Actions workflow (`.github/workflows/build_and_release.yml`):

1. **Test & Verify:** Executes Robolectric and unit test suites with Gradle dependency caching.
2. **Build Debug APK:** Generates rapid QA installation packages.
3. **Sign & Bundle Release:** Decodes keystore secrets (`RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`) and builds signed Release APKs and `.aab` bundles ready for Google Play.

---

## 📄 License

Distributed under the **Apache License 2.0**. See `LICENSE` for more information.
