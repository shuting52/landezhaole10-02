package com.example.ui.screens.toolbox

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * v1.1.18 新工具：雨声助眠
 *
 * - 9 种真实环境音效（细雨/雷雨/海浪/森林/风声/篝火/溪流/夜晚/蛤蟆）：
 *   AudioTrack 实时合成逼真白噪声/粉噪声 + 自然包络调制（无需音频素材）
 * - 自定义导入音频：文件选择器选本地音频，MediaPlayer 循环播放
 * - 音量调节 / 定时播放（到时自动停止）
 * - 智能睡眠检测：屏幕熄灭持续 60 秒视为已入睡，自动停止播放（省电）
 */

private const val SR = 44100

private data class SoundPreset(val id: String, val name: String, val icon: String)

private val SoundPresets = listOf(
    SoundPreset("rain", "细雨", "?"),
    SoundPreset("thunder", "雷雨", "⚡"),
    SoundPreset("ocean", "海浪", "?"),
    SoundPreset("forest", "森林", "?"),
    SoundPreset("wind", "风声", "?"),
    SoundPreset("campfire", "篝火", "?"),
    SoundPreset("stream", "溪流", "?"),
    SoundPreset("night", "夜晚", "?"),
    SoundPreset("frog", "蛤蟆", "?"),
)

/** 合成音效引擎 */
private class SleepSoundEngine {
    private var track: AudioTrack? = null
    private var player: MediaPlayer? = null

