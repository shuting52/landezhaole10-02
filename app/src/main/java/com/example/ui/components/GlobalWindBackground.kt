package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalUiverseState
import com.example.ui.components.ComponentThemeResolver
import com.example.ui.components.LocalComponentThemes
import com.example.ui.uiverse.PatternStylePreset
import com.example.ui.uiverse.UiKitPreset
import com.example.ui.uiverse.DynamicEffectPreset
import androidx.compose.runtime.Composable
import kotlin.math.sin

// Uiverse.io Warm Peach Wind Gradient Palette
val WindPeach1 = Color(0xFFFEC195)
val WindPeach2 = Color(0xFFFCC196)
val WindPeach3 = Color(0xFFFABD92)
val WindPeach4 = Color(0xFFFAC097)
val WindPeach5 = Color(0xFFFAC39C)

/**
 * 全局胶囊化风动背景（Global Capsule Wind Background）
 * - 整体背景升级为「大胶囊舞台底衬 + 动态悬浮流光胶囊群 + 微光胶囊轮廓」
 * - 全屏背景与自定义媒体均以胶囊形态呈现（圆角 36dp 大胶囊轮廓与内部漂浮动感胶囊）
 * - 支持跟随主题色同步变色（霓虹绿地图/国庆红/暖桃风），彻底落实 UI 整体胶囊化设计
 */
