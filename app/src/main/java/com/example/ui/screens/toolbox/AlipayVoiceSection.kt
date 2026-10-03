package com.example.ui.screens.toolbox

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * v1.1.18 优化：支付宝 / 微信到账语音
 *
 * - 真实到账提示音：内置合成的「叮咚 + 金币叮当」钱币碰撞音效（AudioTrack 实时生成 PCM），
 *   播放完提示音后再用系统语音播报到账金额，接近真实收款提示音体验
 * - 双模式：支付宝到账 / 微信到账（微信支付模拟）
 * - 支持自定义金额 / 快捷金额 / 播报预览 / 停止
 */

private const val SAMPLE_RATE = 44100

/** 合成钱币提示音播放器（v1.1.24 重做：多泛音叮咚 + 金属质感金币碰撞 + 轻微混响尾音） */
private class CoinSoundPlayer(private val onDone: (() -> Unit)? = null) {

    private fun generatePcm(): ShortArray {
        val totalSec = 2.2
        val n = (SAMPLE_RATE * totalSec).toInt()
        val out = ShortArray(n)
        val rnd = Random(System.currentTimeMillis())
        // ---- 1. 开头「叮咚」双音（更接近真实收款提示音：主音 + 2 个泛音 + 混响尾音）----
        val dingFreq = 1318.5f // E6 主音
        val dongFreq = 987.77f // B5 主音
        addTone(out, dingFreq, 0.02f, 0.24f, 0.90f, decay = 9f)
        addTone(out, dingFreq * 2.0f, 0.02f, 0.18f, 0.30f, decay = 12f)  // 泛音1
        addTone(out, dingFreq * 3.01f, 0.02f, 0.12f, 0.12f, decay = 16f) // 泛音2
        addTone(out, dingFreq * 0.5f, 0.02f, 0.30f, 0.10f, decay = 6f)  // 泛音3（低频体）
        addTone(out, dongFreq, 0.27f, 0.34f, 0.78f, decay = 7f)
        addTone(out, dongFreq * 2.0f, 0.27f, 0.24f, 0.26f, decay = 10f)
        addTone(out, dongFreq * 3.0f, 0.27f, 0.16f, 0.10f, decay = 14f)
        addTone(out, dongFreq * 0.5f, 0.27f, 0.38f, 0.09f, decay = 5f)

        // ---- 2. 金币碰撞声（5-8 枚清脆硬币：随机频率 + 双泛音 + 长混响尾）----
        var t = 0.68f
        val coinCount = 5 + rnd.nextInt(3)
        repeat(coinCount) { i ->
            val freq = 1900f + rnd.nextFloat() * 1900f
            val dur = 0.05f + rnd.nextFloat() * 0.05f
            val vol = 0.52f - i * 0.045f
            addTone(out, freq, t, dur, vol, decay = 16f)
            addTone(out, freq * 2.01f, t, dur * 0.55f, vol * 0.45f, decay = 20f)
            addTone(out, freq * 2.98f, t, dur * 0.35f, vol * 0.18f, decay = 24f)
            // 混响尾音：主音延迟 0.02s 后衰减重放，制造空间感
            addTone(out, freq * 0.99f, t + 0.018f, dur * 1.8f, vol * 0.30f, decay = 9f)
            t += dur + 0.04f + rnd.nextFloat() * 0.035f
        }
        // 收尾一声更响的金币（模仿硬币落台面）
        addTone(out, 2500f + rnd.nextFloat() * 800f, t, 0.10f, 0.50f, decay = 14f)
        addTone(out, 2500f * 2.01f, t, 0.06f, 0.22f, decay = 18f)
        return out
    }

    private fun addTone(
        out: ShortArray,
        freq: Float,
        startSec: Float,
        durSec: Float,
        vol: Float,
        decay: Float
    ) {
        val start = (startSec * SAMPLE_RATE).toInt().coerceAtLeast(0)
        val len = (durSec * SAMPLE_RATE).toInt().coerceAtMost(out.size - start)
        if (len <= 0) return
        for (i in 0 until len) {
            val t = i.toFloat() / SAMPLE_RATE
            val env = exp(-decay * t) * vol
            val v = sin(2f * PI.toFloat() * freq * t) * env * 32767f
            val idx = start + i
            val mixed = out[idx] + v.toInt()
            out[idx] = mixed.coerceIn(-32767, 32767).toUShort().toShort()
        }
    }

