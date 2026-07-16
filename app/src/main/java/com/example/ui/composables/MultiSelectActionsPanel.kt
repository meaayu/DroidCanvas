package com.example.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MultiSelectActionsPanel(
    isMultiSelectMode: Boolean,
    selectedItemIds: Set<Int>,
    darkTheme: Boolean,
    onTogglePin: () -> Unit,
    onToggleValues: () -> Unit,
    onDeleteSelected: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isMultiSelectMode && selectedItemIds.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 88.dp) // Float elegantly above the bottom dock
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (darkTheme) Color(0xFF1E2127) else Color(0xFFEFF1F6),
            border = BorderStroke(
                1.dp,
                if (darkTheme) Color(0xFF32363F) else Color(0xFFDCDFE7)
            ),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            modifier = Modifier.testTag("multi_select_actions_panel")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection Count Text
                Text(
                    text = "${selectedItemIds.size} Selected",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) Color.White else Color.Black
                )

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(if (darkTheme) Color(0xFF32363F) else Color(0xFFDCDFE7))
                )

                // Pin/Unpin Button
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(36.dp).testTag("multi_select_pin")
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin/Unpin Selected",
                        tint = if (darkTheme) Color.White else Color(0xFF2E4E80),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Values Study Mode Button
                IconButton(
                    onClick = onToggleValues,
                    modifier = Modifier.size(36.dp).testTag("multi_select_values")
                ) {
                    Icon(
                        imageVector = Icons.Default.Contrast,
                        contentDescription = "Toggle Values Study",
                        tint = if (darkTheme) Color.White else Color(0xFF2E4E80),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Selected Button
                IconButton(
                    onClick = onDeleteSelected,
                    modifier = Modifier.size(36.dp).testTag("multi_select_delete")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Selected",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(if (darkTheme) Color(0xFF32363F) else Color(0xFFDCDFE7))
                )

                // Clear Selection Button
                IconButton(
                    onClick = onClearSelection,
                    modifier = Modifier.size(36.dp).testTag("multi_select_clear")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Selection",
                        tint = if (darkTheme) Color(0xFF707684) else Color(0xFF8E95A5),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
