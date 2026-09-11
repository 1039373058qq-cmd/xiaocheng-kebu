package com.example.deviceasset.ui.usage

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF050505)
private val UsagePeach = Color(0xFFE8B8A9)
private val UsagePeachDeep = Color(0xFFD8A094)
private val UsageInk = Color(0xFF261B1D)
private val UsageMuted = Color(0xFF765F5D)
private val UsageBlue = Color(0xFF0878F9)
private val UsageGreen = Color(0xFF59C93A)
private val Track = Color(0xFFF1D3CD)

private val UsageSans = FontFamily.SansSerif

@Composable
fun UsageAssistantScreen() {
    var updatedAt by remember { mutableStateOf("01:10:03") }
    var isWaiting by remember { mutableStateOf(true) }
    var panelExpanded by remember { mutableStateOf(true) }
    var selectedShortcut by remember { mutableStateOf("概览") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Night),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            UsageCard(
                updatedAt = updatedAt,
                isWaiting = isWaiting,
                onRefresh = {
                    updatedAt = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                },
                onWaitingChanged = { isWaiting = it },
            )
            IconPanel(
                expanded = panelExpanded,
                selectedShortcut = selectedShortcut,
                onToggleExpanded = { panelExpanded = !panelExpanded },
                onShortcutSelected = { selectedShortcut = it },
            )
            Text(
                text = "用量助手 · 本地演示数据",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                color = Color(0xFF817B7B),
                fontFamily = UsageSans,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun UsageCard(
    updatedAt: String,
    isWaiting: Boolean,
    onRefresh: () -> Unit,
    onWaitingChanged: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(UsagePeach, UsagePeachDeep),
                    ),
                )
                .padding(horizontal = 18.dp, vertical = 17.dp),
        ) {
            UsageHeader(onRefresh = onRefresh)
            Spacer(Modifier.height(19.dp))
            WindowSummary()
            Spacer(Modifier.height(11.dp))
            UsageProgressBar()
            Spacer(Modifier.height(9.dp))
            Text(
                text = "重置：8月30日 18:27",
                color = UsageMuted,
                fontFamily = UsageSans,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(23.dp))
            Text(
                text = "Token 活动",
                color = UsageInk,
                fontFamily = UsageSans,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(13.dp))
            UsageMetrics()
            Spacer(Modifier.height(18.dp))
            ActivityChart()
            Spacer(Modifier.height(17.dp))
            Text(
                text = "更新于 $updatedAt · 每分钟自动刷新",
                color = UsageMuted,
                fontFamily = UsageSans,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(13.dp))
            DividerLine()
            Spacer(Modifier.height(11.dp))
            WaitingRow(isWaiting = isWaiting, onWaitingChanged = onWaitingChanged)
        }
    }
}

@Composable
private fun UsageHeader(onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UsageLogo()
        Spacer(Modifier.width(11.dp))
        Text(
            text = "Codex 用量",
            modifier = Modifier.weight(1f),
            color = UsageInk,
            fontFamily = UsageSans,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Plus 计划",
            color = UsageInk,
            fontFamily = UsageSans,
            fontSize = 17.sp,
        )
        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .size(42.dp)
                .semantics { contentDescription = "刷新用量" },
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                tint = UsageInk,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun UsageLogo() {
    Row(
        modifier = Modifier
            .size(width = 26.dp, height = 25.dp)
            .clip(RoundedCornerShape(4.dp)),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(15.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(UsageBlue),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(21.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(UsageBlue),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(25.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(UsageBlue),
        )
    }
}

@Composable
private fun WindowSummary() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "7 天窗口",
            color = UsageInk,
            fontFamily = UsageSans,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "剩余 99%  ·  已用 1%",
            color = UsageInk,
            fontFamily = UsageSans,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun UsageProgressBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(50))
            .background(Track),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.012f)
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(UsageGreen),
        )
    }
}