    fun play() {
        Thread {
            val pcm = generatePcm()
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBuf, pcm.size * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            try {
                track.write(pcm, 0, pcm.size)
                track.play()
                val playMs = (pcm.size.toFloat() / SAMPLE_RATE * 1000f).toLong()
                Thread.sleep(playMs)
            } catch (_: Exception) {
            } finally {
                try { track.stop() } catch (_: Exception) {}
                try { track.release() } catch (_: Exception) {}
            }
            Handler(Looper.getMainLooper()).post { onDone?.invoke() }
        }.start()
    }
}

@Composable
fun AlipayVoiceSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("alipay") } // alipay / wechat

    // TTS 引擎
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun ensureTts(onReady: () -> Unit) {
        if (tts != null && ttsReady) {
            onReady()
            return
        }
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.CHINA
                // v1.1.24：调低音调、语速自然，更像真人到账播报
                engine?.setSpeechRate(0.98f)
                engine?.setPitch(0.92f)
                tts = engine
                ttsReady = true
                onReady()
            } else {
                Toast.makeText(context, "语音引擎初始化失败", Toast.LENGTH_SHORT).show()
                playing = false
            }
        }
        tts = engine
    }

    fun play() {
        val amt = amount.trim()
        if (amt.isEmpty()) {
            Toast.makeText(context, "请先输入到账金额", Toast.LENGTH_SHORT).show()
            return
        }
        val text = buildSpeechText(amt, mode)
        if (text == null) {
            Toast.makeText(context, "金额格式不正确", Toast.LENGTH_SHORT).show()
            return
        }
        playing = true
        // 先播「叮咚 + 金币」真实钱声，播完再语音播报
        CoinSoundPlayer(onDone = {
            ensureTts {
                tts?.stop()
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "arrival_voice")
                // 播报完恢复按钮
                Handler(Looper.getMainLooper()).postDelayed({
                    playing = false
                }, (text.length * 220L).coerceAtLeast(1500L))
            }
        }).play()
    }

    fun stop() {
        tts?.stop()
        playing = false
        Toast.makeText(context, "已停止播报", Toast.LENGTH_SHORT).show()
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
            border = BorderStroke(1.dp, Color(0xFF1677FF).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("? 到账语音模拟", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF1677FF))
                Text("支付宝/微信到账 · 叮咚+金币钱声 · 语音播报", fontSize = 11.sp, color = Color(0xFF5A6B7B), modifier = Modifier.padding(top = 2.dp))
            }
        }

        // 模式切换：支付宝 / 微信
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mode == "alipay",
                onClick = { mode = "alipay" },
                label = { Text("? 支付宝到账") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = mode == "wechat",
                onClick = { mode = "wechat" },
                label = { Text("? 微信到账") },
                modifier = Modifier.weight(1f)
            )
        }

        // 金额输入
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' }.take(9) },
            label = { Text("到账金额（元）") },
            prefix = { Text("¥ ", fontWeight = FontWeight.Black) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // 播报预览
        Surface(
            color = Color(0xFFEFF8FF),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 18.dp)
            ) {
                Text(
                    text = buildSpeechText(amount.trim(), mode) ?: "输入金额后自动生成播报文案",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1677FF),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("—— 播报预览（含叮咚+金币提示音）——", fontSize = 10.sp, color = Color(0xFF8AA0B3))
            }
        }

        // 快捷金额
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("1", "8.8", "50", "100", "520", "1314").forEach { v ->
                Surface(
                    color = if (amount == v) Color(0xFF1677FF) else Color(0xFFF0F6FF),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (amount == v) Color(0xFF1677FF) else Color(0xFFD6E6F8)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { amount = v }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("¥$v", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (amount == v) Color.White else Color(0xFF1677FF))
                    }
                }
            }
        }

        // 播报 / 停止
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { play() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1677FF)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text(if (playing) "播报中…" else "播放到账语音", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { stop() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Stop, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text("停止", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            "提示：播放顺序为「叮咚 → 金币叮当 → 语音播报金额」；\n支付宝模式播「支付宝到账 X 元」，微信模式播「微信到账 X 元」。\n适合商家收款提示、整蛊朋友等场景～",
            fontSize = 11.sp,
            color = Color(0xFF8AA0B3),
            lineHeight = 16.sp
        )
    }
}

/** 生成播报文案（按模式） */
private fun buildSpeechText(amountStr: String, mode: String): String? {
    val s = amountStr.trim()
    if (s.isEmpty()) return null
    val amount = try {
        BigDecimal(s)
    } catch (e: Exception) { return null }
    if (amount <= BigDecimal.ZERO) return null
    if (amount > BigDecimal("99999999.99")) return null
    val plain = amount.stripTrailingZeros().toPlainString()
    return if (mode == "wechat") {
        "微信到账，$plain 元"
    } else {
        "支付宝到账，$plain 元"
    }
}
