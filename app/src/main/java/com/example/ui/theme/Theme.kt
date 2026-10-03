package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ui.uiverse.ActiveUiverseState

enum class AtmosphereEffect {
    NONE,
    STARS,
    SNOW,
    RAIN,
    FIREFLIES,
    AURORA
}

data class ThemePreset(
    val id: String,
    val name: String,
    val style: String,
    val categoryName: String = "经典",
    val primaryColor: Color,
    val secondaryColor: Color,
    val bgColor: Color,
    val surfaceColor: Color,
    val textColor: Color,
    val atmosphereEffect: AtmosphereEffect = AtmosphereEffect.NONE
)

object ThemePresetsRepository {
    // v1.1.18：默认主题改为「盛世华诞」——国潮红金配色（中国红主色 + 烫金辅助 + 暖米白底 + 深红褐字）
    // 背景为暖米白（非纯白），文字为深红褐（非绿色），整体红金喜庆，契合国庆主题。
    val shengshiTheme = ThemePreset(
        id = "shengshi_huadan",
        name = "盛世华诞",
        style = "classic",
        categoryName = "经典",
        primaryColor = Color(0xFFDE2910),      // 中国红
        secondaryColor = Color(0xFFE8A200),   // 鎏金
        bgColor = Color(0xFFFFF7EC),          // 暖米白（非纯白）
        surfaceColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF3B1F1F),        // 深红褐（非绿色）
        atmosphereEffect = AtmosphereEffect.NONE
    )

    val defaultTheme = shengshiTheme

    // 经典皮肤（可选，保留供主题切换）
    val classicTheme = ThemePreset(
        id = "classic_default",
        name = "经典皮肤",
        style = "classic",
        categoryName = "经典",
        primaryColor = Color(0xFF2196F3),
        secondaryColor = Color(0xFF42A5F5),
        bgColor = Color(0xFFFAFAFA),
        surfaceColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF212121),
        atmosphereEffect = AtmosphereEffect.NONE
    )

    // 盛世华诞为默认软件主题；经典皮肤保留可选
    val allThemes: List<ThemePreset> = listOf(
        shengshiTheme,
        classicTheme
    )
}

val LocalUiverseState = staticCompositionLocalOf { ActiveUiverseState() }

@Composable
fun MyApplicationTheme(
    themePreset: ThemePreset = ThemePresetsRepository.defaultTheme,
    uiverseState: ActiveUiverseState = ActiveUiverseState(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = themePreset.primaryColor,
        secondary = themePreset.secondaryColor,
        background = themePreset.bgColor,
        surface = themePreset.surfaceColor,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = themePreset.textColor,
        onSurface = themePreset.textColor
    )

    CompositionLocalProvider(LocalUiverseState provides uiverseState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