@Composable
fun GlobalWindBackground(
    modifier: Modifier = Modifier,
    bgMediaType: String = "none",
    bgMediaUrl: String = "",
    // 主题切换优化——背景跟随软件背景（主题背景色）同步
    themeBgColor: Color? = null,
    themePrimaryColor: Color? = null,
    // v1.2.0：渐变主题背景 + 动态效果主题
    themeGradientColors: List<Color> = emptyList(),
    dynamicEffect: DynamicEffectPreset = DynamicEffectPreset.NONE,
    content: @Composable BoxScope.() -> Unit
) {
    val uiverse = LocalUiverseState.current
    val infiniteTransition = rememberInfiniteTransition(label = "capsule_wind_background")

    // 1. 胶囊流光与漂移核心动画 (周期 2.8s 平滑往复)
    val windShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "windShift"
    )

    // 2. 悬浮微胶囊群轻摆摇曳角度 (3.2s)
    val slay1Angle by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slay1"
    )

    // 3. 辅动微胶囊摆角 (2.6s)
    val slay2Angle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slay2"
    )

    // 4. 浮光呼吸脉冲 (2.2s)
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val activePrimary = themePrimaryColor ?: Color(0xFF00C080)
    // v1.1.10：控制台「主题工具箱」global 组件可覆盖全局背景色（优先级：控制台 > 主题预设 > 默认）
    val globalComp = ComponentThemeResolver.resolve(LocalComponentThemes.current, "global")
    // v1.1.22：global.css 纯白背景（默认配置）视为未定制，强制恢复经典皮肤胶囊渐变背景，不再整页白底
    val globalBg = globalComp?.backgroundColor
    val effectiveGlobalBg = if (globalBg != null && globalBg != Color.White) globalBg else null
    val activeBg = effectiveGlobalBg ?: (themeBgColor ?: Color(0xFFFFFFFF))

    Box(modifier = modifier.fillMaxSize()) {
        // ========== 1. 底层动态全屏画布：渲染多层悬浮流动胶囊与微光粒子群 ==========
        when {
            // v1.2.0：动态效果主题优先（可叠加在任意主题上）
            dynamicEffect != DynamicEffectPreset.NONE -> {
                DynamicEffectBackground(
                    effect = dynamicEffect,
                    baseColor = themeBgColor ?: Color(0xFFFFF7EC),
                    accent = activePrimary,
                    windShift = windShift,
                    slay1Angle = slay1Angle,
                    slay2Angle = slay2Angle,
                    glowPulse = glowPulse,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // v1.2.0：渐变颜色主题（柔和渐变背景）
            themeGradientColors.size >= 2 -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(themeGradientColors[0], themeGradientColors[1]),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height)
                        )
                    )
                }
            }
            uiverse.patternStyle == PatternStylePreset.CYBER_GRID || uiverse.activeKit == UiKitPreset.CYBERPUNK_NEON -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(Color(0xFF070913))
                    val gridSize = 36.dp.toPx()
                    val cols = (size.width / gridSize).toInt() + 2
                    val rows = (size.height / gridSize).toInt() + 2
                    for (i in 0..cols) {
                        drawLine(
                            color = Color(0xFF00F0FF).copy(alpha = 0.07f),
                            start = Offset(i * gridSize, 0f),
                            end = Offset(i * gridSize, size.height)
                        )
                    }
                    for (j in 0..rows) {
                        drawLine(
                            color = Color(0xFFFF0055).copy(alpha = 0.05f),
                            start = Offset(0f, j * gridSize + (windShift * gridSize % gridSize)),
                            end = Offset(size.width, j * gridSize + (windShift * gridSize % gridSize))
                        )
                    }
                    // 赛博朋克风：大型悬浮荧光胶囊
                    drawCapsule(
                        center = Offset(size.width * 0.5f, size.height * 0.35f),
                        length = size.width * 0.88f,
                        thickness = 130.dp.toPx(),
                        angleDegrees = -18f + windShift * 4f,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF00F0FF).copy(alpha = 0.18f), Color.Transparent)
                        )
                    )
                    drawCapsule(
                        center = Offset(size.width * 0.65f, size.height * 0.72f),
                        length = size.width * 0.75f,
                        thickness = 100.dp.toPx(),
                        angleDegrees = 24f - windShift * 5f,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFFF0055).copy(alpha = 0.14f), Color.Transparent)
                        )
                    )
                }
            }
            uiverse.patternStyle == PatternStylePreset.DOT_MATRIX || uiverse.activeKit == UiKitPreset.NEO_BRUTALISM_POP -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(Color(0xFFFFFEE8))
                    val spacing = 28.dp.toPx()
                    val cols = (size.width / spacing).toInt() + 1
                    val rows = (size.height / spacing).toInt() + 1
                    for (i in 0..cols) {
                        for (j in 0..rows) {
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.09f),
                                radius = 2.dp.toPx(),
                                center = Offset(i * spacing, j * spacing)
                            )
                        }
                    }
                    // 新粗野风：带有黑描边的醒目黄色/粉色实心大胶囊
                    drawCapsule(
                        center = Offset(size.width * 0.5f, size.height * 0.30f),
                        length = size.width * 0.85f,
                        thickness = 120.dp.toPx(),
                        angleDegrees = -12f,
                        color = Color(0xFFFFDF00).copy(alpha = 0.35f)
                    )
                    drawCapsule(
                        center = Offset(size.width * 0.5f, size.height * 0.30f),
                        length = size.width * 0.85f,
                        thickness = 120.dp.toPx(),
                        angleDegrees = -12f,
                        color = Color.Black.copy(alpha = 0.25f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            uiverse.patternStyle == PatternStylePreset.HEXAGON_MESH || uiverse.activeKit == UiKitPreset.GLASSMORPHISM_AURORA -> {
                // v1.1.11：透明磨砂改胶囊——白色底 + 柔和半透明胶囊（非深色磨砂玻璃）
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(Color(0xFFFFFFFF))
                    // 极光渐变胶囊体
                    drawCapsule(
                        center = Offset(size.width * (0.35f + windShift * 0.1f), size.height * 0.28f),
                        length = size.width * 0.95f,
                        thickness = 160.dp.toPx(),
                        angleDegrees = -25f,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF6366F1).copy(alpha = 0.14f), Color.Transparent)
                        )
                    )
                    drawCapsule(
                        center = Offset(size.width * (0.68f - windShift * 0.1f), size.height * 0.75f),
                        length = size.width * 0.85f,
                        thickness = 140.dp.toPx(),
                        angleDegrees = 20f,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFEC4899).copy(alpha = 0.10f), Color.Transparent)
                        )
                    )
                }
            }
            uiverse.customStyle?.backgroundBrush != null -> {
                Box(modifier = Modifier.fillMaxSize().background(uiverse.customStyle.backgroundBrush))
            }
            // v1.1.22：纯白背景（global.css 默认）视为未定制，落入经典胶囊渐变分支
            uiverse.customStyle?.backgroundColor != null && uiverse.customStyle.backgroundColor != Color.White -> {
                Box(modifier = Modifier.fillMaxSize().background(uiverse.customStyle.backgroundColor))
            }
            else -> {
                // 默认主题胶囊风背景（跟随当前主题色：如霓虹绿地图/国庆红/暖桃风）
                val base = themeBgColor ?: WindPeach1
                val accent = themePrimaryColor ?: WindPeach5

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // 1. 底衬渐变
                    val startX = -width * 0.2f + windShift * width * 0.3f
                    val startY = 0f + windShift * height * 0.1f
                    val endX = width * 1.1f + windShift * width * 0.2f
                    val endY = height * 1.0f

                    if (themeBgColor != null) {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    base,
                                    base.copy(alpha = 0.94f),
                                    Color(
                                        red = (base.red + accent.red) / 2f,
                                        green = (base.green + accent.green) / 2f,
                                        blue = (base.blue + accent.blue) / 2f
                                    ).copy(alpha = 0.88f),
                                    base
                                ),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY)
                            )
                        )
                    } else {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(WindPeach1, WindPeach2, WindPeach3, WindPeach4, WindPeach5),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY)
                            )
                        )
                    }

                    // 2. 大型核心流光胶囊 1（斜穿背景的主胶囊体）
                    val capsule1Center = Offset(
                        width * 0.50f + (windShift - 0.5f) * width * 0.08f,
                        height * 0.36f + (windShift - 0.5f) * height * 0.06f
                    )
                    drawCapsule(
                        center = capsule1Center,
                        length = width * 0.98f,
                        thickness = 175.dp.toPx() * glowPulse,
                        angleDegrees = -22f + (windShift - 0.5f) * 6f,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.26f),
                                accent.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
                    // 核心胶囊的高光微边框
                    drawCapsule(
                        center = capsule1Center,
                        length = width * 0.98f,
                        thickness = 175.dp.toPx() * glowPulse,
                        angleDegrees = -22f + (windShift - 0.5f) * 6f,
                        color = accent.copy(alpha = 0.16f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // 3. 次级流动胶囊 2（右上方浮动胶囊）
                    val capsule2Center = Offset(
                        width * 0.82f - (windShift - 0.5f) * width * 0.10f,
                        height * 0.18f + (windShift - 0.5f) * height * 0.04f
                    )
                    drawCapsule(
                        center = capsule2Center,
                        length = width * 0.70f,
                        thickness = 115.dp.toPx(),
                        angleDegrees = 26f - (windShift - 0.5f) * 8f,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
                    drawCapsule(
                        center = capsule2Center,
                        length = width * 0.70f,
                        thickness = 115.dp.toPx(),
                        angleDegrees = 26f - (windShift - 0.5f) * 8f,
                        color = accent.copy(alpha = 0.12f),
                        style = Stroke(width = 1.2.dp.toPx())
                    )

                    // 4. 次级流动胶囊 3（左下方底座胶囊）
                    val capsule3Center = Offset(
                        width * 0.24f + (windShift - 0.5f) * width * 0.08f,
                        height * 0.76f - (windShift - 0.5f) * height * 0.05f
                    )
                    drawCapsule(
                        center = capsule3Center,
                        length = width * 0.80f,
                        thickness = 135.dp.toPx(),
                        angleDegrees = -18f + (windShift - 0.5f) * 6f,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                accent.copy(alpha = 0.18f),
                                accent.copy(alpha = 0.06f)
                            )
                        )
                    )

                    // 5. 动感悬浮小胶囊微粒群（国潮红金·Floating Capsule Constellation）
                    val goldGlint = Color(0xFFFFD700)
                    // 胶囊粒子 A：左上微光悬浮 (华夏朱红 + 鎏金边)
                    drawCapsule(
                        center = Offset(width * 0.16f, height * 0.12f + windShift * 18f),
                        length = 48.dp.toPx(),
                        thickness = 16.dp.toPx(),
                        angleDegrees = slay1Angle * 2.2f,
                        brush = Brush.horizontalGradient(
                            listOf(accent.copy(alpha = 0.45f), goldGlint.copy(alpha = 0.35f))
                        )
                    )
                    drawCapsule(
                        center = Offset(width * 0.16f, height * 0.12f + windShift * 18f),
                        length = 48.dp.toPx(),
                        thickness = 16.dp.toPx(),
                        angleDegrees = slay1Angle * 2.2f,
                        color = goldGlint.copy(alpha = 0.45f),
                        style = Stroke(width = 1.2.dp.toPx())
                    )

                    // 胶囊粒子 B：右上发光胶囊环 (鎏金华彩)
                    drawCapsule(
                        center = Offset(width * 0.88f, height * 0.26f - windShift * 15f),
                        length = 64.dp.toPx(),
                        thickness = 20.dp.toPx(),
                        angleDegrees = -32f + slay2Angle * 1.5f,
                        brush = Brush.linearGradient(
                            listOf(goldGlint.copy(alpha = 0.50f), accent.copy(alpha = 0.35f))
                        ),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 胶囊粒子 C：左侧居中漂浮小胶囊 (祥瑞锦红)
                    drawCapsule(
                        center = Offset(width * 0.10f, height * 0.48f + windShift * 22f),
                        length = 42.dp.toPx(),
                        thickness = 14.dp.toPx(),
                        angleDegrees = 18f,
                        color = accent.copy(alpha = 0.30f)
                    )

                    // 胶囊粒子 D：右侧居中细长胶囊 (金丝织带)
                    drawCapsule(
                        center = Offset(width * 0.92f, height * 0.58f - windShift * 20f),
                        length = 58.dp.toPx(),
                        thickness = 16.dp.toPx(),
                        angleDegrees = -24f + slay1Angle,
                        brush = Brush.horizontalGradient(
                            listOf(accent.copy(alpha = 0.32f), goldGlint.copy(alpha = 0.30f))
                        )
                    )

                    // 胶囊粒子 E：中下方横向微动胶囊 (赤金华缎)
                    drawCapsule(
                        center = Offset(width * 0.52f, height * 0.84f + windShift * 14f),
                        length = 76.dp.toPx(),
                        thickness = 22.dp.toPx(),
                        angleDegrees = 10f - windShift * 5f,
                        brush = Brush.horizontalGradient(
                            listOf(accent.copy(alpha = 0.38f), goldGlint.copy(alpha = 0.25f))
                        )
                    )

                    // 胶囊粒子 F：右下角小胶囊点缀
                    drawCapsule(
                        center = Offset(width * 0.82f, height * 0.92f),
                        length = 36.dp.toPx(),
                        thickness = 12.dp.toPx(),
                        angleDegrees = -15f,
                        color = goldGlint.copy(alpha = 0.30f)
                    )
                }
            }
        }

        // ========== 2. 自定义背景媒体（图片/视频）：以大胶囊容器裁剪呈现 ==========
        if (bgMediaType == "image" && bgMediaUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .border(2.dp, activePrimary.copy(alpha = 0.35f), RoundedCornerShape(36.dp))
            ) {
                coil.compose.AsyncImage(
                    model = bgMediaUrl,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // 半透明遮罩保证前景内容可读
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
            }
        } else if (bgMediaType == "video" && bgMediaUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .border(2.dp, activePrimary.copy(alpha = 0.35f), RoundedCornerShape(36.dp))
            ) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.view.TextureView(ctx).apply {
                            var player: android.media.MediaPlayer? = null
                            surfaceTextureListener = object : android.view.TextureView.SurfaceTextureListener {
                                override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
                                    try {
                                        val mp = android.media.MediaPlayer()
                                        player = mp
                                        mp.setDataSource(ctx, android.net.Uri.parse(bgMediaUrl))
                                        mp.setSurface(android.view.Surface(surface))
                                        mp.isLooping = true
                                        mp.setVolume(0f, 0f)
                                        mp.setOnPreparedListener { p ->
                                            try { p.start() } catch (_: Exception) {}
                                        }
                                        mp.setOnVideoSizeChangedListener { _, vw, vh ->
                                            if (vw > 0 && vh > 0) {
                                                val viewW = width.toFloat().coerceAtLeast(1f)
                                                val viewH = height.toFloat().coerceAtLeast(1f)
                                                // v1.1.14：修复视频背景被放大裁切问题——改为 fit 模式：
                                                // 保持视频原始比例完整显示（不放大、不裁切），居中摆放
                                                val s = kotlin.math.min(viewW / vw, viewH / vh)
                                                val matrix = android.graphics.Matrix().apply {
                                                    setScale(s, s)
                                                    postTranslate((viewW - vw * s) / 2f, (viewH - vh * s) / 2f)
                                                }
                                                setTransform(matrix)
                                            }
                                        }
                                        mp.setOnErrorListener { _, _, _ ->
                                            try { mp.release() } catch (_: Exception) {}
                                            player = null
                                            true
                                        }
                                        mp.prepareAsync()
                                    } catch (_: Exception) {
                                    }
                                }

                                override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {}
                                override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
                                    try {
                                        player?.reset()
                                        player?.release()
                                    } catch (_: Exception) {}
                                    player = null
                                    return true
                                }
                                override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {}
                            }
                        }
                    },
                    onRelease = { view ->
                        // TextureView surface destruction handles release
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
            }
        }

        // ========== 3. 全局主胶囊舞台背衬（Capsule Stage Framing） ==========
        // 为整个内容区提供一个精美的全尺寸悬浮胶囊底衬，带轻柔微光轮廓与渐变玻璃感
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(36.dp))
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            activePrimary.copy(alpha = 0.38f),
                            activePrimary.copy(alpha = 0.10f),
                            activePrimary.copy(alpha = 0.24f)
                        )
                    ),
                    shape = RoundedCornerShape(36.dp)
                )
        )

        // ========== 4. 顶部摇曳胶囊氛围挂件（Cute Swaying Capsule Wind Badges） ==========
        // 挂件 1：右上角摇曳小胶囊
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-16).dp, y = 14.dp)
                .size(width = 34.dp, height = 18.dp)
                .rotate(slay1Angle)
                .alpha(0.55f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCapsule(
                    center = Offset(size.width / 2f, size.height / 2f),
                    length = size.width,
                    thickness = size.height,
                    color = activePrimary.copy(alpha = 0.70f)
                )
                drawCapsule(
                    center = Offset(size.width / 2f, size.height / 2f),
                    length = size.width,
                    thickness = size.height,
                    color = Color.White.copy(alpha = 0.60f),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
        }

        // 挂件 2：左上角摇曳微型胶囊
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 38.dp, y = 12.dp)
                .size(width = 24.dp, height = 12.dp)
                .rotate(slay2Angle)
                .alpha(0.50f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCapsule(
                    center = Offset(size.width / 2f, size.height / 2f),
                    length = size.width,
                    thickness = size.height,
                    color = activePrimary.copy(alpha = 0.60f)
                )
            }
        }

        // 挂件 3：左上极侧轻摇小胶囊
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 10.dp, y = 16.dp)
                .size(width = 28.dp, height = 14.dp)
                .rotate(slay1Angle * 0.8f)
                .alpha(0.52f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCapsule(
                    center = Offset(size.width / 2f, size.height / 2f),
                    length = size.width,
                    thickness = size.height,
                    color = activePrimary.copy(alpha = 0.55f)
                )
            }
        }

        // ========== 5. 应用前台内容层 ==========
        content()
    }
}