@Composable
private fun UsageMetrics() {
    val metrics = listOf(
        "5.5亿" to "累计",
        "8843.2万" to "单日峰值",
        "14/23 天" to "连续 / 最长",
        "2 小时 23 分" to "最长任务",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        metrics.forEach { (value, label) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = value,
                    color = UsageInk,
                    fontFamily = UsageSans,
                    fontSize = if (value.length > 7) 14.sp else 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = label,
                    color = UsageMuted,
                    fontFamily = UsageSans,
                    fontSize = 11.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ActivityChart() {
    val bars = listOf(
        ChartBar("16th", 0.18f, UsageGreen.copy(alpha = 0.58f)),
        ChartBar("17th", 0.16f, UsageGreen.copy(alpha = 0.58f)),
        ChartBar("18th", 0.10f, UsageGreen.copy(alpha = 0.58f)),
        ChartBar("19th", 0.12f, UsageGreen.copy(alpha = 0.58f)),
        ChartBar("20th", 0.11f, UsageGreen.copy(alpha = 0.58f)),
        ChartBar("21st", 0.95f, UsageBlue),
        ChartBar("22nd", 0.72f, UsageGreen.copy(alpha = 0.58f)),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEach { bar ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height((72 * bar.ratio).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bar.color),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = bar.label,
                    color = UsageInk,
                    fontFamily = UsageSans,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

private data class ChartBar(val label: String, val ratio: Float, val color: Color)

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(UsageMuted.copy(alpha = 0.32f)),
    )
}

@Composable
private fun WaitingRow(isWaiting: Boolean, onWaitingChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(UsagePeachDeep.copy(alpha = 0.65f))
                .clickable { onWaitingChanged(!isWaiting) }
                .semantics { contentDescription = "切换后台等待" },
            contentAlignment = Alignment.Center,
        ) {
            if (isWaiting) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = UsageInk,
                    modifier = Modifier.size(25.dp),
                )
            }
        }
        Spacer(Modifier.width(11.dp))
        Text(
            text = if (isWaiting) "登录时在后台等待 Codex" else "后台等待已暂停",
            modifier = Modifier.weight(1f),
            color = UsageInk,
            fontFamily = UsageSans,
            fontSize = 16.sp,
        )
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onWaitingChanged(!isWaiting) },
            color = UsagePeachDeep.copy(alpha = 0.55f),
        ) {
            Text(
                text = if (isWaiting) "退出" else "开启",
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
                color = UsageInk,
                fontFamily = UsageSans,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun IconPanel(
    expanded: Boolean,
    selectedShortcut: String,
    onToggleExpanded: () -> Unit,
    onShortcutSelected: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded)
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Dashboard,
                    contentDescription = null,
                    tint = UsageBlue,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    text = "图标面板",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFF2EEEE),
                    fontFamily = UsageSans,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (expanded) "收起" else "展开",
                    color = Color(0xFFAAA4A4),
                    fontFamily = UsageSans,
                    fontSize = 12.sp,
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(9.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ShortcutItem(
                            icon = Icons.Outlined.BarChart,
                            label = "概览",
                            selected = selectedShortcut == "概览",
                            onClick = { onShortcutSelected("概览") },
                            modifier = Modifier.weight(1f),
                        )
                        ShortcutItem(
                            icon = Icons.Outlined.Refresh,
                            label = "刷新",
                            selected = selectedShortcut == "刷新",
                            onClick = { onShortcutSelected("刷新") },
                            modifier = Modifier.weight(1f),
                        )
                        ShortcutItem(
                            icon = Icons.Outlined.NotificationsOff,
                            label = "专注",
                            selected = selectedShortcut == "专注",
                            onClick = { onShortcutSelected("专注") },
                            modifier = Modifier.weight(1f),
                        )
                        ShortcutItem(
                            icon = Icons.Outlined.Settings,
                            label = "设置",
                            selected = selectedShortcut == "设置",
                            onClick = { onShortcutSelected("设置") },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF202020))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFFC46A),
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "当前快捷项：$selectedShortcut",
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFC6C0C0),
                            fontFamily = UsageSans,
                            fontSize = 12.sp,
                        )
                        Icon(
                            imageVector = Icons.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = Color(0xFF817B7B),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(if (selected) Color(0xFF263B5E) else Color(0xFF202020))
            .border(
                width = 1.dp,
                color = if (selected) UsageBlue.copy(alpha = 0.75f) else Color.Transparent,
                shape = RoundedCornerShape(13.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else Color(0xFFB8B1B1),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFFB8B1B1),
            fontFamily = UsageSans,
            fontSize = 11.sp,
        )
    }
}
