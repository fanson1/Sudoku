package com.finley.android.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.finley.android.sudoku.ui.theme.AppTheme

@Composable
private fun SampleButton(label: String, destructive: Boolean = false) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(13.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val container = if (destructive) colorScheme.errorContainer.copy(alpha = 0.7f)
    else colorScheme.surface.copy(alpha = 0.85f)
    Surface(
        onClick = {},
        modifier = Modifier.height(44.dp),
        interactionSource = interactionSource,
        shape = shape,
        color = container,
        contentColor = if (destructive) colorScheme.error else colorScheme.onSurfaceVariant,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SudokuSample",
    ) {
        AppTheme {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF0F3F8))
            ) {
                Column(
                    modifier = Modifier.width(300.dp).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SampleButton("撤销")
                    SampleButton("重做")
                    SampleButton("擦除", destructive = true)
                    SampleButton("提示")
                }
            }
        }
    }
}