package com.example.ui.preview

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.di.FuelUnitSystem
import com.example.domain.model.CurrencyPreference
import com.example.domain.model.DashboardMetrics
import com.example.domain.model.FillType
import com.example.domain.model.FuelEntry
import com.example.domain.model.FuelType
import com.example.domain.model.UnitPreference
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AnalyticsUiState
import com.example.ui.screens.ConditionSliceData
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DashboardUiState
import com.example.ui.screens.MileageTrendPoint
import com.example.ui.screens.MonthlyCostData
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SettingsUiState
import com.example.ui.theme.FuelAccentTheme
import com.example.ui.theme.FuelSyncTheme
import com.example.ui.theme.LocalExpressiveShapes

// ==========================================
// 1. EMPTY STATE PREVIEW
// ==========================================

@Composable
fun FuelSyncEmptyState(
    onLogFirstFill: () -> Unit = {}
) {
    val shapes = LocalExpressiveShapes.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalGasStation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to FuelSync",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Track high-precision fuel mileage, telemetry degradation curves, and multi-vehicle garage expenses with ease.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogFirstFill,
                shape = shapes.pill,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log First Fill-Up", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 2. LOADING SHIMMER STATE PREVIEW
// ==========================================

@Composable
fun FuelSyncLoadingShimmerState() {
    val shapes = LocalExpressiveShapes.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(28.dp)
                    .clip(shapes.pill)
                    .background(brush)
            )

            // Hero Metric Card Shimmer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = shapes.telemetryCard,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush)
                )
            }

            // Two secondary cards Shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp),
                    shape = shapes.telemetryCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(brush))
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp),
                    shape = shapes.telemetryCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(brush))
                }
            }

            // List items shimmer
            repeat(3) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = shapes.telemetryCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(brush))
                }
            }
        }
    }
}

// ==========================================
// 3. CALCULATION ERROR & ANOMALY PREVIEW
// ==========================================

@Composable
fun FuelSyncCalculationErrorState(
    onReconcile: () -> Unit = {}
) {
    val shapes = LocalExpressiveShapes.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Odometer Anomaly Detected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A recent fill-up entry contains an odometer reading (24,100 km) that is lower than the preceding record (24,850 km). This breaks continuous calculation chains.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.telemetryCard
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ANOMALY RESOLUTION GUIDANCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. If instrument cluster was swapped, configure Odometer Offset in Settings.\n2. If one entry was mistyped, edit the log in the Logs tab.\n3. If a fill-up was skipped, flag it as 'Missed Previous' to reset the calculation baseline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onReconcile,
                shape = shapes.pill,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Reconcile Database")
            }
        }
    }
}

// ==========================================
// 4. MOCK DATA FOR POPULATED STATE
// ==========================================

private val mockTrendPoints = listOf(
    MileageTrendPoint("1", 1704067200000L, "Jan 1", 7.8, 7.8, 24100.0, 68.50, "Shell Helix"),
    MileageTrendPoint("2", 1705276800000L, "Jan 15", 7.6, 7.7, 24650.0, 71.20, "Chevron Tech"),
    MileageTrendPoint("3", 1706745600000L, "Feb 1", 8.1, 7.83, 25200.0, 74.00, "BP Ultimate"),
    MileageTrendPoint("4", 1708300800000L, "Feb 18", 7.2, 7.67, 25800.0, 65.40, "Costco Fuel"),
    MileageTrendPoint("5", 1709856000000L, "Mar 8", 7.4, 7.62, 26350.0, 67.80, "Mobil Extra"),
    MileageTrendPoint("6", 1711411200000L, "Mar 26", 6.9, 7.50, 26950.0, 63.20, "Shell V-Power")
)

private val mockMonthlyData = listOf(
    MonthlyCostData("Oct 2023", "2023-10", 182.40, 1420.0, 3),
    MonthlyCostData("Nov 2023", "2023-11", 195.10, 1510.0, 3),
    MonthlyCostData("Dec 2023", "2023-12", 215.80, 1640.0, 4),
    MonthlyCostData("Jan 2024", "2024-01", 168.30, 1280.0, 2),
    MonthlyCostData("Feb 2024", "2024-02", 175.50, 1340.0, 3),
    MonthlyCostData("Mar 2024", "2024-03", 192.60, 1480.0, 3)
)

private val mockConditionSlices = listOf(
    ConditionSliceData("Highway 100%", 12, 45.0f, 540.0, 6.8),
    ConditionSliceData("Mixed 50/50", 8, 30.0f, 380.0, 7.6),
    ConditionSliceData("City Urban", 5, 18.0f, 210.0, 8.9),
    ConditionSliceData("Heavy Traffic", 2, 7.0f, 95.0, 10.2)
)

// ==========================================
// DUAL PREVIEWS: LIGHT & DARK / COMPACT & TABLET
// ==========================================

@Preview(name = "Analytics Populated - Light (Phone)", showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Analytics Populated - Dark (Phone)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
fun PreviewAnalyticsPopulated() {
    FuelSyncTheme {
        AnalyticsScreen(
            uiState = AnalyticsUiState(
                isLoading = false,
                unitPreference = UnitPreference.L_PER_100KM,
                currencyPreference = CurrencyPreference.USD,
                trendPoints = mockTrendPoints,
                monthlyData = mockMonthlyData,
                conditionSlices = mockConditionSlices,
                bestTankEfficiency = 6.9,
                bestTankDate = "Mar 26",
                worstTankEfficiency = 8.1,
                worstTankDate = "Feb 1",
                projectedNextMonthCost = 184.50,
                efficiencyDegradationPercent = -2.4,
                selectedTrendPoint = mockTrendPoints.last(),
                selectedSliceIndex = 0
            )
        )
    }
}

@Preview(name = "Analytics Populated - Tablet Expanded", showBackground = true, widthDp = 900, heightDp = 600)
@Composable
fun PreviewAnalyticsTablet() {
    FuelSyncTheme {
        AnalyticsScreen(
            uiState = AnalyticsUiState(
                isLoading = false,
                unitPreference = UnitPreference.L_PER_100KM,
                currencyPreference = CurrencyPreference.USD,
                trendPoints = mockTrendPoints,
                monthlyData = mockMonthlyData,
                conditionSlices = mockConditionSlices,
                bestTankEfficiency = 6.9,
                bestTankDate = "Mar 26",
                projectedNextMonthCost = 184.50,
                efficiencyDegradationPercent = -2.4,
                selectedTrendPoint = mockTrendPoints[3]
            )
        )
    }
}

@Preview(name = "Settings - Light Mode", showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Settings - Dark OLED Night Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
fun PreviewSettingsScreen() {
    FuelSyncTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                currentUnitSystem = FuelUnitSystem.METRIC_L_PER_100KM,
                currentCurrency = CurrencyPreference.USD,
                isNightPumpMode = false,
                currentAccentTheme = FuelAccentTheme.ELECTRIC_CYAN,
                latestOdometerKm = 26950.0
            )
        )
    }
}

@Preview(name = "Mock State: Empty", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewEmptyState() {
    FuelSyncTheme {
        FuelSyncEmptyState()
    }
}

@Preview(name = "Mock State: Loading Shimmer", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewLoadingShimmer() {
    FuelSyncTheme {
        FuelSyncLoadingShimmerState()
    }
}

@Preview(name = "Mock State: Calculation Anomaly", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewCalculationError() {
    FuelSyncTheme {
        FuelSyncCalculationErrorState()
    }
}
