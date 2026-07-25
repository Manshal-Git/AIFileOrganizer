package com.manshal79.aifileorganizer.presentation.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AppColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.SurfaceContainerLowest,
    primaryContainer = AppColors.PrimaryContainer,
    onPrimaryContainer = AppColors.OnPrimaryContainer,
    inversePrimary = AppColors.PrimaryFixedDim,
    secondary = AppColors.Secondary,
    onSecondary = AppColors.SurfaceContainerLowest,
    secondaryContainer = AppColors.SecondaryContainer,
    onSecondaryContainer = AppColors.OnSecondaryContainer,
    tertiary = AppColors.Tertiary,
    onTertiary = AppColors.SurfaceContainerLowest,
    tertiaryContainer = AppColors.TertiaryContainer,
    background = AppColors.Surface,
    onBackground = AppColors.OnSurface,
    surface = AppColors.Surface,
    onSurface = AppColors.OnSurface,
    surfaceVariant = AppColors.SurfaceVariant,
    onSurfaceVariant = AppColors.OnSurfaceVariant,
    surfaceContainerLowest = AppColors.SurfaceContainerLowest,
    surfaceContainerLow = AppColors.SurfaceContainerLow,
    surfaceContainer = AppColors.SurfaceContainer,
    surfaceContainerHigh = AppColors.SurfaceContainerHigh,
    surfaceContainerHighest = AppColors.SurfaceVariant,
    surfaceDim = AppColors.SurfaceDim,
    surfaceBright = AppColors.Surface,
    outline = AppColors.Outline,
    outlineVariant = AppColors.OutlineVariant,
    error = AppColors.Error,
    onError = AppColors.SurfaceContainerLowest,
    errorContainer = AppColors.ErrorContainer,
    onErrorContainer = AppColors.OnErrorContainer,
    inverseSurface = AppColors.InverseSurface,
    inverseOnSurface = AppColors.InverseOnSurface,
)

// Inter isn't bundled as a font resource, so only the design's metrics are carried
// over — the platform default family renders them.
private val AppTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.6).sp,
        ),
        headlineSmall = headlineSmall.copy(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.5).sp,
        ),
        titleMedium = titleMedium.copy(
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp,
        ),
        titleSmall = titleSmall.copy(
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        ),
        bodyLarge = bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
        labelLarge = labelLarge.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
        labelMedium = labelMedium.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
        ),
        labelSmall = labelSmall.copy(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium),
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}

/** Design tokens that have no Material 3 role but are used across the screen. */
internal object AppDimens {
    val ContainerPadding = 32.dp
    val Gutter = 24.dp
    val StackGap = 16.dp
    val SideNavWidth = 232.dp
    val TopBarHeight = 72.dp

    /** Playbook rule 10: minimum touch target for anything tappable. */
    val MinTouchTarget = 48.dp
}

/** Uppercase eyebrow label style used by the side nav and stat cards. */
internal val EyebrowTextStyle: TextStyle
    @Composable get() = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp)
