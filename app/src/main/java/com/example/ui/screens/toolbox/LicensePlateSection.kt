package com.example.ui.screens.toolbox

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * v1.1.18 新工具：车牌摇号
 *
 * - 选号设置：身份（个人/单位）、城市（省份简称）、类型（燃油蓝牌/新能源绿牌）
 * - 号牌池：按设置生成一批候选号牌（10 选 1）
 * - 开始摇号：号牌数字快速滚动动画，最后定格中签号牌
 */
private data class PlateCity(val label: String, val province: String, val cityLetter: String)

private val Cities = listOf(
    PlateCity("北京 · 京", "京", "A"),
    PlateCity("上海 · 沪", "沪", "A"),
    PlateCity("广东 · 粤", "粤", "A"),
    PlateCity("浙江 · 浙", "浙", "A"),
    PlateCity("江苏 · 苏", "苏", "A"),
    PlateCity("四川 · 川", "川", "A"),
    PlateCity("湖北 · 鄂", "鄂", "A"),
    PlateCity("湖南 · 湘", "湘", "A"),
    PlateCity("福建 · 闽", "闽", "A"),
    PlateCity("山东 · 鲁", "鲁", "A"),
    PlateCity("河南 · 豫", "豫", "A"),
    PlateCity("陕西 · 陕", "陕", "A"),
    PlateCity("重庆 · 渝", "渝", "A"),
    PlateCity("天津 · 津", "津", "A"),
    PlateCity("河北 · 冀", "冀", "A"),
    PlateCity("辽宁 · 辽", "辽", "A")
)

private val BlueChars = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ"
private val NewChars = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ"

private fun genPlate(city: PlateCity, isNewEnergy: Boolean): String {
    val prefix = city.province + city.cityLetter
    return if (isNewEnergy) {
        // 新能源：6 位，第二位固定 D（纯电）/F（混动）
        val second = if (Random.nextBoolean()) "D" else "F"
        val rest = buildString { repeat(5) { append(NewChars[Random.nextInt(NewChars.length)]) } }
        prefix + second + rest
    } else {
        // 燃油：5 位，首字母，其余数字为主
        val first = NewChars[Random.nextInt(2, NewChars.length)]
        val rest = buildString { repeat(4) { append(NewChars[Random.nextInt(10)]) } }
        prefix + first + rest
    }
}

@Composable
fun LicensePlateSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isUnit by remember { mutableStateOf(false) }        // 身份：个人 / 单位
    var city by remember { mutableStateOf(Cities[0]) }
    var isNewEnergy by remember { mutableStateOf(false) }   // 类型：燃油 / 新能源

    var pool by remember { mutableStateOf((0 until 10).map { genPlate(Cities[0], false) }) }   // 号牌池（初始即生成）
    var rolling by remember { mutableStateOf(false) }
    var rollingPlate by remember { mutableStateOf("") }                  // 滚动中的号牌
    var finalPlate by remember { mutableStateOf<String?>(null) }         // 定格结果

    fun regeneratePool() {
        pool = (0 until 10).map { genPlate(city, isNewEnergy) }
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
            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.7f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("🚗 车牌摇号", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFFDE2910))
                Text("选号设置 · 号牌池 · 一键摇号", fontSize = 11.sp, color = Color(0xFF7A4A45), modifier = Modifier.padding(top = 2.dp))
            }
        }

        // 选号设置：身份
        Text("身份", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A4A45))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !isUnit, onClick = { isUnit = false }, label = { Text("👤 个人") }, modifier = Modifier.weight(1f))
            FilterChip(selected = isUnit, onClick = { isUnit = true }, label = { Text("🏢 单位") }, modifier = Modifier.weight(1f))
        }

        // 选号设置：城市
        Text("城市", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A4A45))
        OutlinedTextField(
            value = city.label,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("选择城市") },
            modifier = Modifier.fillMaxWidth()
        )
        // 城市横向滚动选择
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Cities.forEach { c ->
                FilterChip(
                    selected = city == c,
                    onClick = { city = c },
                    label = { Text(c.label, fontSize = 11.sp) }
                )
            }
        }

        // 选号设置：类型
        Text("类型", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A4A45))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !isNewEnergy, onClick = { isNewEnergy = false }, label = { Text("🔵 燃油蓝牌") }, modifier = Modifier.weight(1f))
            FilterChip(selected = isNewEnergy, onClick = { isNewEnergy = true }, label = { Text("🟢 新能源绿牌") }, modifier = Modifier.weight(1f))
        }

        // 号牌池
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("号牌池（10 选 1）", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF3B1F1F))
            androidx.compose.material3.TextButton(onClick = { regeneratePool() }) { Text("刷新号牌池", fontSize = 12.sp) }
        }

        // 池内号牌两列
        pool.chunked(2).forEach { rowList ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowList.forEach { p ->
                    Surface(
                        color = Color(0xFFF3F7FF),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = p,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A4FA0),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 7.dp)
                        )
                    }
                }
                if (rowList.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }

        // 摇号结果展示
        Surface(
            color = Color(0xFF1E2A4A),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(2.dp, Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 20.dp)
            ) {
                Text(
                    text = if (rolling) rollingPlate.ifBlank { "摇号中…" } else (finalPlate ?: "点击开始摇号"),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700),
                    letterSpacing = 3.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (finalPlate != null && !rolling) "🎉 恭喜摇中！" else "车牌摇号 · 好运加持",
                    fontSize = 11.sp,
                    color = Color(0xFF9FB4E8)
                )
            }
        }

        // 开始摇号按钮
        Button(
            onClick = {
                if (rolling) return@Button
                finalPlate = null
                rolling = true
                scope.launch {
                    // 滚动动画：每 90ms 换一个随机号牌，共约 2.6 秒
                    repeat(28) { i ->
                        rollingPlate = genPlate(city, isNewEnergy)
                        delay(if (i < 20) 70L else (70 + (i - 20) * 60).toLong()) // 越来越慢
                    }
                    // 从池中定格一个
                    val picked = pool.ifEmpty { (0 until 10).map { genPlate(city, isNewEnergy) } }.random()
                    finalPlate = picked
                    rolling = false
                    rollingPlate = picked
                    Toast.makeText(context, "摇中号牌：$picked", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDE2910)),
            shape = RoundedCornerShape(14.dp),
            enabled = !rolling,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.DirectionsCar, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (rolling) "摇号中…" else "开始摇号 🎲", fontWeight = FontWeight.Black, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            "说明：摇号结果仅供参考娱乐，实际选号请以当地车管所规定为准。",
            fontSize = 11.sp,
            color = Color(0xFF9A7B6B)
        )
    }
}
