package com.example.ui.screens.toolbox

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import com.example.data.remote.IpMonitorDto
import com.example.data.remote.SettingsDto
import com.example.data.remote.VersionDto
import com.example.ui.components.IpLocationMonitorWidget
import com.example.ui.components.UiverseAmber
import com.example.ui.components.UiverseBlue
import com.example.ui.components.UiverseInk
import com.example.ui.components.UiversePink
import com.example.ui.components.UiverseTextMuted
import com.example.ui.components.UiverseTrack
import com.example.ui.components.neoShadow
import com.example.ui.screens.openQqGroup

/**
 * v1.1.13：工具箱 · 本机云端对接分区
 * 提供与本体软件核心能力「对接」的工具（调用 NavViewModel / 本体组件）：
 *  1. 云端连接状态 —— 数据源仓库 / 在线状态 / 云端版本 vs 本地版本
 *  2. 检查更新 —— 触发本体云端版本检测（与设置页「检查更新」同源）
 *  3. 重放开屏动画 —— 调用本体开屏（SplashScreenOverlay）
 *  4. 主题切换 —— 打开本体主题库
 *  5. IP 定位监控 —— 复用首页 IP 定位组件（云端控制台可开关/配 URL）
 *  6. 官方交流群 —— 直达本体官方 QQ 群（与设置页同源，规则5锁定不变）
 * 样式：UiverseListGroup 白底 + 琥珀硬阴影 + 墨色描边（与工具箱本地工具一致）
 */
@Composable
fun AppBridgeSection(
    cloudReady: Boolean,
    cloudVersion: VersionDto?,
    cloudIpMonitor: IpMonitorDto?,
    cloudSettings: SettingsDto?,
    onCheckUpdate: () -> Unit,
    onShowSplash: () -> Unit,
    onOpenTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localVersion = com.example.BuildConfig.VERSION_NAME
    val cloudName = cloudVersion?.name?.ifBlank { "—" } ?: "—"
    val cloudCode = cloudVersion?.code ?: 0
    val localCode = com.example.BuildConfig.VERSION_CODE
    val hasNew = cloudCode > localCode

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .neoShadow(
                offsetX = 4.dp,
                offsetY = 4.dp,
                shadowColor = UiverseAmber,
                borderColor = UiverseInk,
                borderWidth = 3.5.dp,
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.White
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        // ===== 分区标题 =====
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text("🔗", fontSize = 15.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "本机 · 云端对接",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = UiverseInk
            )
            Spacer(modifier = Modifier.weight(1f))
            // 在线状态徽章
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (cloudReady) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.18f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (cloudReady) "● 云端已连接" else "○ 连接中…",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (cloudReady) Color(0xFF15803D) else Color(0xFFB45309)
                )
            }
        }

        // ===== 1. 云端连接状态（数据源 + 版本对比）=====
        AppBridgeRow(
            icon = Icons.Filled.Cloud,
            iconColor = UiverseBlue,
            title = "云端连接状态",
            subtitle = "数据源：landezhaole10-02 · 云端 v$cloudName / 本地 v$localVersion" +
                (if (hasNew) " · ⚠️ 有新版本" else " · 已是最新"),
            onClick = null
        )

        HorizontalDivider(color = UiverseTrack, thickness = 2.dp)

        // ===== 2. 检查更新 =====
        AppBridgeRow(
            icon = Icons.Filled.Refresh,
            iconColor = UiversePink,
            title = "检查更新",
            subtitle = if (hasNew) "云端发现新版本 v$cloudName，点击立即检测" else "已是最新版本 v$localVersion",
            onClick = onCheckUpdate
        )

        HorizontalDivider(color = UiverseTrack, thickness = 2.dp)

        // ===== 3. 重放开屏动画 =====
        AppBridgeRow(
            icon = Icons.Filled.RocketLaunch,
            iconColor = UiverseAmber,
            title = "重放开屏动画",
            subtitle = "再次展示粒子流光开屏（本体 SplashScreenOverlay）",
            onClick = onShowSplash
        )

        HorizontalDivider(color = UiverseTrack, thickness = 2.dp)

        // ===== 4. 主题切换 =====
        AppBridgeRow(
            icon = Icons.Filled.ColorLens,
            iconColor = UiversePink,
            title = "主题切换",
            subtitle = "打开本体主题库（国潮/新拟态等预设）",
            onClick = onOpenTheme
        )

        HorizontalDivider(color = UiverseTrack, thickness = 2.dp)

        // ===== 5. IP 定位监控（内嵌首页同款组件）=====
        AppBridgeRow(
            icon = Icons.Filled.Cloud,
            iconColor = UiverseBlue,
            title = "IP 定位监控",
            subtitle = "展示当前网络 IP 与地理位置（云端可开关）",
            onClick = null,
            content = {
                IpLocationMonitorWidget(
                    cloudIpMonitor = cloudIpMonitor,
                    modifier = Modifier.padding(start = 62.dp, end = 14.dp, bottom = 10.dp)
                )
            }
        )

        HorizontalDivider(color = UiverseTrack, thickness = 2.dp)

        // ===== 6. 官方交流群 =====
        AppBridgeRow(
            icon = Icons.Filled.Group,
            iconColor = UiverseAmber,
            title = "官方交流群",
            subtitle = "直达本体官方 QQ 群（规则5锁定，内容永不变更）",
            onClick = {
                openQqGroup(
                    context,
                    groupUrl = cloudSettings?.qqGroupUrl?.ifBlank { com.example.ui.screens.OFFICIAL_QQ_GROUP_URL }
                        ?: com.example.ui.screens.OFFICIAL_QQ_GROUP_URL,
                    groupUin = cloudSettings?.qqGroupUin?.ifBlank { "439211347" } ?: "439211347"
                )
            }
        )
    }
}

/** 对接分区单行工具（左侧图标方块 + 标题/副标题 + 右侧箭头；可选内嵌 content） */
@Composable
private fun AppBridgeRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
    content: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconColor.copy(alpha = 0.16f), RoundedCornerShape(9.dp))
                    .clip(RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = UiverseInk
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = UiverseTextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (onClick != null) {
                Text("›", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = UiverseTextMuted)
            }
        }
        content?.invoke()
    }
}
