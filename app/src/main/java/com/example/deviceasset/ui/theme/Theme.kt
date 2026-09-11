package com.example.deviceasset.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val InkBlack = Color(0xFF263238)
private val WarmPaper = Color(0xFFF4F0E7)
private val RiceWhite = Color(0xFFFFFCF5)
private val Celadon = Color(0xFF637D72)
private val Vermilion = Color(0xFFB5664B)

private val LightColors = lightColorScheme(
    primary = InkBlack,
    onPrimary = Color(0xFFF8F4EB),
    primaryContainer = Color(0xFFD9E0D9),
    onPrimaryContainer = Color(0xFF1D2928),
    secondary = Celadon,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE6DE),
    onSecondaryContainer = Color(0xFF24352F),
    tertiary = Vermilion,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF0D9CB),
    onTertiaryContainer = Color(0xFF4B2419),
    background = WarmPaper,
    onBackground = InkBlack,
    surface = RiceWhite,
    onSurface = InkBlack,
    surfaceVariant = Color(0xFFE8E4DA),
    onSurfaceVariant = Color(0xFF626A64),
    outline = Color(0xFF9DA39B),
    outlineVariant = Color(0xFFD2D2C8),
    error = Color(0xFFAC3F3F),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE2E8DE),
    onPrimary = Color(0xFF26302D),
    primaryContainer = Color(0xFF43534A),
    onPrimaryContainer = Color(0xFFE2E8DE),
    secondary = Color(0xFFB7CBBB),
    onSecondary = Color(0xFF26332D),
    secondaryContainer = Color(0xFF3E5146),
    onSecondaryContainer = Color(0xFFD9E7D9),
    tertiary = Color(0xFFF0AF91),
    onTertiary = Color(0xFF4B2114),
    tertiaryContainer = Color(0xFF693B2C),
    onTertiaryContainer = Color(0xFFFFDBCB),
    background = Color(0xFF1D2422),
    onBackground = Color(0xFFE4E8DF),
    surface = Color(0xFF252C29),
    onSurface = Color(0xFFE4E8DF),
    surfaceVariant = Color(0xFF3A423D),
    onSurfaceVariant = Color(0xFFC1C9BF),
    outline = Color(0xFF89958A),
    outlineVariant = Color(0xFF4B554E),
)

private val InkTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 27.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    ),
)

private val InkShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
)

@Composable
fun DeviceAssetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = InkTypography,
        shapes = InkShapes,
        content = content,
    )
}
