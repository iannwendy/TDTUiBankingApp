package com.example.tdtumobilebanking.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = Color.White,
    secondary = BrandRed,
    onSecondary = Color.White,
    secondaryContainer = BrandRedDark,
    onSecondaryContainer = Color.White,
    tertiary = BrandSky,
    background = Color.White,
    surface = SurfaceHigh,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    surfaceVariant = SurfaceLow
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    secondary = BrandRed,
    onSecondary = Color.White,
    background = Color(0xFF0F1724),
    surface = Color(0xFF0F1724),
    onSurface = Color(0xFFE8ECF5),
    onSurfaceVariant = Color(0xFFCAD3E4),
    outline = Color(0xFF2C3A55)
)

@Composable
fun TDTUMobileBankingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}