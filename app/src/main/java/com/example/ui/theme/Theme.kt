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
    val atmosphereEffect: AtmosphereEffect = AtmosphereEffect.NONE,
    // v1.2.0：渐变主题支持——有值时背景/卡片使用该渐变（从浅到深 2-3 色）
    val gradientColors: List<Color> = emptyList()
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
    // v1.2.0：10 款渐变颜色主题（柔和渐变背景 + 对应主色）
    val gradientSunset = ThemePreset(
        id = "grad_sunset", name = "落日橘粉", style = "gradient", categoryName = "渐变色",
        primaryColor = Color(0xFFFF7E5F), secondaryColor = Color(0xFFFEB47B),
        bgColor = Color(0xFFFFF3EC), surfaceColor = Color(0xFFFFFFFF), textColor = Color(0xFF4A2C20),
        gradientColors = listOf(Color(0xFFFFD8C4), Color(0xFFFFF0E8))
    )
    val gradientOcean = ThemePreset(
        id = "grad_ocean", name = "海洋蓝", style = "gradient", categoryName = "渐变色",
        primaryColor = Color(0xFF2193B0), secondaryColor = Color(0xFF6DD5ED),
        bgColor = Color(0xFFEAF6FB), surfaceColor = Color(0xFFFFFFFF), textColor = Color(0xFF14343F),
        gradientColors = listOf(Color(0xFFC9E9F7), Color(0xFFE8F6FC))
    )
    val gradientMint = ThemePreset(
        id = "grad_mint", name = "薄荷奶绿", style = "gradient", categoryName = "渐变色",
        primaryColor = Color(0xFF11998E), secondaryColor = Color(0xFF38EF7D),
        bgColor = Color(0xFFEAFBF2), surfaceColor = Color(0xFFFFFFFF), textColor = Color(0xFF123B2E),
        gradientColors = listOf(Color(0xFFC8F0DC), Color(0xFFEAFBF2))
    )
    val gradientLavender = ThemePreset(
        id = "grad_lavender", name = "薰衣草紫", style = "gradient", categoryName = "渐变色",
        primaryColor = Color(0xFF7F7FD5), secondaryColor = Color(0xFF86A8E7),
        bgColor = Color(0xFFF2EFFB), surfaceColor = Color(0xFFFFFFFF), textColor = Color(0xFF2E2A4A),
        gradientColors = listOf(Color(0xFFDCD6F5), Color(0xFFF2EFFB))
    )
    val gradientCherry = ThemePreset(
        id = "grad_cherry", name = "樱花粉", style = "gradient", categoryName = "渐变色",
        primaryColor = Color(0xFFF857A6), secondaryColor = Color(0xFFFF5858),
        bgColor = Color(0xFFFFF0F4), surfaceColor = Color(0xFFFFFFFF), textColor = Color(0xFF4A2030),
        gradientColors = listOf(Color(0xFFFBD3E0), Color(0xFFFFF0F4))
    )
    val gradientSky = ThemePreset(
        id = "grad_sky", name = "天空蓝", style = "gradient", categoryName = "渐变色",
        primaryColor = Color(0xFF4A90D9), secondaryColor = Color(0xFF63B8FF),
        bgColor = Color(0xFFEDF5FF), surfaceColor = Color(0xFFFFFFFF), textColor = Color(0xFF1B3A5C),
        gradientColors = listOf(Color(0xFFCFE8FF), Color(0xFFEDF5FF))
    )
    val gradientPeach = ThemePreset(
        id = "grad_peach", name = "蜜桃甜橙", style = "gradient", categoryName = "渐变色",
        primaryColor = Color(0xFFFF9A8B), secondaryColor = Color(0xFFFF6A88),
        bgColor = Color(0xFFFFF5F0), surfaceColor = Color(0xFFFFFFFF), textColor = Color(0xFF4A2A20),
        gradientColors = listOf(Color(0xFFFFD8CC), Color(0xFFFFF5F0))
    )
    val gradientAurora = ThemePreset(
        id = "grad_aurora", name = "极光绿", style = "gradient", categoryName = "渐变色",
        primaryColor = Color(0xFF00B4DB), secondaryColor = Color(0xFF0083B0),
        bgColor = Color(0xFFEAF8FA), surfaceColor = Color(0xFFFFFFFF), textColor = Color(0xFF103A40),
        gradientColors = listOf(Color(0xFFC0EEF4), Color(0xFFEAF8FA))
    )
    val gradientRoseGold = ThemePreset(
        id = "grad_rosegold", name = "玫瑰金", style = "gradient", categoryName = "渐变色",
        primaryColor = Color(0xFFB76E79), secondaryColor = Color(0xFFEACDC2),
        bgColor = Color(0xFFFAF3F0), surfaceColor = Color(0xFFFFFFFF), textColor = Color(0xFF3A2028),
        gradientColors = listOf(Color(0xFFF0D9D0), Color(0xFFFAF3F0))
    )
    val gradientLemon = ThemePreset(
        id = "grad_lemon", name = "柠檬黄", style = "gradient", categoryName = "渐变色",
        primaryColor = Color(0xFFF7971E), secondaryColor = Color(0xFFFFD200),
        bgColor = Color(0xFFFFFAEB), surfaceColor = Color(0xFFFFFFFF), textColor = Color(0xFF4A3A10),
        gradientColors = listOf(Color(0xFFFFE9A8), Color(0xFFFFFAEB))
    )
    val gradientNebula = ThemePreset(
        id = "grad_nebula", name = "星云蓝紫", style = "gradient", categoryName = "渐变色",
        primaryColor = Color(0xFF5C6BC0), secondaryColor = Color(0xFFAB47BC),
        bgColor = Color(0xFFF1EFFA), surfaceColor = Color(0xFFFFFFFF), textColor = Color(0xFF26204A),
        gradientColors = listOf(Color(0xFFD8D0F5), Color(0xFFF1EFFA))
    )

    val gradientThemes: List<ThemePreset> = listOf(
        gradientSunset, gradientOcean, gradientMint, gradientLavender, gradientCherry,
        gradientSky, gradientPeach, gradientAurora, gradientRoseGold, gradientLemon, gradientNebula
    )

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
