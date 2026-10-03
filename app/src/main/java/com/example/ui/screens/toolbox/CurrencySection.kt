package com.example.ui.screens.toolbox

import android.content.Context
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL

/**
 * v1.1.18 新工具：货币转换
 *
 * - 覆盖全球主要货币（50+ 种，含各国常用币种）
 * - 从/到货币自定义选择（点击菜单栏形式）
 * - 在线实时汇率（frankfurter 免费 API），失败回退内置基准汇率
 * - 支持反向换算（1 美金 = 多少人民币 / 1 人民币 = 多少美金）
 */

/** 货币列表：代码 → 名称/符号 */
private val Currencies = listOf(
    "USD" to "美元 \$", "CNY" to "人民币 ¥", "EUR" to "欧元 €", "GBP" to "英镑 £",
    "JPY" to "日元 ¥", "KRW" to "韩元 ₩", "HKD" to "港币 HK\$", "TWD" to "新台币 NT\$",
    "SGD" to "新加坡元 S\$", "AUD" to "澳元 A\$", "CAD" to "加元 C\$", "NZD" to "新西兰元 NZ\$",
    "CHF" to "瑞郎 ₣", "INR" to "印度卢比 ₹", "RUB" to "俄罗斯卢布 ₽", "BRL" to "巴西雷亚尔 R\$",
    "MXN" to "墨西哥比索 M\$", "ZAR" to "南非兰特 R", "TRY" to "土耳其里拉 ₺", "THB" to "泰铢 ฿",
    "VND" to "越南盾 ₫", "MYR" to "马来西亚林吉特 RM", "IDR" to "印尼盾 Rp", "PHP" to "菲律宾比索 ₱",
    "AED" to "阿联酋迪拉姆 د.إ", "SAR" to "沙特里亚尔 ﷼", "SEK" to "瑞典克朗 kr", "NOK" to "挪威克朗 kr",
    "DKK" to "丹麦克朗 kr", "PLN" to "波兰兹罗提 zł", "CZK" to "捷克克朗 Kč", "HUF" to "匈牙利福林 Ft",
    "RON" to "罗马尼亚列伊 lei", "ILS" to "以色列谢克尔 ₪", "EGP" to "埃及镑 £", "NGN" to "尼日利亚奈拉 ₦",
    "PKR" to "巴基斯坦卢比 ₨", "BDT" to "孟加拉塔卡 ৳", "KES" to "肯尼亚先令 Sh", "ARS" to "阿根廷比索 \$",
    "KZT" to "哈萨克坚戈 ₸", "UAH" to "乌克兰格里夫纳 ₴", "BGN" to "保加利亚列弗 лв", "HRK" to "克罗地亚库纳 kn",
    "ISK" to "冰岛克朗 kr", "LKR" to "斯里兰卡卢比 Rs", "NPR" to "尼泊尔卢比 ₨", "MAD" to "摩洛哥迪拉姆 د.م",
    "QAR" to "卡塔尔里亚尔 ﷼", "KWD" to "科威特第纳尔 د.ك", "BHD" to "巴林第纳尔 د.ب", "OMR" to "阿曼里亚尔 ر.ع"
)

/** 内置 USD 基准汇率（近似值，在线失败时兜底） */
private val FallbackRates = mapOf(
    "USD" to 1.0, "CNY" to 7.18, "EUR" to 0.92, "GBP" to 0.79, "JPY" to 150.0, "KRW" to 1350.0,
    "HKD" to 7.80, "TWD" to 32.0, "SGD" to 1.35, "AUD" to 1.53, "CAD" to 1.37, "NZD" to 1.67,
    "CHF" to 0.88, "INR" to 83.5, "RUB" to 92.0, "BRL" to 5.6, "MXN" to 18.0, "ZAR" to 18.5,
    "TRY" to 34.0, "THB" to 34.0, "VND" to 25000.0, "MYR" to 4.7, "IDR" to 16000.0, "PHP" to 58.0,
    "AED" to 3.67, "SAR" to 3.75, "SEK" to 10.5, "NOK" to 10.8, "DKK" to 6.9, "PLN" to 4.0,
    "CZK" to 23.0, "HUF" to 355.0, "RON" to 4.6, "ILS" to 3.7, "EGP" to 48.0, "NGN" to 1500.0,
    "PKR" to 278.0, "BDT" to 120.0, "KES" to 129.0, "ARS" to 970.0, "KZT" to 470.0, "UAH" to 41.0,
    "BGN" to 1.80, "HRK" to 7.0, "ISK" to 138.0, "LKR" to 300.0, "NPR" to 133.0, "MAD" to 10.0,
    "QAR" to 3.64, "KWD" to 0.31, "BHD" to 0.376, "OMR" to 0.385
)

