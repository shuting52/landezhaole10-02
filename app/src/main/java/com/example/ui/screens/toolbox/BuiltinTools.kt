package com.example.ui.screens.toolbox

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/* ====================================================================
 * v1.1.16 内置工具箱：真正可用的离线小工具（替代跳转外链的云端 tools）
 * 包含：计算器+ / 单位换算 / 数日子 / 时间计算 / 帮我做决定 /
 *       数字时钟 / 计分板 / 补光灯 / 随手记 / 极简记账
 * 全部本地运行，无需联网，无需跳转第三方网站。
 * ==================================================================== */

// ---------- 通用小件 ----------

@Composable
private fun ToolHeader(title: String, sub: String) {
    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
    Text(sub, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun NumPad(
    onDigit: (String) -> Unit,
    onDot: () -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onOp: (String) -> Unit,
    onEqual: () -> Unit
) {
    val btnColor = MaterialTheme.colorScheme.primary
    val rows = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"),
        listOf("0", ".", "C", "+")
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    val isOp = key in listOf("+", "-", "×", "÷")
                    val isClear = key == "C"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isOp) btnColor else if (isClear) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable {
                                when {
                                    isClear -> onClear()
                                    isOp -> onOp(key)
                                    key == "." -> onDot()
                                    else -> onDigit(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            key,
                            fontSize = 20.sp,
                            fontWeight = if (isOp) FontWeight.Black else FontWeight.Bold,
                            color = if (isOp) Color.White else if (isClear) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("⌫", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(2f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(btnColor)
                    .clickable(onClick = onEqual),
                contentAlignment = Alignment.Center
            ) {
                Text("=", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
    }
}

/** 1. 计算器+：带计算历史、多项求和 */
@Composable
fun CalculatorScreenView(modifier: Modifier = Modifier) {
    var expr by remember { mutableStateOf("0") }
    var result by remember { mutableStateOf("") }
    var history by remember { mutableStateListOf<String>() }

    fun evaluate(e: String): String {
        return try {
            val cleaned = e
                .replace("×", "*")
                .replace("÷", "/")
            val r = evalSimple(cleaned)
            if (r.isFinite()) (if (r == r.toLong().toDouble()) r.toLong().toString() else "%.6f".format(Locale.US, r).trimEnd('0').trimEnd('.')) else "错误"
        } catch (e2: Exception) { "错误" }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ToolHeader("计算器+", "带计算历史 · 支持多项连续运算")
        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text(expr, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 2)
                Text(result, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            }
        }
        NumPad(
            onDigit = { d -> expr = if (expr == "0") d else expr + d; result = "" },
            onDot = { if (!expr.contains(".") || expr.endsWith("+") || expr.endsWith("-") || expr.endsWith("×") || expr.endsWith("÷")) expr += "." },
            onClear = { expr = "0"; result = "" },
            onBack = { expr = if (expr.length > 1) expr.dropLast(1) else "0" },
            onOp = { op -> if (expr.isNotEmpty() && !expr.endsWith("+") && !expr.endsWith("-") && !expr.endsWith("×") && !expr.endsWith("÷")) expr += op },
            onEqual = {
                if (expr.isNotEmpty() && !expr.endsWith("+") && !expr.endsWith("-") && !expr.endsWith("×") && !expr.endsWith("÷")) {
                    result = evaluate(expr)
                    history.add(0, "$expr = $result")
                    if (history.size > 30) history.removeAt(history.size - 1)
                }
            }
        )
        if (history.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text("计算历史", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            history.take(10).forEachIndexed { _, item ->
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text(item, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }
    }
}

private fun evalSimple(s: String): Double {
    // 简易四则运算解析（支持 + - * / 与括号不做，只做从左到右的乘除优先）
    val tokens = Regex("(\\d+\\.?\\d*|[+\\-*/])").findAll(s).map { it.value }.toList()
    // 先处理乘除
    val stack = mutableListOf<String>()
    var i = 0
    while (i < tokens.size) {
        val t = tokens[i]
        if (t == "*" || t == "/") {
            val left = stack.removeAt(stack.size - 1).toDouble()
            val right = tokens[i + 1].toDouble()
            stack.add(if (t == "*") (left * right).toString() else (left / right).toString())
            i += 2
        } else {
            stack.add(t)
            i++
        }
    }
    // 再处理加减
    var total = stack[0].toDouble()
    i = 1
    while (i < stack.size) {
        val op = stack[i]
        val v = stack[i + 1].toDouble()
        total = if (op == "+") total + v else total - v
        i += 2
    }
    return total
}

/** 2. 单位换算：长度/重量/温度/面积/体积/数据 */
@Composable
fun UnitConvertScreenView(modifier: Modifier = Modifier) {
    val categories = listOf("长度", "重量", "温度", "面积", "体积")
    var cat by remember { mutableStateOf("长度") }
    // 单位表：名称 to 对基准单位的倍率
    val unitsMap = mapOf(
        "长度" to listOf("毫米" to 0.001, "厘米" to 0.01, "米" to 1.0, "千米" to 1000.0, "英寸" to 0.0254, "英尺" to 0.3048, "英里" to 1609.344),
        "重量" to listOf("毫克" to 0.001, "克" to 1.0, "千克" to 1000.0, "吨" to 1_000_000.0, "斤" to 500.0, "磅" to 453.59237, "盎司" to 28.349523),
        "温度" to listOf("摄氏度", "华氏度", "开尔文"),
        "面积" to listOf("平方厘米" to 0.0001, "平方米" to 1.0, "公顷" to 10000.0, "平方千米" to 1_000_000.0, "亩" to 666.6667, "平方英尺" to 0.092903),
        "体积" to listOf("毫升" to 0.001, "升" to 1.0, "立方米" to 1000.0, "加仑" to 3.785412, "立方英尺" to 28.316847)
    )
    val units = unitsMap[cat] ?: emptyList()
    var fromIdx by remember { mutableStateOf(0) }
    var toIdx by remember { mutableStateOf(1) }
    var input by remember { mutableStateOf("1") }
    var out by remember { mutableStateOf("") }

    fun convert() {
        val v = input.toDoubleOrNull() ?: 0.0
        if (cat == "温度") {
            out = tempConvert(units[fromIdx].first, units[toIdx].first, v)
        } else {
            val fromFactor = units[fromIdx].second
            val toFactor = units[toIdx].second
            val r = v * fromFactor / toFactor
            out = "%.6f".format(Locale.US, r).trimEnd('0').trimEnd('.')
        }
    }

    Column(modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ToolHeader("单位换算", "长度 / 重量 / 温度 / 面积 / 体积 · 本地换算")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            categories.forEach { c ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (cat == c) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .clickable { cat = c; fromIdx = 0; toIdx = minOf(1, unitsMap[c]!!.size - 1); input = "1"; out = "" }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(c, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (cat == c) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        OutlinedTextField(value = input, onValueChange = { input = it.filter { c -> c.isDigit() || c == '.' }; convert() }, label = { Text("输入数值") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        Row(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            units.forEachIndexed { idx, u ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (fromIdx == idx) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                        .border(1.dp, if (fromIdx == idx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .clickable { fromIdx = idx; convert() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(u.first, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (fromIdx == idx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                }
            }
        }
        Text("→ 转换为：", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            units.forEachIndexed { idx, u ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (toIdx == idx) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f) else Color.Transparent)
                        .border(1.dp, if (toIdx == idx) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .clickable { toIdx = idx; convert() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(u.first, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (toIdx == idx) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                }
            }
        }
        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
            Text("结果：${input} ${units[fromIdx].first} = $out ${units[toIdx].first}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
        }
    }
}

private fun tempConvert(from: String, to: String, v: Double): String {
    var c = when (from) {
        "华氏度" -> (v - 32) * 5 / 9
        "开尔文" -> v - 273.15
        else -> v
    }
    val r = when (to) {
        "华氏度" -> c * 9 / 5 + 32
        "开尔文" -> c + 273.15
        else -> c
    }
    return "%.2f".format(Locale.US, r)
}

/** 3. 数日子：生日/纪念日倒计时与已过天数 */
@Composable
fun CountdownScreenView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("lzdz_days_prefs", Context.MODE_PRIVATE) }
    var title by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<Pair<String, String>>>(loadDays(prefs)) }
    var msg by remember { mutableStateOf("") }

    fun add() {
        if (title.isBlank() || dateStr.isBlank()) { msg = "请填写名称和日期（如 2026-10-01）"; return }
        if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(dateStr)) { msg = "日期格式：2026-10-01"; return }
        val newList = items + (title to dateStr)
        items = newList
        saveDays(prefs, newList)
        title = ""; dateStr = ""; msg = "已添加 ✓"
    }

    Column(modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ToolHeader("数日子", "生日 / 纪念日倒数与已过天数 · 本地保存")
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("名称（如：生日 / 纪念日）") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = dateStr, onValueChange = { dateStr = it }, label = { Text("日期（如 2026-10-01）") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { add() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("添加日子")
        }
        if (msg.isNotBlank()) Text(msg, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        if (items.isEmpty()) {
            Text("还没有记录，添加一个生日或纪念日吧～", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            items.forEach { (t, d) ->
                val days = daysUntilToday(d)
                val text = if (days >= 0) "还有 $days 天" else "已过 ${-days} 天"
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(d, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Black, color = if (days >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

private fun loadDays(prefs: android.content.SharedPreferences): MutableList<Pair<String, String>> {
    val s = prefs.getString("days_list", "") ?: ""
    return if (s.isBlank()) mutableListOf() else s.split(";;").mapNotNull { seg ->
        val p = seg.split("::")
        if (p.size == 2) p[0] to p[1] else null
    }.toMutableList()
}

private fun saveDays(prefs: android.content.SharedPreferences, list: List<Pair<String, String>>) {
    prefs.edit().putString("days_list", list.joinToString(";;") { "${it.first}::${it.second}" }).apply()
}

private fun daysUntilToday(dateStr: String): Long {
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val target = fmt.parse(dateStr) ?: return 0
        val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
        val t = Calendar.getInstance().apply { time = target; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
        (t.time - today.time) / 86400000L
    } catch (e: Exception) { 0 }
}

/** 4. 时间计算：两日期相距年/月/天/时/分/秒 */
@Composable
fun TimeCalcScreenView(modifier: Modifier = Modifier) {
    var d1 by remember { mutableStateOf("2026-01-01") }
    var d2 by remember { mutableStateOf("2026-10-01") }
    var result by remember { mutableStateOf("") }

    fun calc() {
        result = try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val a = fmt.parse(d1) ?: return
            val b = fmt.parse(d2) ?: return
            var diff = (b.time - a.time) / 1000
            val neg = diff < 0
            if (neg) diff = -diff
            val days = diff / 86400
            val years = days / 365
            val months = days / 30
            val hours = diff / 3600
            val mins = diff / 60
            val prefix = if (neg) "较早 " else "相距 "
            "相差 $days 天\n≈ $years 年 / $months 个月\n≈ $hours 小时 / $mins 分钟 / $diff 秒"
        } catch (e: Exception) { "日期格式：2026-10-01" }
    }

    Column(modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ToolHeader("时间计算", "计算两个日期相距多少年/月/天/时/分/秒")
        OutlinedTextField(value = d1, onValueChange = { d1 = it }, label = { Text("起始日期（如 2026-01-01）") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = d2, onValueChange = { d2 = it }, label = { Text("结束日期（如 2026-10-01）") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { calc() }, modifier = Modifier.fillMaxWidth()) { Text("开始计算") }
        if (result.isNotBlank()) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                Text(result, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(14.dp))
            }
        }
    }
}

/** 5. 帮我做决定：选项列表随机抽取 */
@Composable
fun DecideScreenView(modifier: Modifier = Modifier) {
    var options by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("还没决定呢，点击按钮摇一摇～") }
    var rolling by remember { mutableStateOf(false) }

    fun pick() {
        val list = options.split("\n", ",", "，", " ").map { it.trim() }.filter { it.isNotBlank() }
        if (list.isEmpty()) { result = "请先输入至少两个选项（逗号/换行分隔）"; return }
        rolling = true
        result = list.random()
        rolling = false
    }

    Column(modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ToolHeader("帮我做决定", "选择困难终结者 · 输入选项随机抽取")
        OutlinedTextField(value = options, onValueChange = { options = it }, label = { Text("选项（用逗号或换行分隔，如：火锅,烧烤,日料）") }, modifier = Modifier.fillMaxWidth().height(120.dp))
        Button(onClick = { pick() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), modifier = Modifier.fillMaxWidth()) {
            Text(if (rolling) "思考中…" else "🎲 帮我决定")
        }
        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
            Text(result, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        }
    }
}

/** 6. 数字时钟：实时翻页时钟 */
@Composable
fun DigitalClockScreenView(modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    // 每秒刷新
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) { kotlinx.coroutines.delay(1000); now = System.currentTimeMillis() }
    }
    val fmtH = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    val fmtD = remember { SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINA) }
    Column(modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(fmtD.format(Date(now)), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1D1D29))
                .padding(vertical = 26.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(fmtH.format(Date(now)), fontSize = 44.sp, fontWeight = FontWeight.Black, color = Color(0xFF7FE0C3), fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        }
        Spacer(Modifier.height(8.dp))
        Text("沉浸式翻页时钟 · 每秒实时刷新", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** 7. 计分板：两队/多人比赛计分 */
@Composable
fun ScoreboardScreenView(modifier: Modifier = Modifier) {
    var scoreA by remember { mutableStateOf(0) }
    var scoreB by remember { mutableStateOf(0) }
    var nameA by remember { mutableStateOf("红队") }
    var nameB by remember { mutableStateOf("蓝队") }

    fun add(delta: Int, team: String) {
        if (team == "A") scoreA = (scoreA + delta).coerceAtLeast(0) else scoreB = (scoreB + delta).coerceAtLeast(0)
    }

    Column(modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ToolHeader("计分板", "乒乓球 / 篮球 / 台球 / 羽毛球 / 足球… 多人计分")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("A" to nameA to scoreA, "B" to nameB to scoreB).forEach { (pair, score) ->
                val (tag, name) = pair
                Surface(shape = RoundedCornerShape(16.dp), color = if (tag == "A") Color(0xFFE53935).copy(alpha = 0.12f) else Color(0xFF1E88E5).copy(alpha = 0.12f), modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(14.dp)) {
                        Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(score.toString(), fontSize = 40.sp, fontWeight = FontWeight.Black, color = if (tag == "A") Color(0xFFE53935) else Color(0xFF1E88E5))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(-1, 1, 2).forEach { d ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .clickable { add(d, tag) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(if (d > 0) "+$d" else "$d", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = nameA, onValueChange = { nameA = it }, label = { Text("红队名称") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = nameB, onValueChange = { nameB = it }, label = { Text("蓝队名称") }, modifier = Modifier.weight(1f))
        }
        Button(onClick = { scoreA = 0; scoreB = 0 }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("重新开始")
        }
    }
}

/** 8. 补光灯：全屏补光 + 多色光效 */
@Composable
fun FlashlightScreenView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var on by remember { mutableStateOf(false) }
    var colorHex by remember { mutableStateOf(0xFFFFFF) }
    val colors = listOf(
        0xFFFFFF to "白光", 0xFFF5E6C8 to "暖光", 0xFFFFE0B2 to "橙光",
        0xFFFFCDD2 to "粉光", 0xFFB3E5FC to "蓝光", 0xFFC8E6C9 to "绿光"
    )
    Column(modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ToolHeader("补光灯", "拍摄补光 · 内置多种光效 · 点击切换")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (on) Color(colorHex) else Color(0xFF2A2A2A))
                .clickable { on = !on },
            contentAlignment = Alignment.Center
        ) {
            Text(if (on) "点击关闭" else "点击开启补光", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (on) Color(0xFF333333) else Color.White)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colors.forEach { (c, name) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .border(2.dp, if (colorHex == c) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                            .clickable { colorHex = c; on = true }
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(name, fontSize = 9.sp)
                }
            }
        }
    }
}

/** 9. 随手记：本地记事本（打卡/记录/目标） */
@Composable
fun QuickNoteScreenView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("lzdz_notes_prefs", Context.MODE_PRIVATE) }
    var text by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(loadNotes(prefs)) }

    fun save() {
        if (text.isBlank()) return
        val fmt = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
        val newList = listOf("${fmt.format(Date())} | $text") + notes
        notes = newList
        saveNotes(prefs, newList)
        text = ""
        Toast.makeText(context, "已记录 ✓", Toast.LENGTH_SHORT).show()
    }

    Column(modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ToolHeader("随手记", "打卡 · 记录 · 目标进度 · 照片墙（本地保存）")
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("记点什么…") }, modifier = Modifier.fillMaxWidth().height(90.dp))
        Button(onClick = { save() }, modifier = Modifier.fillMaxWidth()) { Text("保存记录") }
        if (notes.isEmpty()) {
            Text("还没有记录，写一条吧～", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            notes.take(50).forEach { n ->
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                    Text(n, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
}

private fun loadNotes(prefs: android.content.SharedPreferences): List<String> =
    prefs.getString("notes_list", "")?.split(";;")?.filter { it.isNotBlank() } ?: emptyList()

private fun saveNotes(prefs: android.content.SharedPreferences, list: List<String>) {
    prefs.edit().putString("notes_list", list.joinToString(";;")).apply()
}

/** 10. 极简记账：本地收支记账 */
@Composable
fun LedgerScreenView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("lzdz_ledger_prefs", Context.MODE_PRIVATE) }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("支出") }
    var category by remember { mutableStateOf("餐饮") }
    var records by remember { mutableStateOf(loadLedger(prefs)) }
    val cats = listOf("餐饮", "交通", "购物", "娱乐", "居住", "其他")

    fun add() {
        val v = amount.toDoubleOrNull() ?: run { Toast.makeText(context, "请输入有效金额", Toast.LENGTH_SHORT).show(); return }
        val fmt = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
        val newList = listOf("${fmt.format(Date())}|$type|$category|$v") + records
        records = newList
        saveLedger(prefs, newList)
        amount = ""
        Toast.makeText(context, "已记账 ✓", Toast.LENGTH_SHORT).show()
    }

    val total = records.mapNotNull { seg ->
        val p = seg.split("|")
        if (p.size == 4) p[0] to (p[3].toDoubleOrNull() ?: 0.0) else null
    }.fold(0.0 to 0.0) { acc, (t, v) ->
        if (t == "支出") acc.first + v to acc.second else acc.first to acc.second + v
    }

    Column(modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ToolHeader("极简记账", "本地收支记账 · 分类统计一目了然")
        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("本月支出 ¥%.2f · 收入 ¥%.2f".format(Locale.US, total.first, total.second), fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("支出", "收入").forEach { t ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (type == t) (if (t == "支出") Color(0xFFE53935) else Color(0xFF43A047)) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .clickable { type = t }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(t, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (type == t) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            cats.forEach { c ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (category == c) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                        .border(1.dp, if (category == c) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .clickable { category = c }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(c, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (category == c) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        OutlinedTextField(value = amount, onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("金额（元）") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        Button(onClick = { add() }, colors = ButtonDefaults.buttonColors(containerColor = if (type == "支出") Color(0xFFE53935) else Color(0xFF43A047)), modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("记一笔")
        }
        if (records.isEmpty()) {
            Text("还没有账目，记一笔吧～", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            records.take(30).forEach { seg ->
                val p = seg.split("|")
                if (p.size == 4) {
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${p[1]} · ${p[2]}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(p[0], fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(if (p[1] == "支出") "-¥" else "+¥" + p[3], fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (p[1] == "支出") Color(0xFFE53935) else Color(0xFF43A047))
                        }
                    }
                }
            }
        }
    }
}

private fun loadLedger(prefs: android.content.SharedPreferences): List<String> =
    prefs.getString("ledger_list", "")?.split(";;")?.filter { it.isNotBlank() } ?: emptyList()

private fun saveLedger(prefs: android.content.SharedPreferences, list: List<String>) {
    prefs.edit().putString("ledger_list", list.joinToString(";;")).apply()
}
