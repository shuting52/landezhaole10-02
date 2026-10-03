package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * v1.1.18 优化：设置页页脚
 * - 打字机品牌标语（保持原有交互）
 * - 增加版本号展示（当前 v1.1.18）
 * - 红金主题装饰分隔 + 更精致的排版（盛世华诞配色）
 */
@Composable
fun TypewriterFooter(
    modifier: Modifier = Modifier
) {
    val fullText = "探索无限前沿 · 赋能每一个灵感"
    var charCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            for (i in 0..fullText.length) {
                charCount = i
                delay(110)
            }
            delay(2200)
            for (i in fullText.length downTo 0) {
                charCount = i
                delay(55)
            }
            delay(900)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 顶部装饰分隔线（红金）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color(0xFFFFD700).copy(alpha = 0.45f))
            )
            Text("?", fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color(0xFFDE2910).copy(alpha = 0.4f))
            )
        }

        // 打字机标语
        Surface(
            color = Color(0xFFFFF9EE),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = fullText.take(charCount),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFB8860B)
                )
                Text(
                    text = "▍",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDE2910)
                )
            }
        }

        // 版本信息卡
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "v${com.example.BuildConfig.VERSION_NAME}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFDE2910)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "盛世华诞 · 愿祖国繁荣昌盛",
                    fontSize = 10.sp,
                    color = Color(0xFFB8860B)
                )
            }
        }

        Text(
            text = "© 2026 懒得找了 · 保留所有权利",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
