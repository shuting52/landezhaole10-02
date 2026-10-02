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
    // v1.1.14：主题切换回归「经典皮肤」——删除全局代码/组件定制功能，
    // 仅保留默认经典皮肤（简单干净：经典蓝主色 + 纯白底 + 深灰字）与自定义背景。
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

    val defaultTheme = classicTheme

    // 仅保留经典皮肤（主题切换即经典，无多余皮肤可选）
    val allThemes: List<ThemePreset> = listOf(
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
