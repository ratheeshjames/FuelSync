package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class FuelAccentTheme(
    val displayName: String,
    val previewLight: Color,
    val previewDark: Color
) {
    ELECTRIC_CYAN("Electric Cyan", FuelCyanPrimaryLight, FuelCyanPrimaryDark),
    PUMP_AMBER("Pump Amber", PumpAmberSecondaryLight, PumpAmberSecondaryDark),
    ECO_GREEN("Eco Green", EcoGreenTertiaryLight, EcoGreenTertiaryDark)
}

private fun getLightColorScheme(accent: FuelAccentTheme) = when (accent) {
    FuelAccentTheme.ELECTRIC_CYAN -> lightColorScheme(
        primary = FuelCyanPrimaryLight,
        onPrimary = FuelCyanOnPrimaryLight,
        primaryContainer = FuelCyanContainerLight,
        onPrimaryContainer = FuelCyanOnContainerLight,
        secondary = PumpAmberSecondaryLight,
        onSecondary = PumpAmberOnSecondaryLight,
        secondaryContainer = PumpAmberContainerLight,
        onSecondaryContainer = PumpAmberOnContainerLight,
        tertiary = EcoGreenTertiaryLight,
        onTertiary = EcoGreenOnTertiaryLight,
        tertiaryContainer = EcoGreenContainerLight,
        onTertiaryContainer = EcoGreenOnContainerLight,
        error = AlertRedLight,
        onError = AlertRedOnLight,
        errorContainer = AlertRedContainerLight,
        onErrorContainer = AlertRedOnContainerLight,
        background = NeutralSurfaceLight,
        onBackground = NeutralOnSurfaceLight,
        surface = NeutralSurfaceLight,
        onSurface = NeutralOnSurfaceLight,
        surfaceVariant = NeutralSurfaceVariantLight,
        onSurfaceVariant = NeutralOnSurfaceVariantLight,
        outline = NeutralOutlineLight,
        outlineVariant = NeutralOutlineVariantLight
    )
    FuelAccentTheme.PUMP_AMBER -> lightColorScheme(
        primary = PumpAmberSecondaryLight,
        onPrimary = PumpAmberOnSecondaryLight,
        primaryContainer = PumpAmberContainerLight,
        onPrimaryContainer = PumpAmberOnContainerLight,
        secondary = FuelCyanPrimaryLight,
        onSecondary = FuelCyanOnPrimaryLight,
        secondaryContainer = FuelCyanContainerLight,
        onSecondaryContainer = FuelCyanOnContainerLight,
        tertiary = EcoGreenTertiaryLight,
        onTertiary = EcoGreenOnTertiaryLight,
        tertiaryContainer = EcoGreenContainerLight,
        onTertiaryContainer = EcoGreenOnContainerLight,
        error = AlertRedLight,
        onError = AlertRedOnLight,
        errorContainer = AlertRedContainerLight,
        onErrorContainer = AlertRedOnContainerLight,
        background = NeutralSurfaceLight,
        onBackground = NeutralOnSurfaceLight,
        surface = NeutralSurfaceLight,
        onSurface = NeutralOnSurfaceLight,
        surfaceVariant = NeutralSurfaceVariantLight,
        onSurfaceVariant = NeutralOnSurfaceVariantLight,
        outline = NeutralOutlineLight,
        outlineVariant = NeutralOutlineVariantLight
    )
    FuelAccentTheme.ECO_GREEN -> lightColorScheme(
        primary = EcoGreenTertiaryLight,
        onPrimary = EcoGreenOnTertiaryLight,
        primaryContainer = EcoGreenContainerLight,
        onPrimaryContainer = EcoGreenOnContainerLight,
        secondary = FuelCyanPrimaryLight,
        onSecondary = FuelCyanOnPrimaryLight,
        secondaryContainer = FuelCyanContainerLight,
        onSecondaryContainer = FuelCyanOnContainerLight,
        tertiary = PumpAmberSecondaryLight,
        onTertiary = PumpAmberOnSecondaryLight,
        tertiaryContainer = PumpAmberContainerLight,
        onTertiaryContainer = PumpAmberOnContainerLight,
        error = AlertRedLight,
        onError = AlertRedOnLight,
        errorContainer = AlertRedContainerLight,
        onErrorContainer = AlertRedOnContainerLight,
        background = NeutralSurfaceLight,
        onBackground = NeutralOnSurfaceLight,
        surface = NeutralSurfaceLight,
        onSurface = NeutralOnSurfaceLight,
        surfaceVariant = NeutralSurfaceVariantLight,
        onSurfaceVariant = NeutralOnSurfaceVariantLight,
        outline = NeutralOutlineLight,
        outlineVariant = NeutralOutlineVariantLight
    )
}