/**
 * 绘制任意旋转角度、支持渐变/纯色、填充/描边的几何胶囊（Capsule / Pill Shape）
 */
private fun DrawScope.drawCapsule(
    center: Offset,
    length: Float,
    thickness: Float,
    angleDegrees: Float = 0f,
    color: Color = Color.Unspecified,
    brush: Brush? = null,
    style: DrawStyle = Fill
) {
    rotate(angleDegrees, pivot = center) {
        val topLeft = Offset(center.x - length / 2f, center.y - thickness / 2f)
        val pillSize = Size(length, thickness)
        val cornerRadius = CornerRadius(thickness / 2f, thickness / 2f)
        if (brush != null) {
            drawRoundRect(
                brush = brush,
                topLeft = topLeft,
                size = pillSize,
                cornerRadius = cornerRadius,
                style = style
            )
        } else {
            drawRoundRect(
                color = color,
                topLeft = topLeft,
                size = pillSize,
                cornerRadius = cornerRadius,
                style = style
            )
        }
    }
}

/**
 * v1.2.0：动态效果主题背景（Canvas 动画，可叠加在任意颜色/渐变主题上）
 * 手绘风 / 贴纸风 / 潮流风 / 国庆风 / 新拟物风 / Q版卡通 / 极光 / 萤火 / 飘雪 / 细雨
 */
