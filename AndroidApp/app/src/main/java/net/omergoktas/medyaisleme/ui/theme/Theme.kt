package net.omergoktas.medyaisleme.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import net.omergoktas.medyaisleme.data.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PrimarySky,
    onPrimary = BgDark,
    primaryContainer = PanelBgDark,
    onPrimaryContainer = PrimarySky,
    secondary = FocusRingAmber,
    onSecondary = BgDark,
    background = BgDark,
    onBackground = TextLight,
    surface = CardBgDark,
    onSurface = TextLight,
    surfaceVariant = PanelBgDark,
    onSurfaceVariant = TextMuted,
    outline = BorderDark,
    error = ErrorRose,
    onError = TextLight
)

private val LightColorScheme = lightColorScheme(
    primary = PrimarySkyHover,
    onPrimary = TextLight,
    primaryContainer = PanelBgLight,
    onPrimaryContainer = PrimarySkyHover,
    secondary = FocusRingAmber,
    onSecondary = BgDark,
    background = BgLight,
    onBackground = TextDark,
    surface = CardBgLight,
    onSurface = TextDark,
    surfaceVariant = PanelBgLight,
    onSurfaceVariant = TextMutedLight,
    outline = BorderLight,
    error = ErrorRose,
    onError = TextLight
)

@Composable
fun MedyaIslemeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.DYNAMIC -> systemInDark
    }

    val context = LocalContext.current
    val colorScheme = when {
        themeMode == ThemeMode.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
