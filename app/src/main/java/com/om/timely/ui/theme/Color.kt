package com.om.timely.ui.theme

import androidx.compose.ui.graphics.Color

// ── Legacy M3 seed colors (used by Theme.kt) ────────────────────────────────
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)

// ── Timely App – Brand Color System ─────────────────────────────────────────
object BrandColors {
    // Primary Brand Colors
    val PrimaryPurple = Color(0xFF7C3AED)
    val SecondaryPurple = Color(0xFF6366F1)
    val DeepPurple = Color(0xFF4C1D95)
    val LightPurple = Color(0xFF9333EA)
    val PurpleTint = Color(0xFF818CF8)

    // Backgrounds
    val BgStudent = Color(0xFFF8F7FF)
    val BgFaculty = Color(0xFFF0FDF4)
    val BgWhite = Color(0xFFFFFFFF)
    val BgOffWhite = Color(0xFFFAFAFA)
    
    // Card Backgrounds
    val CardPurpleLight = Color(0xFFF5F3FF)
    val CardGreenLight = Color(0xFFECFDF5)
    val CardYellowLight = Color(0xFFFEF3C7)
    val CardBlueLight = Color(0xFFEFF6FF)
    val CardRedLight = Color(0xFFFEF2F2)
    val CardGrayLight = Color(0xFFF9FAFB)

    // Text Colors
    val TextDark = Color(0xFF1F2937)
    val TextMedium = Color(0xFF374151)
    val TextGray = Color(0xFF6B7280)
    val TextLightGray = Color(0xFF9CA3AF)

    // Functional & Notification Type Colors
    val Success = Color(0xFF059669)     // Also Green
    val Error = Color(0xFFEF4444)       // Cancel (Red)
    val Warning = Color(0xFFD97706)     // Amber
    val Info = Color(0xFF2563EB)        // Change (Blue)
    val ExtraClass = Color(0xFF16A34A)  // Green
    val GeneralNotice = Color(0xFFFF9800) // Orange

    // Borders
    val BorderLight = Color(0xFFF3F4F6)
    val BorderStandard = Color(0xFFE5E7EB)
    val BorderMedium = Color(0xFFDDD6FE)
    val BorderDark = Color(0xFF9CA3AF)
}