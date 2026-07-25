package com.manshal79.aifileorganizer.presentation.designsystem

import androidx.compose.ui.graphics.Color

/**
 * Palette from the "Frictionless Desktop" design.
 *
 * The design is light-only, so there is no dark counterpart here. Everything that
 * maps onto a Material 3 role lives in [AppColorScheme]; the slate tones below are
 * the design's dark surfaces (side nav, stat cards, toast) which have no M3 role.
 */
internal object AppColors {
    val Primary = Color(0xFF493EE5)
    val PrimaryHover = Color(0xFF4338CA)
    val PrimaryContainer = Color(0xFF635BFF)
    val OnPrimaryContainer = Color(0xFFFEFAFF)
    val PrimaryFixedDim = Color(0xFFC3C0FF)
    val OnPrimaryFixed = Color(0xFF0F0069)

    val Surface = Color(0xFFFBF8FF)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFF3F2FF)
    val SurfaceContainer = Color(0xFFECECFF)
    val SurfaceContainerHigh = Color(0xFFE4E7FF)
    val SurfaceVariant = Color(0xFFDDE1FF)
    val SurfaceDim = Color(0xFFD4D8F7)

    val OnSurface = Color(0xFF151A31)
    val OnSurfaceVariant = Color(0xFF464555)
    val Outline = Color(0xFF777587)
    val OutlineVariant = Color(0xFFC7C4D8)

    val Secondary = Color(0xFF5B5F62)
    val SecondaryContainer = Color(0xFFDDE0E3)
    val OnSecondaryContainer = Color(0xFF5F6366)

    val Tertiary = Color(0xFF974100)
    val TertiaryContainer = Color(0xFFBE5400)
    val TertiaryFixedDim = Color(0xFFFFB68F)

    val Error = Color(0xFFBA1A1A)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF93000A)

    val InverseSurface = Color(0xFF2A2F47)
    val InverseOnSurface = Color(0xFFF0EFFF)

    // Dark chrome (Tailwind slate) used by the side nav, stat cards and toast.
    val Slate900 = Color(0xFF0F172A)
    val Slate800 = Color(0xFF1E293B)
    val Slate700 = Color(0xFF334155)
    val Slate600 = Color(0xFF475569)
    val Slate400 = Color(0xFF94A3B8)
    val Slate300 = Color(0xFFCBD5E1)
}