private fun getDarkColorScheme(accent: FuelAccentTheme) = when (accent) {
    FuelAccentTheme.ELECTRIC_CYAN -> darkColorScheme(
        primary = FuelCyanPrimaryDark,
        onPrimary = FuelCyanOnPrimaryDark,
        primaryContainer = FuelCyanContainerDark,
        onPrimaryContainer = FuelCyanOnContainerDark,
        secondary = PumpAmberSecondaryDark,
        onSecondary = PumpAmberOnSecondaryDark,
        secondaryContainer = PumpAmberContainerDark,
        onSecondaryContainer = PumpAmberOnContainerDark,
        tertiary = EcoGreenTertiaryDark,
        onTertiary = EcoGreenOnTertiaryDark,
        tertiaryContainer = EcoGreenContainerDark,
        onTertiaryContainer = EcoGreenOnContainerDark,
        error = AlertRedDark,
        onError = AlertRedOnDark,
        errorContainer = AlertRedContainerDark,
        onErrorContainer = AlertRedOnContainerDark,
        background = NeutralSurfaceDark,
        onBackground = NeutralOnSurfaceDark,
        surface = NeutralSurfaceDark,
        onSurface = NeutralOnSurfaceDark,
        surfaceVariant = NeutralSurfaceVariantDark,
        onSurfaceVariant = NeutralOnSurfaceVariantDark,
        outline = NeutralOutlineDark,
        outlineVariant = NeutralOutlineVariantDark
    )
    FuelAccentTheme.PUMP_AMBER -> darkColorScheme(
        primary = PumpAmberSecondaryDark,
        onPrimary = PumpAmberOnSecondaryDark,
        primaryContainer = PumpAmberContainerDark,
        onPrimaryContainer = PumpAmberOnContainerDark,
        secondary = FuelCyanPrimaryDark,
        onSecondary = FuelCyanOnPrimaryDark,
        secondaryContainer = FuelCyanContainerDark,
        onSecondaryContainer = FuelCyanOnContainerDark,
        tertiary = EcoGreenTertiaryDark,
        onTertiary = EcoGreenOnTertiaryDark,
        tertiaryContainer = EcoGreenContainerDark,
        onTertiaryContainer = EcoGreenOnContainerDark,
        error = AlertRedDark,
        onError = AlertRedOnDark,
        errorContainer = AlertRedContainerDark,
        onErrorContainer = AlertRedOnContainerDark,
        background = NeutralSurfaceDark,
        onBackground = NeutralOnSurfaceDark,
        surface = NeutralSurfaceDark,
        onSurface = NeutralOnSurfaceDark,
        surfaceVariant = NeutralSurfaceVariantDark,
        onSurfaceVariant = NeutralOnSurfaceVariantDark,
        outline = NeutralOutlineDark,
        outlineVariant = NeutralOutlineVariantDark
    )
    FuelAccentTheme.ECO_GREEN -> darkColorScheme(
        primary = EcoGreenTertiaryDark,
        onPrimary = EcoGreenOnTertiaryDark,
        primaryContainer = EcoGreenContainerDark,
        onPrimaryContainer = EcoGreenOnContainerDark,
        secondary = FuelCyanPrimaryDark,
        onSecondary = FuelCyanOnPrimaryDark,
        secondaryContainer = FuelCyanContainerDark,
        onSecondaryContainer = FuelCyanOnContainerDark,
        tertiary = PumpAmberSecondaryDark,
        onTertiary = PumpAmberOnSecondaryDark,
        tertiaryContainer = PumpAmberContainerDark,
        onTertiaryContainer = PumpAmberOnContainerDark,
        error = AlertRedDark,
        onError = AlertRedOnDark,
        errorContainer = AlertRedContainerDark,
        onErrorContainer = AlertRedOnContainerDark,
        background = NeutralSurfaceDark,
        onBackground = NeutralOnSurfaceDark,
        surface = NeutralSurfaceDark,
        onSurface = NeutralOnSurfaceDark,
        surfaceVariant = NeutralSurfaceVariantDark,
        onSurfaceVariant = NeutralOnSurfaceVariantDark,
        outline = NeutralOutlineDark,
        outlineVariant = NeutralOutlineVariantDark
    )
}

// Ultra-dark night pump mode with minimal glare
private fun getOledNightColorScheme(accent: FuelAccentTheme) = darkColorScheme(
    primary = when (accent) {
        FuelAccentTheme.ELECTRIC_CYAN -> FuelCyanPrimaryDark
        FuelAccentTheme.PUMP_AMBER -> PumpAmberSecondaryDark
        FuelAccentTheme.ECO_GREEN -> EcoGreenTertiaryDark
    },
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF162026),
    onPrimaryContainer = Color.White,
    secondary = OledAccentRed,
    onSecondary = OledSurface,
    secondaryContainer = AlertRedContainerDark,
    onSecondaryContainer = AlertRedOnContainerDark,
    tertiary = EcoGreenTertiaryDark,
    onTertiary = EcoGreenOnTertiaryDark,
    tertiaryContainer = EcoGreenContainerDark,
    onTertiaryContainer = EcoGreenOnContainerDark,
    error = AlertRedDark,
    onError = AlertRedOnDark,
    errorContainer = AlertRedContainerDark,
    onErrorContainer = AlertRedOnContainerDark,
    background = OledSurface,
    onBackground = OledOnSurface,
    surface = OledSurface,
    onSurface = OledOnSurface,
    surfaceVariant = OledSurfaceContainer,
    onSurfaceVariant = NeutralOnSurfaceVariantDark,
    outline = NeutralOutlineDark,
    outlineVariant = NeutralOutlineVariantDark
)

@Composable
fun FuelSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    oledNightPumpMode: Boolean = false,
    dynamicColor: Boolean = false,
    accentTheme: FuelAccentTheme = FuelAccentTheme.ELECTRIC_CYAN,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        oledNightPumpMode -> getOledNightColorScheme(accentTheme)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getDarkColorScheme(accentTheme)
        else -> getLightColorScheme(accentTheme)
    }

    CompositionLocalProvider(
        LocalExpressiveShapes provides ExpressiveCustomShapes(),
        LocalExpressiveMotion provides ExpressiveMotionTokens()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FuelSyncTypography,
            shapes = FuelSyncShapes,
            content = content
        )
    }
}
