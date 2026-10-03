package com.example.ui.screens.toolbox

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * v1.1.18 新工具：环境检测（ping0.cc/env）
 *
 * 内嵌 WebView 加载 https://ping0.cc/env，检测当前网络环境：
 * IP 地址 / 地理位置 / 运营商 / 代理 & VPN / 网络类型 等，
 * 支持返回 / 刷新。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EnvCheckSection(modifier: Modifier = Modifier) {
    var webView by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 标题卡
        Surface(
            color = Color(0xFFFFFDF9).copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.55f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("? 环境检测 · ping0.cc", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF6C5CE7))
                Text("IP 地址 · 地理位置 · 代理/VPN · 网络类型 一键检测", fontSize = 11.sp, color = Color(0xFF7A6A9A), modifier = Modifier.padding(top = 2.dp))
            }
        }

        // 工具栏：返回 / 刷新
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF3F0FF),
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = { webView?.goBack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = Color(0xFF6C5CE7))
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF3F0FF),
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = { webView?.reload() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "刷新", tint = Color(0xFF6C5CE7))
                }
            }
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "ping0.cc/env",
                fontSize = 11.sp,
                color = Color(0xFF9A8FC0),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(3f)
            )
        }

        // WebView 内嵌检测页
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    webViewClient = WebViewClient()
                    loadUrl("https://ping0.cc/env")
                    webView = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        )
    }
}