    fun startSynthetic(type: String) {
        stopAll()
        val pcm = generateSample(type)
        if (pcm.isEmpty()) return
        val minBuf = AudioTrack.getMinBufferSize(SR, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val t = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SR)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(minBuf, pcm.size * 2))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        t.write(pcm, 0, pcm.size)
        t.setLoopPoints(0, pcm.size, -1) // 无限循环
        t.play()
        track = t
    }

    fun startCustom(context: Context, uri: Uri) {
        stopAll()
        try {
            val mp = MediaPlayer()
            mp.setDataSource(context, uri)
            mp.isLooping = true
            mp.prepare()
            mp.start()
            player = mp
        } catch (e: Exception) {
            Toast.makeText(context, "音频加载失败：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun setVolume(v: Float) {
        track?.setVolume(v)
        player?.setVolume(v, v)
    }

    fun stopAll() {
        try { track?.stop() } catch (_: Exception) {}
        try { track?.release() } catch (_: Exception) {}
        track = null
        try { player?.stop() } catch (_: Exception) {}
        try { player?.release() } catch (_: Exception) {}
        player = null
    }

    /** 生成 10 秒环境音效 PCM */
    private fun generateSample(type: String): ShortArray {
        val n = SR * 10
        val out = ShortArray(n)
        val rnd = Random(42)
        when (type) {
            "rain" -> rain(out, rnd, light = true)
            "thunder" -> thunder(out, rnd)
            "ocean" -> ocean(out, rnd)
            "forest" -> forest(out, rnd)
            "wind" -> wind(out, rnd)
            "campfire" -> campfire(out, rnd)
            "stream" -> stream(out, rnd)
            "night" -> night(out, rnd)
            "frog" -> frog(out, rnd)
        }
        return out
    }

    // ============ 各音效合成（v1.1.24 重写，更接近真实环境音） ============
    private fun rain(out: ShortArray, rnd: Random, light: Boolean) {
        // 真实雨声 ≈ 粉噪声（柔和雨幕）+ 高频雨滴颗粒 + 轻微流水
        var lp = 0f        // 一阶低通（雨幕主体）
        var lp2 = 0f       // 次级低通（低频流水底）
        var prev = 0f
        val strength = if (light) 0.30f else 0.42f
        for (i in out.indices) {
            val w = rnd.nextFloat() * 2f - 1f
            lp += 0.12f * (w - lp)
            lp2 += 0.02f * (w - lp2)
            // 高频雨滴：随机短促颗粒（比原先更密集、更真实）
            val droplet = if (rnd.nextFloat() < 0.018f) (rnd.nextFloat() * 2f - 1f) * 0.55f else 0f
            // 轻微湿滑声（相邻样本差分制造水感）
            val hiss = (w - prev) * 0.35f
            prev = w
            out[i] = ((lp * strength + lp2 * 0.10f + droplet + hiss) * 32767f)
                .coerceIn(-32767f, 32767f).toInt().toUShort().toShort()
        }
    }

    private fun thunder(out: ShortArray, rnd: Random) {
        // 雷雨 = 雨幕 + 低频隆隆 + 间歇雷爆（加强低频能量，更真实）
        var lp = 0f
        var lp2 = 0f
        var rumblePhase = 0f
        var rumbleAmp = 0.5f
        var nextThunder = 3f + rnd.nextFloat() * 6f // 秒
        var thunderActive = 0f
        var thunderFade = 0f
        for (i in out.indices) {
            val t = i / SR.toFloat()
            // 持续雨声（加强）
            val w = rnd.nextFloat() * 2f - 1f
            lp += 0.10f * (w - lp)
            lp2 += 0.03f * (w - lp2)
            // 低频隆隆（两阶叠加更厚重）
            rumblePhase += 0.5f + rnd.nextFloat() * 0.3f
            val rumble = sin(rumblePhase) * 0.35f + sin(rumblePhase * 0.37f) * 0.45f + sin(rumblePhase * 0.13f) * 0.20f
            // 雷声爆发
            if (t > nextThunder) {
                thunderActive = 1f
                thunderFade = 0f
                nextThunder = t + 5f + rnd.nextFloat() * 10f
            }
            if (thunderActive > 0f) {
                thunderFade += 0.0012f
                thunderActive = (1f - thunderFade).coerceAtLeast(0f)
                val boom = sin(rnd.nextFloat() * 60f * PI.toFloat() * t) * 0.9f
                out[i] = ((lp * 0.22f + lp2 * 0.18f + rumble * rumbleAmp * 0.42f + boom * thunderActive * 0.8f) * 32767f)
                    .coerceIn(-32767f, 32767f).toInt().toUShort().toShort()
            } else {
                out[i] = ((lp * 0.22f + lp2 * 0.15f + rumble * rumbleAmp * 0.36f) * 32767f)
                    .coerceIn(-32767f, 32767f).toInt().toUShort().toShort()
            }
            if (rnd.nextFloat() < 0.02f) rumbleAmp = 0.3f + rnd.nextFloat() * 0.4f
        }
    }

    private fun ocean(out: ShortArray, rnd: Random) {
        var lp = 0f
        for (i in out.indices) {
            val t = i / SR.toFloat()
            val w = rnd.nextFloat() * 2f - 1f
            lp += 0.05f * (w - lp)
            // 海浪涨落包络：两个慢速正弦叠加
            val swell = 0.5f + 0.5f * sin(2f * PI.toFloat() * 0.09f * t + 1.3f)
            val swell2 = 0.4f + 0.6f * sin(2f * PI.toFloat() * 0.037f * t)
            val env = (swell * 0.55f + swell2 * 0.45f) * 0.55f
            out[i] = (lp * env * 32767f).coerceIn(-32767f, 32767f).toInt().toUShort().toShort()
        }
    }

    private fun forest(out: ShortArray, rnd: Random) {
        // v1.1.24：森林 = 柔和风声 + 叶沙沙 + 间歇真实鸟鸣（频率扫频啁啾）
        var lp = 0f
        var prev = 0f
        var chirpActive = 0f
        var chirpPhase = 0f
        var nextChirp = 1.2f + rnd.nextFloat() * 3f
        var chirpFreq = 3000f
        for (i in out.indices) {
            val t = i / SR.toFloat()
            val w = rnd.nextFloat() * 2f - 1f
            lp += 0.06f * (w - lp)
            val breeze = 0.6f + 0.4f * sin(2f * PI.toFloat() * 0.13f * t)
            // 叶沙沙（高频细节）
            val rustle = (w - prev) * 0.20f * breeze
            prev = w
            // 鸟鸣：频率在 2.4k-4.2k 间扫频的短啁啾，更接近真实叫声
            if (t > nextChirp) {
                chirpActive = 1f
                chirpPhase = 0f
                chirpFreq = 2400f + rnd.nextFloat() * 1800f
                nextChirp = t + 2.5f + rnd.nextFloat() * 4f
            }
            var chirp = 0f
            if (chirpActive > 0f) {
                chirpPhase += 0.05f
                val env = sin(chirpPhase * PI.toFloat()).coerceAtLeast(0f)
                val f = chirpFreq * (1f + 0.12f * sin(chirpPhase * 2.4f))
                chirp = sin(2f * PI.toFloat() * f * t) * env * 0.16f
                chirpActive -= 0.02f
            }
            out[i] = ((lp * 0.28f * breeze + rustle + chirp) * 32767f)
                .coerceIn(-32767f, 32767f).toInt().toUShort().toShort()
        }
    }

    private fun wind(out: ShortArray, rnd: Random) {
        var lp = 0f
        var phase = 0f
        for (i in out.indices) {
            val t = i / SR.toFloat()
            val w = rnd.nextFloat() * 2f - 1f
            lp += 0.03f * (w - lp)
            phase += 0.7f
            // 风声是幅度缓慢起伏的低频噪声
            val gust = 0.5f + 0.5f * sin(phase * 0.011f) * sin(phase * 0.0047f)
            out[i] = (lp * 0.45f * gust * 32767f).coerceIn(-32767f, 32767f).toInt().toUShort().toShort()
        }
    }

    private fun campfire(out: ShortArray, rnd: Random) {
        var lp = 0f
        for (i in out.indices) {
            val w = rnd.nextFloat() * 2f - 1f
            lp += 0.07f * (w - lp)
            var crackle = 0f
            if (rnd.nextFloat() < 0.012f) {
                crackle = (rnd.nextFloat() * 2f - 1f) * 1.4f // 噼啪爆音
            }
            out[i] = ((lp * 0.32f + crackle) * 32767f).coerceIn(-32767f, 32767f).toInt().toUShort().toShort()
        }
    }

    private fun stream(out: ShortArray, rnd: Random) {
        var lp = 0f
        var hp = 0f
        for (i in out.indices) {
            val w = rnd.nextFloat() * 2f - 1f
            lp += 0.12f * (w - lp)
            // 高通（简单差分）突出水流高频哗哗
            hp = w - lp
            val shimmer = 0.6f + 0.4f * sin(2f * PI.toFloat() * 0.21f * (i / SR.toFloat()))
            out[i] = ((lp * 0.12f + hp * 0.9f) * shimmer * 32767f).coerceIn(-32767f, 32767f).toInt().toUShort().toShort()
        }
    }

    private fun night(out: ShortArray, rnd: Random) {
        // v1.1.24：夜晚 = 轻柔夜风 + 蟋蟀啁啾（断续高频 + 鸣叫节奏变化，更自然）
        var lp = 0f
        for (i in out.indices) {
            val t = i / SR.toFloat()
            val w = rnd.nextFloat() * 2f - 1f
            lp += 0.05f * (w - lp)
            // 蟋蟀：4.5kHz 短脉冲串，鸣叫节奏随时间缓慢变化
            val slow = 0.62f + 0.38f * sin(2f * PI.toFloat() * 0.11f * t)
            val pulse = (sin(2f * PI.toFloat() * 4.5f * t) * 0.5f + 0.5f)
            val gate = if ((t % (1.0f - slow * 0.4f)) < (0.22f + slow * 0.1f)) 1f else 0f
            val cricket = pulse * gate * (0.10f + slow * 0.06f)
            out[i] = ((lp * 0.10f + cricket) * 32767f).coerceIn(-32767f, 32767f).toInt().toUShort().toShort()
        }
    }

    private fun frog(out: ShortArray, rnd: Random) {
        var lp = 0f
        var nextCroak = 0.8f
        var croakActive = 0f
        var croakPhase = 0f
        for (i in out.indices) {
            val t = i / SR.toFloat()
            val w = rnd.nextFloat() * 2f - 1f
            lp += 0.06f * (w - lp)
            // 蛤蟆咕咕声：低频短音 + 颤音
            if (t > nextCroak) {
                croakActive = 1f
                nextCroak = t + 0.8f + rnd.nextFloat() * 2.2f
                croakPhase = 0f
            }
            var croak = 0f
            if (croakActive > 0f) {
                croakPhase += 0.35f
                croakActive -= 0.02f
                val f = 75f + 25f * sin(croakPhase * 0.3f)
                croak = sin(2f * PI.toFloat() * f * (i / SR.toFloat())) * croakActive.coerceAtLeast(0f) * 0.5f
            }
            out[i] = ((lp * 0.08f + croak) * 32767f).coerceIn(-32767f, 32767f).toInt().toUShort().toShort()
        }
    }
}

@Composable
fun RainSoundSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf("rain") }
    var volume by remember { mutableStateOf(0.7f) }
    var timerMinutes by remember { mutableStateOf(15) }
    var playing by remember { mutableStateOf(false) }
    var customAudioName by remember { mutableStateOf<String?>(null) }
    var sleepStopped by remember { mutableStateOf(false) }

    val engine = remember { SleepSoundEngine() }

    DisposableEffect(Unit) {
        onDispose { engine.stopAll() }
    }

    // 导入音频
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            customAudioName = uri.lastPathSegment?.substringAfterLast('/') ?: "自定义音频"
            engine.startCustom(context, uri)
            engine.setVolume(volume)
            playing = true
            sleepStopped = false
        }
    }

    // 定时自动停止
    LaunchedEffect(playing, timerMinutes) {
        if (playing) {
            delay(timerMinutes * 60_000L)
            engine.stopAll()
            playing = false
            Toast.makeText(context, "定时结束，已停止播放", Toast.LENGTH_SHORT).show()
        }
    }

    // 智能睡眠检测：屏幕熄灭 60 秒自动停止
    LaunchedEffect(playing) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        var screenOffStart = 0L
        while (playing) {
            val interactive = pm.isInteractive
            if (!interactive) {
                if (screenOffStart == 0L) screenOffStart = System.currentTimeMillis()
                if (System.currentTimeMillis() - screenOffStart > 60_000) {
                    engine.stopAll()
                    playing = false
                    sleepStopped = true
                    Toast.makeText(context, "检测到您已入睡，已自动停止播放 ?", Toast.LENGTH_SHORT).show()
                    break
                }
            } else {
                screenOffStart = 0L
            }
            delay(5000)
        }
    }

    fun startPlay() {
        engine.stopAll()
        if (customAudioName != null && playing == false && selected == "custom") {
            // 无自定义 uri 记忆，回到合成
            selected = "rain"
        }
        engine.startSynthetic(selected)
        engine.setVolume(volume)
        playing = true
        sleepStopped = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 标题卡
        Surface(
            color = Color(0xFFFFFDF9).copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Color(0xFF3E7CFF).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("? 雨声助眠", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E5EAA))
                Text("9 种真实环境音效 · 自定义导入 · 定时 · 智能睡眠检测", fontSize = 11.sp, color = Color(0xFF5A7A9A), modifier = Modifier.padding(top = 2.dp))
            }
        }

        // 环境音效选择
        Text("环境音效", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A7A9A))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SoundPresets.size) { idx ->
                val s = SoundPresets[idx]
                FilterChip(
                    selected = selected == s.id,
                    onClick = {
                        selected = s.id
                        customAudioName = null
                        if (playing) { engine.stopAll(); engine.startSynthetic(s.id); engine.setVolume(volume) }
                    },
                    label = { Text("${s.icon} ${s.name}") }
                )
            }
        }

        // 自定义导入
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { audioPicker.launch(arrayOf("audio/*", "application/ogg", "application/mp3")) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("导入音频", fontWeight = FontWeight.Bold)
            }
            if (customAudioName != null) {
                Surface(
                    color = Color(0xFFEAF3FF),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.4f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Text("? ", fontSize = 14.sp)
                        Text(customAudioName!!, fontSize = 11.sp, maxLines = 1, color = Color(0xFF2E5EAA))
                    }
                }
            }
        }

        // 音量
        Text("音量", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A7A9A))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("?", fontSize = 14.sp)
            Slider(
                value = volume,
                onValueChange = { volume = it; engine.setVolume(it) },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
            Text("?", fontSize = 14.sp)
            Text("${(volume * 100).toInt()}%", fontSize = 12.sp, color = Color(0xFF2E5EAA), modifier = Modifier.padding(start = 8.dp))
        }

        // 定时
        Text("定时播放（到时自动停止）", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A7A9A))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf(5, 15, 30, 60, 90)) { mins ->
                FilterChip(
                    selected = timerMinutes == mins,
                    onClick = { timerMinutes = mins },
                    label = { Text("$mins 分钟") }
                )
            }
        }

        // 播放控制
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { startPlay() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5EAA)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text(if (playing) "重新播放" else "开始播放", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { engine.stopAll(); playing = false },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Stop, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text("停止", fontWeight = FontWeight.Bold)
            }
        }

        // 状态提示
        Surface(
            color = if (playing) Color(0xFFEAF3FF) else Color(0xFFF5F7FA),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when {
                    sleepStopped -> "? 智能睡眠检测：屏幕熄灭超时，已自动停止（省电）"
                    playing -> "? 正在播放：${SoundPresets.firstOrNull { it.id == selected }?.name ?: "自定义音频"} · 定时 $timerMinutes 分钟 · 屏幕熄灭 60 秒后自动停止"
                    else -> "? 待机中：选择音效后点击「开始播放」；导入音频可直接播放自定义助眠音"
                },
                fontSize = 11.sp,
                color = Color(0xFF5A7A9A),
                lineHeight = 16.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
