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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * v1.1.18 优化：车牌摇号
 *
 * - 城市选择：覆盖全国 34 个省级行政区（省份菜单 → 城市菜单），点击菜单栏形式呈现
 * - 类型选择：燃油蓝牌 / 新能源绿牌（点击菜单栏形式）
 * - 号牌池：按设置生成 10 个候选号牌，可刷新
 * - 开始摇号：号牌快速滚动动画，最后定格中签号牌
 */

/** 全国城市数据：省份简称 → (省份名, 城市列表[城市名, 字母]) */
private val NationalCityData: List<Pair<String, Pair<String, List<Pair<String, String>>>>> = listOf(
    "京" to ("北京" to listOf("北京" to "A")),
    "津" to ("天津" to listOf("天津" to "A")),
    "冀" to ("河北" to listOf("石家庄" to "A", "唐山" to "B", "秦皇岛" to "C", "邯郸" to "D", "邢台" to "E", "保定" to "F", "张家口" to "G", "承德" to "H", "沧州" to "J", "廊坊" to "R", "衡水" to "T")),
    "晋" to ("山西" to listOf("太原" to "A", "大同" to "B", "阳泉" to "C", "长治" to "D", "晋城" to "E", "朔州" to "F", "忻州" to "H", "吕梁" to "J", "晋中" to "K", "临汾" to "L", "运城" to "M")),
    "蒙" to ("内蒙古" to listOf("呼和浩特" to "A", "包头" to "B", "乌海" to "C", "赤峰" to "D", "通辽" to "G", "鄂尔多斯" to "K", "呼伦贝尔" to "E", "巴彦淖尔" to "L", "乌兰察布" to "J")),
    "辽" to ("辽宁" to listOf("沈阳" to "A", "大连" to "B", "鞍山" to "C", "抚顺" to "D", "本溪" to "E", "丹东" to "F", "锦州" to "G", "营口" to "H", "阜新" to "J", "辽阳" to "K", "盘锦" to "L", "铁岭" to "M", "朝阳" to "N", "葫芦岛" to "P")),
    "吉" to ("吉林" to listOf("长春" to "A", "吉林" to "B", "四平" to "C", "辽源" to "D", "通化" to "E", "白山" to "F", "松原" to "G", "白城" to "H", "延边" to "H")),
    "黑" to ("黑龙江" to listOf("哈尔滨" to "A", "齐齐哈尔" to "B", "牡丹江" to "C", "佳木斯" to "D", "大庆" to "E", "伊春" to "F", "鸡西" to "G", "鹤岗" to "H", "双鸭山" to "J", "七台河" to "K", "绥化" to "M", "黑河" to "N", "大兴安岭" to "P")),
    "沪" to ("上海" to listOf("上海" to "A")),
    "苏" to ("江苏" to listOf("南京" to "A", "无锡" to "B", "徐州" to "C", "常州" to "D", "苏州" to "E", "南通" to "F", "连云港" to "G", "淮安" to "H", "盐城" to "J", "扬州" to "K", "镇江" to "L", "泰州" to "M", "宿迁" to "N")),
    "浙" to ("浙江" to listOf("杭州" to "A", "宁波" to "B", "温州" to "C", "绍兴" to "D", "湖州" to "E", "嘉兴" to "F", "金华" to "G", "衢州" to "H", "台州" to "J", "丽水" to "K", "舟山" to "L")),
    "皖" to ("安徽" to listOf("合肥" to "A", "芜湖" to "B", "蚌埠" to "C", "淮南" to "D", "马鞍山" to "E", "淮北" to "F", "铜陵" to "G", "安庆" to "H", "黄山" to "J", "滁州" to "M", "阜阳" to "K", "宿州" to "L", "六安" to "N", "亳州" to "S", "池州" to "R", "宣城" to "P")),
    "闽" to ("福建" to listOf("福州" to "A", "莆田" to "B", "泉州" to "C", "厦门" to "D", "漳州" to "E", "龙岩" to "F", "三明" to "G", "南平" to "H", "宁德" to "J")),
    "赣" to ("江西" to listOf("南昌" to "A", "赣州" to "B", "宜春" to "C", "吉安" to "D", "上饶" to "E", "抚州" to "F", "九江" to "G", "景德镇" to "H", "萍乡" to "J", "新余" to "K", "鹰潭" to "L")),
    "鲁" to ("山东" to listOf("济南" to "A", "青岛" to "B", "淄博" to "C", "枣庄" to "D", "东营" to "E", "烟台" to "F", "潍坊" to "G", "济宁" to "H", "泰安" to "J", "威海" to "K", "日照" to "L", "临沂" to "Q", "德州" to "N", "聊城" to "P", "滨州" to "M", "菏泽" to "R")),
    "豫" to ("河南" to listOf("郑州" to "A", "开封" to "B", "洛阳" to "C", "平顶山" to "D", "安阳" to "E", "鹤壁" to "F", "新乡" to "G", "焦作" to "H", "濮阳" to "J", "许昌" to "K", "漯河" to "L", "三门峡" to "M", "南阳" to "R", "商丘" to "N", "信阳" to "S", "周口" to "P", "驻马店" to "Q")),
    "鄂" to ("湖北" to listOf("武汉" to "A", "黄石" to "B", "十堰" to "C", "荆州" to "D", "宜昌" to "E", "襄阳" to "F", "鄂州" to "G", "荆门" to "H", "孝感" to "K", "黄冈" to "J", "咸宁" to "L", "随州" to "S")),
    "湘" to ("湖南" to listOf("长沙" to "A", "株洲" to "B", "湘潭" to "C", "衡阳" to "D", "邵阳" to "E", "岳阳" to "F", "常德" to "J", "张家界" to "G", "益阳" to "H", "郴州" to "L", "永州" to "M", "怀化" to "N", "娄底" to "K", "湘西" to "U")),
    "粤" to ("广东" to listOf("广州" to "A", "深圳" to "B", "珠海" to "C", "汕头" to "D", "佛山" to "E", "韶关" to "F", "湛江" to "G", "肇庆" to "H", "江门" to "J", "茂名" to "K", "惠州" to "L", "梅州" to "M", "汕尾" to "N", "河源" to "P", "阳江" to "Q", "清远" to "R", "东莞" to "S", "中山" to "T", "潮州" to "U", "揭阳" to "V", "云浮" to "W")),
    "桂" to ("广西" to listOf("南宁" to "A", "柳州" to "B", "桂林" to "C", "梧州" to "D", "北海" to "E", "防城港" to "P", "钦州" to "N", "贵港" to "R", "玉林" to "K", "百色" to "L", "贺州" to "J", "河池" to "M", "来宾" to "G", "崇左" to "F")),
    "琼" to ("海南" to listOf("海口" to "A", "三亚" to "B", "琼海" to "C", "儋州" to "F")),
    "渝" to ("重庆" to listOf("重庆" to "A")),
    "川" to ("四川" to listOf("成都" to "A", "绵阳" to "B", "自贡" to "C", "攀枝花" to "D", "泸州" to "E", "德阳" to "F", "广元" to "H", "遂宁" to "J", "内江" to "K", "乐山" to "L", "资阳" to "M", "宜宾" to "Q", "南充" to "R", "达州" to "S", "雅安" to "T", "阿坝" to "U", "甘孜" to "V", "凉山" to "W", "广安" to "X", "巴中" to "Y", "眉山" to "Z")),
    "贵" to ("贵州" to listOf("贵阳" to "A", "六盘水" to "B", "遵义" to "C", "铜仁" to "D", "黔西南" to "E", "毕节" to "F", "安顺" to "G", "黔东南" to "H", "黔南" to "J")),
    "云" to ("云南" to listOf("昆明" to "A", "曲靖" to "D", "玉溪" to "F", "保山" to "M", "昭通" to "C", "丽江" to "P", "普洱" to "J", "临沧" to "S", "楚雄" to "E", "红河" to "G", "文山" to "H", "西双版纳" to "K", "大理" to "L", "德宏" to "N", "怒江" to "Q", "迪庆" to "R")),
    "藏" to ("西藏" to listOf("拉萨" to "A", "昌都" to "B", "山南" to "C", "日喀则" to "D", "那曲" to "E", "阿里" to "F", "林芝" to "G")),
    "陕" to ("陕西" to listOf("西安" to "A", "铜川" to "B", "宝鸡" to "C", "咸阳" to "D", "渭南" to "E", "汉中" to "F", "安康" to "G", "商洛" to "H", "延安" to "J", "榆林" to "K", "杨凌" to "V")),
    "甘" to ("甘肃" to listOf("兰州" to "A", "嘉峪关" to "B", "金昌" to "C", "白银" to "D", "天水" to "E", "酒泉" to "F", "张掖" to "G", "武威" to "H", "定西" to "J", "陇南" to "K", "平凉" to "L", "庆阳" to "M", "临夏" to "N", "甘南" to "P")),
    "青" to ("青海" to listOf("西宁" to "A", "海东" to "B", "海北" to "C", "黄南" to "D", "海南" to "E", "果洛" to "F", "玉树" to "G", "海西" to "H")),
    "宁" to ("宁夏" to listOf("银川" to "A", "石嘴山" to "B", "吴忠" to "C", "固原" to "D", "中卫" to "E")),
    "新" to ("新疆" to listOf("乌鲁木齐" to "A", "克拉玛依" to "B", "吐鲁番" to "K", "哈密" to "L", "昌吉" to "B", "博尔塔拉" to "E", "巴音郭楞" to "M", "阿克苏" to "N", "喀什" to "Q", "和田" to "R", "伊犁" to "F", "塔城" to "G", "阿勒泰" to "H", "石河子" to "C")),
    "港" to ("香港" to listOf("香港" to "A")),
    "澳" to ("澳门" to listOf("澳门" to "A")),
    "台" to ("台湾" to listOf("台北" to "A", "高雄" to "B", "台中" to "C", "台南" to "D"))
)

