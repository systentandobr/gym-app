package com.tadevolta.gym.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.tadevolta.gym.ui.theme.*

private val LightColorScheme = lightColorScheme(
    primary = Primary500,
    onPrimary = Color.White,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary900,
    
    secondary = Secondary500,
    onSecondary = Color.White,
    secondaryContainer = Secondary100,
    onSecondaryContainer = Secondary900,
    
    tertiary = Bronze,
    onTertiary = Color.White,
    
    error = Destructive,
    onError = DestructiveForeground,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = Background,
    onBackground = Foreground,
    
    surface = Card,
    onSurface = CardForeground,
    surfaceVariant = Muted,
    onSurfaceVariant = MutedForeground,
    
    outline = Border,
    outlineVariant = Input,
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary400,
    onPrimary = Primary900,
    primaryContainer = Primary800,
    onPrimaryContainer = Primary100,
    
    secondary = Secondary400,
    onSecondary = Secondary900,
    secondaryContainer = Secondary800,
    onSecondaryContainer = Secondary100,
    
    tertiary = BronzeLight,
    onTertiary = Color.Black,
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = BackgroundDark,
    onBackground = ForegroundDark,
    
    surface = CardDark,
    onSurface = CardForegroundDark,
    surfaceVariant = MutedDark,
    onSurfaceVariant = MutedForegroundDark,
    
    outline = BorderDark,
    outlineVariant = InputDark,
)

@Composable
fun TadevoltaGymTheme(
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