@Composable
fun CurrencySection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fromCode by remember { mutableStateOf("USD") }
    var toCode by remember { mutableStateOf("CNY") }
    var amount by remember { mutableStateOf("1") }
    var fromMenu by remember { mutableStateOf(false) }
    var toMenu by remember { mutableStateOf(false) }
    var rates by remember { mutableStateOf(FallbackRates) }
    var usingOnline by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }

    fun convert() {
        val amt = amount.trim().toDoubleOrNull()
        if (amt == null || amt <= 0) {
            result = "请输入有效金额"
            return
        }
        val fromRate = rates[fromCode] ?: 1.0
        val toRate = rates[toCode] ?: 1.0
        val value = amt / fromRate * toRate
        val big = BigDecimal(value).setScale(2, RoundingMode.HALF_UP)
        result = "$amount ${fromCode} = $big ${toCode}"
    }

    fun fetchRates() {
        loading = true
        scope.launch {
            val fetched = withContext(Dispatchers.IO) {
                try {
                    val conn = URL("https://api.frankfurter.app/latest?from=USD").openConnection() as HttpURLConnection
                    conn.connectTimeout = 8000
                    conn.readTimeout = 8000
                    conn.setRequestProperty("User-Agent", "LazyFind")
                    val body = conn.inputStream.bufferedReader().readText()
                    val ratesObj = JSONObject(body).getJSONObject("rates")
                    val map = mutableMapOf("USD" to 1.0)
                    ratesObj.keys().forEach { k ->
                        map[k] = ratesObj.getDouble(k)
                    }
                    // 补充内置中没有的
                    FallbackRates.forEach { (k, v) -> if (!map.containsKey(k)) map[k] = v }
                    map
                } catch (_: Exception) {
                    FallbackRates
                }
            }
            rates = fetched
            usingOnline = fetched !== FallbackRates
            loading = false
            Toast.makeText(context, if (usingOnline) "已更新实时汇率" else "在线汇率获取失败，使用内置汇率", Toast.LENGTH_SHORT).show()
            convert()
        }
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
            border = BorderStroke(1.dp, Color(0xFF00B386).copy(alpha = 0.55f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("? 货币转换", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF00875A))
                Text("全球 50+ 货币 · 实时汇率 · 双向换算", fontSize = 11.sp, color = Color(0xFF5A8A7A), modifier = Modifier.padding(top = 2.dp))
            }
        }

        // 金额输入
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
            label = { Text("金额") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // 从货币选择（菜单栏）
        Text("从货币", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A8A7A))
        Box {
            OutlinedTextField(
                value = Currencies.firstOrNull { it.first == fromCode }?.second ?: fromCode,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                label = { Text("选择货币") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { fromMenu = true }
            )
            DropdownMenu(expanded = fromMenu, onDismissRequest = { fromMenu = false }) {
                // v1.1.24：菜单项太多（50+）会超出屏幕被截断，加滚动确保所有货币可选
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Currencies.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text("$name ($code)", fontSize = 13.sp) },
                            onClick = { fromCode = code; fromMenu = false; convert() }
                        )
                    }
                }
            }
        }

        // 交换按钮
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = Color(0xFFE6F7F1),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clickable {
                        val t = fromCode; fromCode = toCode; toCode = t
                        convert()
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = Color(0xFF00875A), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("交换货币", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00875A))
                }
            }
        }

        // 到货币选择（菜单栏）
        Text("到货币", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A8A7A))
        Box {
            OutlinedTextField(
                value = Currencies.firstOrNull { it.first == toCode }?.second ?: toCode,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                label = { Text("选择货币") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { toMenu = true }
            )
            DropdownMenu(expanded = toMenu, onDismissRequest = { toMenu = false }) {
                // v1.1.24：菜单项太多（50+）会超出屏幕被截断，加滚动确保所有货币可选
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Currencies.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text("$name ($code)", fontSize = 13.sp) },
                            onClick = { toCode = code; toMenu = false; convert() }
                        )
                    }
                }
            }
        }

        // 汇率信息
        Surface(
            color = Color(0xFFEAF9F3),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "1 $fromCode ≈ ${BigDecimal(rates[toCode]?.let { 1.0 / (rates[fromCode] ?: 1.0) * it } ?: 1.0).setScale(2, RoundingMode.HALF_UP)} $toCode",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00875A),
                    modifier = Modifier.weight(1f)
                )
                Text(if (usingOnline) "实时汇率" else "内置汇率", fontSize = 10.sp, color = Color(0xFF5A8A7A))
            }
        }

        // 换算结果
        Surface(
            color = Color(0xFF1E3A2E),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(2.dp, Color(0xFF2DD4A7)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 22.dp)
            ) {
                Text(
                    text = result.ifBlank { "输入金额后自动换算" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2DD4A7),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("? 实时换算结果", fontSize = 11.sp, color = Color(0xFF8FB5A8))
            }
        }

        // 操作按钮
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { convert() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00875A)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("换算", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { fetchRates() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D7D6A)),
                shape = RoundedCornerShape(14.dp),
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (loading) "更新中…" else "更新汇率", fontWeight = FontWeight.Bold)
            }
        }
    }
}