private val BlueChars = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ"

private fun genPlate(province: String, cityLetter: String, isNewEnergy: Boolean): String {
    val prefix = province + cityLetter
    return if (isNewEnergy) {
        val second = if (Random.nextBoolean()) "D" else "F"
        val rest = buildString { repeat(5) { append(BlueChars[Random.nextInt(BlueChars.length)]) } }
        prefix + second + rest
    } else {
        val first = BlueChars[Random.nextInt(2, BlueChars.length)]
        val rest = buildString { repeat(4) { append(BlueChars[Random.nextInt(10)]) } }
        prefix + first + rest
    }
}

@Composable
fun LicensePlateSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isUnit by remember { mutableStateOf(false) }
    var isNewEnergy by remember { mutableStateOf(false) }

    // 省份/城市选择
    var selectedProvinceIdx by remember { mutableStateOf(10) } // 默认粤
    var selectedCityIdx by remember { mutableStateOf(0) }
    var provinceMenuOpen by remember { mutableStateOf(false) }
    var cityMenuOpen by remember { mutableStateOf(false) }
    var typeMenuOpen by remember { mutableStateOf(false) }
    var identityMenuOpen by remember { mutableStateOf(false) }

    val currentProvince = NationalCityData[selectedProvinceIdx]
    val currentCity = currentProvince.second.second[selectedCityIdx.coerceAtMost(currentProvince.second.second.size - 1)]

    var pool by remember { mutableStateOf((0 until 10).map { genPlate("粤", "A", false) }) }
    var rolling by remember { mutableStateOf(false) }
    var rollingPlate by remember { mutableStateOf("") }
    var finalPlate by remember { mutableStateOf<String?>(null) }

    fun regeneratePool() {
        pool = (0 until 10).map { genPlate(currentProvince.first, currentCity.second, isNewEnergy) }
    }

    fun selectProvince(idx: Int) {
        selectedProvinceIdx = idx
        selectedCityIdx = 0
        provinceMenuOpen = false
        cityMenuOpen = false
        regeneratePool()
    }

    fun selectCity(idx: Int) {
        selectedCityIdx = idx
        cityMenuOpen = false
        regeneratePool()
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
                Text("? 车牌摇号", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFFDE2910))
                Text("全国城市 · 菜单选择 · 号牌池 · 一键摇号", fontSize = 11.sp, color = Color(0xFF7A4A45), modifier = Modifier.padding(top = 2.dp))
            }
        }

        // 选号设置：身份（菜单栏）
        Text("身份", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A4A45))
        Box {
            OutlinedTextField(
                value = if (isUnit) "单位 ?" else "个人 ?",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                label = { Text("选择身份") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { identityMenuOpen = true }
            )
            DropdownMenu(expanded = identityMenuOpen, onDismissRequest = { identityMenuOpen = false }) {
                DropdownMenuItem(text = { Text("个人 ?") }, onClick = { isUnit = false; identityMenuOpen = false })
                DropdownMenuItem(text = { Text("单位 ?") }, onClick = { isUnit = true; identityMenuOpen = false })
            }
        }

        // 选号设置：省份（菜单栏）
        Text("省份", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A4A45))
        Box {
            OutlinedTextField(
                value = "${currentProvince.second.first} ${currentProvince.first}",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                label = { Text("选择省份（全国 34 省级）") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { provinceMenuOpen = true }
            )
            DropdownMenu(
                expanded = provinceMenuOpen,
                onDismissRequest = { provinceMenuOpen = false }
            ) {
                NationalCityData.forEachIndexed { idx, p ->
                    DropdownMenuItem(
                        text = { Text("${p.second.first} · ${p.first}", fontSize = 13.sp) },
                        onClick = { selectProvince(idx) }
                    )
                }
            }
        }

        // 选号设置：城市（菜单栏）
        Text("城市", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A4A45))
        Box {
            OutlinedTextField(
                value = "${currentCity.first} · ${currentProvince.first}${currentCity.second}",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                label = { Text("选择城市") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { cityMenuOpen = true }
            )
            DropdownMenu(expanded = cityMenuOpen, onDismissRequest = { cityMenuOpen = false }) {
                currentProvince.second.second.forEachIndexed { idx, c ->
                    DropdownMenuItem(
                        text = { Text("${c.first} · ${currentProvince.first}${c.second}", fontSize = 13.sp) },
                        onClick = { selectCity(idx) }
                    )
                }
            }
        }

        // 选号设置：类型（菜单栏）
        Text("类型", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A4A45))
        Box {
            OutlinedTextField(
                value = if (isNewEnergy) "新能源绿牌 ?" else "燃油蓝牌 ?",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                label = { Text("选择号牌类型") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { typeMenuOpen = true }
            )
            DropdownMenu(expanded = typeMenuOpen, onDismissRequest = { typeMenuOpen = false }) {
                DropdownMenuItem(text = { Text("燃油蓝牌 ?") }, onClick = { isNewEnergy = false; typeMenuOpen = false; regeneratePool() })
                DropdownMenuItem(text = { Text("新能源绿牌 ?") }, onClick = { isNewEnergy = true; typeMenuOpen = false; regeneratePool() })
            }
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
                    text = if (finalPlate != null && !rolling) "? 恭喜摇中！" else "车牌摇号 · 好运加持",
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
                    repeat(28) { i ->
                        rollingPlate = genPlate(currentProvince.first, currentCity.second, isNewEnergy)
                        delay(if (i < 20) 70L else (70 + (i - 20) * 60).toLong())
                    }
                    val picked = pool.ifEmpty { (0 until 10).map { genPlate(currentProvince.first, currentCity.second, isNewEnergy) } }.random()
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
            Text(if (rolling) "摇号中…" else "开始摇号 ?", fontWeight = FontWeight.Black, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            "说明：已覆盖全国 34 个省级行政区与主要城市，摇号结果仅供参考娱乐，实际选号以当地车管所规定为准。",
            fontSize = 11.sp,
            color = Color(0xFF9A7B6B)
        )
    }
}
