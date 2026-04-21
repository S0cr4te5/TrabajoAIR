package com.sendaurjc.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores principales - ROJO
private val PrimaryRed = Color(0xFFD32F2F)
private val PrimaryRedLight = Color(0xFFE53935)
private val PrimaryRedDark = Color(0xFFC62828)
private val SecondaryRed = Color(0xFFB71C1C)
private val TertiaryRed = Color(0xFF880E4F)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    primaryContainer = PrimaryRedLight,
    onPrimaryContainer = Color.White,
    secondary = SecondaryRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDADA),
    onSecondaryContainer = Color(0xFF600D0D),
    tertiary = TertiaryRed,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFDA1D0),
    onTertiaryContainer = Color(0xFF3E001A),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF201A1A),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF201A1A),
    surfaceVariant = Color(0xFFFFDADA),
    onSurfaceVariant = Color(0xFF604141),
    outline = Color(0xFF9E7373),
    outlineVariant = Color(0xFFD9C2C2),
    scrim = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRedLight,
    onPrimary = Color(0xFF67000B),
    primaryContainer = PrimaryRedDark,
    onPrimaryContainer = Color(0xFFFFDADA),
    secondary = Color(0xFFFFB4AC),
    onSecondary = Color(0xFF5F1111),
    secondaryContainer = Color(0xFF78291C),
    onSecondaryContainer = Color(0xFFFFDADA),
    tertiary = Color(0xFFFDA1D0),
    onTertiary = Color(0xFF5E1A3C),
    tertiaryContainer = Color(0xFF7A3054),
    onTertiaryContainer = Color(0xFFFDA1D0),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF201A1A),
    onBackground = Color(0xFFEBE0DF),
    surface = Color(0xFF201A1A),
    onSurface = Color(0xFFEBE0DF),
    surfaceVariant = Color(0xFF604141),
    onSurfaceVariant = Color(0xFFD9C2C2),
    outline = Color(0xFF9E7373),
    outlineVariant = Color(0xFF604141),
    scrim = Color.Black
)

@Composable
fun SendaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

