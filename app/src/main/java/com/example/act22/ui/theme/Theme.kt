package com.example.act22.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.act22.R

// Dark and Light Color Schemes
private val LightColorScheme = lightColorScheme(
    //backgrounds
    primary = DarkPurple60,
    primaryContainer = DarkPurple40,
    secondary = Purple40,
    secondaryContainer = Purple60,
    tertiary = Lilac40,
    tertiaryContainer = Lilac20,
    background = White,
    surface = Grey20,
    surfaceVariant = Grey40,

    //texts
    onPrimary = White,
    onPrimaryContainer = DarkPurple20,
    onSecondary = White,
    onSecondaryContainer = Grey40,
    onTertiary = White,
    onTertiaryContainer = DarkPurple60,
    onBackground = DarkPurple60,
    onSurface = Purple40,
    onSurfaceVariant = Lilac40
)

private val DarkColorScheme = darkColorScheme(
    //backgrounds
    primary = DarkPurple60,
    primaryContainer = DarkPurple40,
    secondary = Purple40,
    secondaryContainer = Purple60,
    tertiary = Lilac40,
    tertiaryContainer = Lilac20,
    background = Black,
    surface = Grey80,
    surfaceVariant = Grey60,

    //texts
    onPrimary = White,
    onPrimaryContainer = DarkPurple20,
    onSecondary = White,
    onSecondaryContainer = Grey40,
    onTertiary = White,
    onTertiaryContainer = DarkPurple60,
    onBackground = White,
    onSurface = Purple20,
    onSurfaceVariant = Lilac20
)

@Composable
fun ACT22Theme(
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

@Composable
fun getLogoResource(darkTheme: Boolean = isSystemInDarkTheme()): Int {
//    return if (darkTheme) {
//        R.drawable.logo_dark
//    } else {
//        R.drawable.logo_bright
//    }
    return  R.drawable.logo_bright
}

