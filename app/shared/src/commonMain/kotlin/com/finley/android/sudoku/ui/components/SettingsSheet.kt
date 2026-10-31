package com.finley.android.sudoku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.finley.android.sudoku.i18n.Language
import com.finley.android.sudoku.i18n.LocalStrings
import com.finley.android.sudoku.i18n.StringKey
import com.finley.android.sudoku.ui.theme.ThemeMode

/**
 * Reusable "app settings" sheet: language, theme and Pro status.
 */
@Composable
fun SettingsSheetContent(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    language: Language,
    onLanguageChange: (Language) -> Unit,
    isPro: Boolean,
    onUpgradeClick: () -> Unit
) {
    val s = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = s[StringKey.APPEARANCE_SETTINGS],
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = s[StringKey.APPEARANCE_SUBTITLE],
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
            text = s[StringKey.LANGUAGE],
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(Language.entries.toList(), key = { it.code }) { entry ->
                val selected = entry == language
                FilterChip(
                    selected = selected,
                    onClick = { onLanguageChange(entry) },
                    label = {
                        Text(
                            text = if (entry == Language.SYSTEM) {
                                s[StringKey.LANGUAGE_SYSTEM]
                            } else {
                                entry.nativeName
                            },
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = if (entry == Language.SYSTEM) {
                        {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        null
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Text(
            text = s[StringKey.LANGUAGE_SUBTITLE],
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Text(
            text = s[StringKey.THEME_MODE],
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ThemeModeOption(
            icon = Icons.Default.BrightnessMedium,
            title = s[StringKey.THEME_SYSTEM],
            subtitle = s[StringKey.THEME_SYSTEM_SUB],
            selected = themeMode == ThemeMode.SYSTEM,
            onClick = { onThemeModeChange(ThemeMode.SYSTEM) }
        )
        ThemeModeOption(
            icon = Icons.Default.BrightnessHigh,
            title = s[StringKey.THEME_LIGHT],
            subtitle = s[StringKey.THEME_LIGHT_SUB],
            selected = themeMode == ThemeMode.LIGHT,
            onClick = { onThemeModeChange(ThemeMode.LIGHT) }
        )
        ThemeModeOption(
            icon = Icons.Default.DarkMode,
            title = s[StringKey.THEME_DARK],
            subtitle = s[StringKey.THEME_DARK_SUB],
            selected = themeMode == ThemeMode.DARK,
            onClick = { onThemeModeChange(ThemeMode.DARK) }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        ProSection(isPro = isPro, onUpgradeClick = onUpgradeClick)
    }
}

@Composable
private fun ProSection(isPro: Boolean, onUpgradeClick: () -> Unit) {
    val s = LocalStrings.current
    val colorScheme = MaterialTheme.colorScheme

    Text(
        text = s[StringKey.PRO_SECTION],
        style = MaterialTheme.typography.labelLarge,
        color = colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (isPro) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colorScheme.primaryContainer.copy(alpha = 0.5f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            SpacerW(width = 14.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s[StringKey.PRO_ACTIVE],
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimaryContainer
                )
                Text(
                    text = s[StringKey.PRO_ACTIVE_SUBTITLE],
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
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
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colorScheme.primaryContainer.copy(alpha = 0.5f))
                .clickable { onUpgradeClick() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            SpacerW(width = 14.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s[StringKey.PRO_ENTRY_TITLE],
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimaryContainer
                )
                Text(
                    text = s[StringKey.PRO_ENTRY_SUBTITLE],
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
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
    val s = LocalStrings.current

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Text(
            text = s[StringKey.GAME_SETTINGS],
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = s[StringKey.GAME_SETTINGS_SUBTITLE],
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
            text = s[StringKey.AUTO_ERASE_NOTES],
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SwitchOption(
            title = s[StringKey.AUTO_ERASE_NOTES_TITLE],
            subtitle = s[StringKey.AUTO_ERASE_NOTES_SUB],
            checked = autoEraseNotes,
            onCheckChange = onAutoEraseNotesChange
        )

        Text(
            text = s[StringKey.SHOW_CONFLICTS],
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        SwitchOption(
            title = s[StringKey.SHOW_CONFLICTS_TITLE],
            subtitle = s[StringKey.SHOW_CONFLICTS_SUB],
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
