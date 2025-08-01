package com.cremcashcamfin.collateralappraiser.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00CFEA),       // Cyan Blue
    secondary = Color(0xFFC63FF3),     // Violet Purple
    tertiary = Color(0xFF2F56D6),      // Royal Blue
    background = Color(0xFF2D3D5B),    // Navy Blue
    surface = Color(0xFF2D3D5B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00CFEA),       // Cyan Blue
    secondary = Color(0xFFC63FF3),     // Violet Purple
    tertiary = Color(0xFF2F56D6),      // Royal Blue
    background = Color(0xFFFFFFFF),    // White
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF2D3D5B),     // Navy Blue for contrast
    onSecondary = Color(0xFF2D3D5B),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF2D3D5B),
    onSurface = Color(0xFF2D3D5B)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun CollateralAppraiserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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
        shapes = Shapes,
        content = content
    )
}