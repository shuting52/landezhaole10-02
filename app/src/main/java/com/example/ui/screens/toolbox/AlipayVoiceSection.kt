package com.example.ui.screens.toolbox

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

/**
 * v1.1.18 新工具：支付宝到账语音
 *
 * - 输入金额（支持小数），实时生成「支付宝到账 XX 元」播报文案
 * - 播报预览：点击即可用系统 TTS 语音播报，金额自动中文朗读
 * - 支持自定义金额 / 暂停停止播报
 */
@Composable
fun AlipayVoiceSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }

    // TTS 引擎（延迟初始化，首次点击播报时建立）
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    var speaking by remember { mutableStateOf(false) }

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
        val engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine.language = Locale.CHINA
                engine.setSpeechRate(1.0f)
                tts = engine
                ttsReady = true
                onReady()
            } else {
                Toast.makeText(context, "语音引擎初始化失败", Toast.LENGTH_SHORT).show()
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
        val text = buildSpeechText(amt)
        if (text == null) {
            Toast.makeText(context, "金额格式不正确", Toast.LENGTH_SHORT).show()
            return
        }
        ensureTts {
            tts?.stop()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "alipay_voice")
            speaking = true
        }
    }

    fun stop() {
        tts?.stop()
        speaking = false
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
            border = BorderStroke(1.dp, Color(0xFF00A3E0).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("🔊 支付宝到账语音", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF1677FF))
                Text("输入金额 · 生成到账播报 · 一键语音朗读", fontSize = 11.sp, color = Color(0xFF5A6B7B), modifier = Modifier.padding(top = 2.dp))
            }
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
                    text = buildSpeechText(amount.trim()) ?: "输入金额后自动生成播报文案",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1677FF),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("—— 播报预览 ——", fontSize = 10.sp, color = Color(0xFF8AA0B3))
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
                        .clickableChip { amount = v }
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
                Text(if (speaking) "重新播报" else "播放到账语音", fontWeight = FontWeight.Bold)
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
            "提示：播报内容为「支付宝到账 X 元」，金额由系统语音自动中文朗读；\n适合商家收款提示、整蛊朋友等场景～",
            fontSize = 11.sp,
            color = Color(0xFF8AA0B3),
            lineHeight = 16.sp
        )
    }
}

/** 生成播报文案：支付宝到账 X 元（金额校验） */
private fun buildSpeechText(amountStr: String): String? {
    val s = amountStr.trim()
    if (s.isEmpty()) return null
    val amount = try {
        BigDecimal(s)
    } catch (e: Exception) { return null }
    if (amount <= BigDecimal.ZERO) return null
    if (amount > BigDecimal("99999999.99")) return null
    val plain = amount.stripTrailingZeros().toPlainString()
    return "支付宝到账，$plain 元"
}

/** 轻量点击包装 */
private fun Modifier.clickableChip(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