@Composable
private fun DynamicEffectBackground(
    effect: DynamicEffectPreset,
    baseColor: Color,
    accent: Color,
    windShift: Float,
    slay1Angle: Float,
    slay2Angle: Float,
    glowPulse: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        when (effect) {
            DynamicEffectPreset.HAND_DRAWN -> {
                // 手绘风：奶油底 + 手绘波浪线 + 涂鸦小圆点
                drawRect(Color(0xFFFFF8E7))
                val waveColor = Color(0xFF8B6F47).copy(alpha = 0.35f)
                for (row in 0..5) {
                    val y = size.height * (0.1f + row * 0.17f) + windShift * 12f
                    var x = 0f
                    var up = true
                    while (x < size.width) {
                        val y2 = y + (if (up) 1 else -1) * (14f + slay1Angle * 0.6f)
                        drawLine(waveColor, Offset(x, y), Offset(x + 28f, y2), strokeWidth = 2f)
                        up = !up
                        x += 28f
                    }
                }
                // 涂鸦圆点
                repeat(14) { i ->
                    val px = (i * 97 % 1000) / 1000f * size.width
                    val py = (i * 53 % 1000) / 1000f * size.height + windShift * 6f
                    drawCircle(accent.copy(alpha = 0.18f), radius = 5.dp.toPx(), center = Offset(px, py))
                }
            }
            DynamicEffectPreset.STICKER -> {
                // 贴纸风：淡彩底 + 漂浮贴纸圆/星/心
                drawRect(Color(0xFFFDF3F8))
                repeat(12) { i ->
                    val px = (i * 131 % 1000) / 1000f * size.width + windShift * 20f
                    val py = (i * 73 % 1000) / 1000f * size.height + slay2Angle * 2f
                    val r = (8 + (i % 3) * 5).dp.toPx()
                    when (i % 3) {
                        0 -> drawCircle(Color(0xFFFF9ECD).copy(alpha = 0.35f), r, Offset(px, py))
                        1 -> drawCircle(Color(0xFF7ED9F7).copy(alpha = 0.30f), r, Offset(px, py))
                        else -> {
                            // 简单四角星
                            drawCircle(Color(0xFFFFD54F).copy(alpha = 0.40f), r * 0.6f, Offset(px, py))
                        }
                    }
                }
            }
            DynamicEffectPreset.TRENDY -> {
                // 潮流风：对角撞色斜条 + 渐变
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFFE9F3), Color(0xFFE7F6FF)),
                        start = Offset.Zero, end = Offset(size.width, size.height)
                    )
                )
                val stripe = Color(0xFF7C4DFF).copy(alpha = 0.08f)
                for (i in -4..10) {
                    val x = i * 90.dp.toPx() - windShift * 40f
                    drawLine(
                        stripe,
                        Offset(x, 0f),
                        Offset(x + size.height * 0.5f, size.height),
                        strokeWidth = 26.dp.toPx()
                    )
                }
                // 撞色圆
                drawCircle(Color(0xFFFF6A88).copy(alpha = 0.22f), 60.dp.toPx() * glowPulse, Offset(size.width * 0.8f, size.height * 0.2f))
                drawCircle(Color(0xFF00B4D8).copy(alpha = 0.20f), 40.dp.toPx(), Offset(size.width * 0.15f, size.height * 0.8f))
            }
            DynamicEffectPreset.NATIONAL_DAY -> {
                // 国庆风：红金渐变 + 五角星 + 飘带
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFDE2910).copy(alpha = 0.85f), Color(0xFFB71C1C).copy(alpha = 0.9f), Color(0xFFE8A200)),
                        start = Offset.Zero, end = Offset(size.width, size.height)
                    )
                )
                // 五角星（简化圆点金光）
                repeat(10) { i ->
                    val px = (i * 61 % 1000) / 1000f * size.width
                    val py = (i * 37 % 1000) / 1000f * size.height + windShift * 10f
                    drawCircle(Color(0xFFFFD700).copy(alpha = 0.65f), (4 + i % 3).dp.toPx(), Offset(px, py))
                }
                drawCircle(Color(0xFFFFD700).copy(alpha = 0.35f), 70.dp.toPx() * glowPulse, Offset(size.width / 2f, size.height * 0.28f))
            }
            DynamicEffectPreset.NEUMORPHIC -> {
                // 新拟物风：浅灰底 + 柔和双阴影圆
                drawRect(Color(0xFFE4E9F0))
                val shadow1 = Color(0xFFC5CDD9).copy(alpha = 0.55f)
                val shadow2 = Color.White.copy(alpha = 0.7f)
                repeat(8) { i ->
                    val px = (i * 131 % 1000) / 1000f * size.width + windShift * 14f
                    val py = (i * 71 % 1000) / 1000f * size.height
                    val r = (22 + (i % 3) * 10).dp.toPx()
                    drawCircle(shadow1, r, Offset(px + 6.dp.toPx(), py + 6.dp.toPx()))
                    drawCircle(shadow2, r, Offset(px - 6.dp.toPx(), py - 6.dp.toPx()))
                    drawCircle(Color(0xFFF4F7FB), r * 0.9f, Offset(px, py))
                }
            }
            DynamicEffectPreset.Q_CARTOON -> {
                // Q版卡通风：糖果色底 + 漂浮圆点气泡
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFFF3E0), Color(0xFFE8F6FF)),
                        start = Offset.Zero, end = Offset(size.width, size.height)
                    )
                )
                val bubbleColors = listOf(
                    Color(0xFFFFB3C1).copy(alpha = 0.35f),
                    Color(0xFFBDE0FE).copy(alpha = 0.35f),
                    Color(0xFFFFE5B4).copy(alpha = 0.35f),
                    Color(0xFFB8F2E6).copy(alpha = 0.35f)
                )
                repeat(16) { i ->
                    val px = (i * 89 % 1000) / 1000f * size.width + windShift * 24f
                    val py = (i * 47 % 1000) / 1000f * size.height + slay2Angle * 3f
                    val r = (6 + (i % 4) * 4).dp.toPx()
                    drawCircle(bubbleColors[i % 4], r, Offset(px, py))
                }
            }
            DynamicEffectPreset.AURORA -> {
                // 极光：深色底 + 绿色/紫色极光波浪
                drawRect(Color(0xFF0B1026))
                val auroraColors = listOf(Color(0xFF00FF87).copy(alpha = 0.30f), Color(0xFF60EFFF).copy(alpha = 0.22f), Color(0xFF8F00FF).copy(alpha = 0.25f))
                repeat(3) { layer ->
                    val c = auroraColors[layer]
                    val baseY = size.height * (0.25f + layer * 0.18f) + windShift * 20f * (layer + 1)
                    var prev = Offset(-10f, baseY)
                    var x = 0f
                    while (x <= size.width + 10f) {
                        val y = baseY + sin(x / 90f + layer * 2f) * 24f + slay1Angle * 1.5f
                        drawLine(c, prev, Offset(x, y), strokeWidth = (30 + layer * 18).dp.toPx())
                        prev = Offset(x, y)
                        x += 12f
                    }
                }
                // 星星
                repeat(20) { i ->
                    val px = (i * 37 % 1000) / 1000f * size.width
                    val py = (i * 83 % 1000) / 1000f * size.height * 0.5f
                    drawCircle(Color.White.copy(alpha = 0.4f), 1.2.dp.toPx(), Offset(px, py))
                }
            }
            DynamicEffectPreset.FIREFLY -> {
                // 萤火：深色底 + 漂浮光点（呼吸）
                drawRect(Color(0xFF101C14))
                repeat(22) { i ->
                    val px = (i * 71 % 1000) / 1000f * size.width + windShift * 30f
                    val py = (i * 43 % 1000) / 1000f * size.height + slay1Angle * 4f
                    val r = (2 + (i % 3) * 1.5f).dp.toPx() * glowPulse
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFFFFE57F).copy(alpha = 0.85f), Color(0xFFFFE57F).copy(alpha = 0f)),
                            center = Offset(px, py),
                            radius = r * 3f
                        ),
                        radius = r,
                        center = Offset(px, py)
                    )
                }
            }
            DynamicEffectPreset.SNOW -> {
                // 飘雪：浅蓝底 + 飘落雪花
                drawRect(Color(0xFFF0F8FF))
                repeat(18) { i ->
                    val px = ((i * 53 % 1000) / 1000f * size.width + windShift * 40f) % size.width
                    val py = ((i * 29 % 1000) / 1000f * size.height + windShift * size.height * 0.3f) % size.height
                    val r = (2 + (i % 3)).dp.toPx()
                    drawCircle(Color.White.copy(alpha = 0.9f), r, Offset(px, py))
                    drawCircle(Color(0xFFB3D9F5).copy(alpha = 0.5f), r * 1.8f, Offset(px, py), style = Stroke(width = 1.dp.toPx()))
                }
            }
            DynamicEffectPreset.DRIZZLE -> {
                // 细雨：浅灰底 + 斜雨丝
                drawRect(Color(0xFFF2F5F8))
                val drop = Color(0xFF7FB2E5).copy(alpha = 0.45f)
                repeat(30) { i ->
                    val px = ((i * 79 % 1000) / 1000f * size.width + windShift * 60f) % size.width
                    val py = ((i * 41 % 1000) / 1000f * size.height + windShift * size.height * 0.35f) % size.height
                    drawLine(drop, Offset(px, py), Offset(px - 5.dp.toPx(), py + 14.dp.toPx()), strokeWidth = 1.5.dp.toPx())
                }
            }
            DynamicEffectPreset.NONE -> {
                drawRect(baseColor)
            }
        }
    }
}
