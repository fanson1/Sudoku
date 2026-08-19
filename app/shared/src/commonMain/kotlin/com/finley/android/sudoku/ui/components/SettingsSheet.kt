package com.finley.android.sudoku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finley.android.sudoku.ui.theme.ThemeMode

/**
 * A reusable "app settings" sheet exposing theme mode selection.
 */
@Composable
fun SettingsSheetContent(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Text(
            text = "外观设置",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "选择你喜欢的界面主题",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Text(
            text = "主题模式",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ThemeModeOption(
            icon = Icons.Default.BrightnessMedium,
            title = "跟随系统",
            subtitle = "根据设备自动切换",
            selected = themeMode == ThemeMode.SYSTEM,
            onClick = { onThemeModeChange(ThemeMode.SYSTEM) }
        )
        ThemeModeOption(
            icon = Icons.Default.BrightnessHigh,
            title = "浅色模式",
            subtitle = "明亮清爽的界面",
            selected = themeMode == ThemeMode.LIGHT,
            onClick = { onThemeModeChange(ThemeMode.LIGHT) }
        )
        ThemeModeOption(
            icon = Icons.Default.DarkMode,
            title = "深色模式",
            subtitle = "夜间护眼更舒适",
            selected = themeMode == ThemeMode.DARK,
            onClick = { onThemeModeChange(ThemeMode.DARK) }
        )
    }
}

@Composable
private fun ThemeModeOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (selected) colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        SpacerW(width = 14.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
        if (selected) {
            Surface(
                modifier = Modifier.size(22.dp),
                shape = CircleShape,
                color = colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun SpacerW(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}

@Composable
fun GameSettingsSheet(
    autoEraseNotes: Boolean,
    showConflicts: Boolean,
    onAutoEraseNotesChange: (Boolean) -> Unit,
    onShowConflictsChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Text(
            text = "游戏设置",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "自定义你的游戏体验",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Text(
            text = "自动擦除笔记",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SwitchOption(
            title = "填入数字后自动删除相关候选数",
            subtitle = "提高求解效率",
            checked = autoEraseNotes,
            onCheckChange = onAutoEraseNotesChange
        )

        Text(
            text = "显示冲突",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        SwitchOption(
            title = "实时高亮错误单元格",
            subtitle = "红色背景提示错误放置",
            checked = showConflicts,
            onCheckChange = onShowConflictsChange
        )
    }
}

@Composable
private fun SwitchOption(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckChange,
                colors = SwitchDefaults.colors(
                    checkedBorderColor = colorScheme.primary,
                    checkedThumbColor = colorScheme.primary,
                    checkedTrackColor = colorScheme.primary.copy(alpha = 0.4f),
                    uncheckedBorderColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    uncheckedThumbColor = colorScheme.surface,
                    uncheckedTrackColor = colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            )
        }
    }
}
