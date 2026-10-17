package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GeminiBluePrimary,
    onPrimary = BackgroundDark,
    primaryContainer = SurfaceElevatedDark,
    onPrimaryContainer = GeminiBluePrimary,
    secondary = GeminiBlueSecondary,
    onSecondary = BackgroundDark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = GeminiBlueSecondary,
    tertiary = GeminiPurpleTertiary,
    onTertiary = BackgroundDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = GeminiBlueSecondary,
    onPrimary = SurfaceLight,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = GeminiBlueSecondary,
    secondary = GeminiBluePrimary,
    onSecondary = SurfaceLight,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = GeminiBluePrimary,
    tertiary = GeminiPurpleTertiary,
    onTertiary = SurfaceLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = BorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek Gemini dark workspace
    dynamicColor: Boolean = false, // Keep consistent branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
