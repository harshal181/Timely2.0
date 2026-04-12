package com.om.timely.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light scheme wired to Timely brand colors
private val LightColorScheme = lightColorScheme(
    primary = BrandColors.PrimaryPurple,          // #7C3AED
    onPrimary = Color.White,
    secondary = BrandColors.SecondaryPurple,       // #6366F1
    onSecondary = Color.White,
    tertiary = BrandColors.LightPurple,            // #9333EA
    onTertiary = Color.White,
    background = BrandColors.BgOffWhite,           // #FAFAFA
    onBackground = BrandColors.TextDark,           // #1F2937
    surface = BrandColors.BgWhite,                 // #FFFFFF
    onSurface = BrandColors.TextDark,              // #1F2937
    surfaceVariant = BrandColors.CardGrayLight,    // #F9FAFB
    onSurfaceVariant = BrandColors.TextGray,       // #6B7280
    outline = BrandColors.BorderStandard,          // #E5E7EB
    outlineVariant = BrandColors.BorderLight,      // #F3F4F6
    error = BrandColors.Error,                     // #EF4444
    onError = Color.White,
)

// Dark scheme — keeps purple brand identity on dark backgrounds
private val DarkColorScheme = darkColorScheme(
    primary = BrandColors.PurpleTint,
    onPrimary = Color.White,
    secondary = BrandColors.SecondaryPurple,
    onSecondary = Color.White,
    tertiary = BrandColors.LightPurple,
    onTertiary = Color.White,
    background = Color(0xFF0F0F0F),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = BrandColors.TextLightGray,
    outline = Color(0xFF3A3A3C),
    error = BrandColors.Error,
    onError = Color.White,
)

@Composable
fun TimelyTheme(
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