package com.example.ui.screens.toolbox

import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

/**
 * v1.1.18 新工具：我的工具夹（列表形式，可任意编辑/删除）
 *
 * 三大分类（标签切换）：
 * - ?? 老师用（教案 / 成绩单 / 家长沟通记录…）
 * - ?? 上班用（日报 / 会议纪要 / 待办清单…）
 * - ?? 记录用（灵感 / 收支 / 备忘…）
 *
 * 每个条目 = 名称 + 备注；支持添加 / 编辑 / 删除，本地 SharedPreferences 持久化。
 */
private const val PREFS = "lzdz_custom_tools"
private const val KEY = "tools_json"

private data class ToolItem(val id: Long, val name: String, val note: String)

private enum class ToolKind(val id: String, val label: String, val icon: String) {
    TEACHER("teacher", "老师用", "📚"),
    WORK("work", "上班用", "💼"),
    RECORD("record", "记录用", "📝")
}

private fun loadTools(context: Context, kind: String): List<ToolItem> {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val raw = prefs.getString("$KEY:$kind", "[]") ?: "[]"
    val out = mutableListOf<ToolItem>()
    try {
        val arr = JSONArray(raw)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(ToolItem(o.optLong("id"), o.optString("name"), o.optString("note")))
        }
    } catch (_: Exception) {}
    return out
}

private fun saveTools(context: Context, kind: String, list: List<ToolItem>) {
    val arr = JSONArray()
    list.forEach { t ->
        val o = JSONObject()
        o.put("id", t.id)
        o.put("name", t.name)
        o.put("note", t.note)
        arr.put(o)
    }
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit().putString("$KEY:$kind", arr.toString()).apply()
}

/** 我的工具夹 · 列表呈现（可任意编辑删除） */
@Composable
fun CustomToolsSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var kind by remember { mutableStateOf(ToolKind.TEACHER) }
    var tools by remember { mutableStateOf(loadTools(context, ToolKind.TEACHER.id)) }
    var showEditor by remember { mutableStateOf<Pair<ToolItem?, ToolKind>?>(null) }

    // 切换分类时重新加载
    fun switchKind(k: ToolKind) {
        kind = k
        tools = loadTools(context, k.id)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 标题卡
        Surface(
            color = Color(0xFFFFFDF9).copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.7f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("🧰 我的工具夹", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFFDE2910))
                Text("老师/上班/记录 · 列表形式 · 可任意添加编辑删除", fontSize = 11.sp, color = Color(0xFF7A4A45), modifier = Modifier.padding(top = 2.dp))
            }
        }

        // 分类切换
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolKind.entries.forEach { k ->
                FilterChip(
                    selected = kind == k,
                    onClick = { switchKind(k) },
                    label = { Text("${k.icon} ${k.label}") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 工具列表
        if (tools.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFFFF9EF), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("还没有工具，点击下方「添加」新建一条吧～", fontSize = 12.sp, color = Color(0xFF9A7B6B))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tools, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFFFF3E0), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Work, contentDescription = null, tint = Color(0xFFDE2910), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B1F1F), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (item.note.isNotBlank()) {
                                Text(item.note, fontSize = 11.sp, color = Color(0xFF9A7B6B), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                        IconButton(onClick = { showEditor = item to kind }) {
                            Icon(Icons.Filled.Edit, contentDescription = "编辑", tint = Color(0xFFDE2910), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = {
                            tools = tools.filterNot { it.id == item.id }
                            saveTools(context, kind.id, tools)
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // 添加按钮
        Button(
            onClick = { showEditor = null to kind },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDE2910)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text("添加工具", fontWeight = FontWeight.Bold)
        }
    }

    // 添加 / 编辑弹窗
    val editor = showEditor
    if (editor != null) {
        ToolEditorDialog(
            initial = editor.first,
            onDismiss = { showEditor = null },
            onSave = { name, note ->
                val curKind = editor.second
                val list = loadTools(context, curKind.id)
                val updated = if (editor.first == null) {
                    list + ToolItem(System.currentTimeMillis(), name, note)
                } else {
                    list.map { if (it.id == editor.first!!.id) ToolItem(it.id, name, note) else it }
                }
                saveTools(context, curKind.id, updated)
                if (kind == curKind) tools = updated
                showEditor = null
            }
        )
    }
}

@Composable
private fun ToolEditorDialog(
    initial: ToolItem?,
    onDismiss: () -> Unit,
    onSave: (name: String, note: String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (initial == null) Icons.Filled.Add else Icons.Filled.Edit, contentDescription = null, tint = Color(0xFFDE2910))
                Spacer(modifier = Modifier.size(8.dp))
                Text(if (initial == null) "添加工具" else "编辑工具", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(30) },
                    label = { Text("工具名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(100) },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onSave(name.trim(), note.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDE2910)),
                enabled = name.isNotBlank()
            ) { Text("保存", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
