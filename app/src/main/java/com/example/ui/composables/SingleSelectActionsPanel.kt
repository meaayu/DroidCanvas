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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.Layers
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
import androidx.compose.ui.unit.sp
import com.example.data.CanvasItem

@Composable
fun SingleSelectActionsPanel(
    selectedItem: CanvasItem?,
    isMultiSelectMode: Boolean,
    darkTheme: Boolean,
    onDuplicate: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleValues: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onDelete: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isMultiSelectMode && selectedItem != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 88.dp) // Float elegantly above the bottom dock
    ) {
        if (selectedItem != null) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (darkTheme) Color(0xFF1E2127) else Color(0xFFEFF1F6),
                border = BorderStroke(
                    1.dp,
                    if (darkTheme) Color(0xFF32363F) else Color(0xFFDCDFE7)
                ),
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                modifier = Modifier.testTag("single_select_actions_panel")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duplicate Button
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(36.dp).testTag("single_select_duplicate")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Item",
                            tint = if (darkTheme) Color.White else Color(0xFF2E4E80),
                            modifier = Modifier.size(18.dp)
                        )
                    }

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
                        modifier = Modifier.size(36.dp).testTag("single_select_pin")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (selectedItem.isPinned) "Unpin Reference" else "Pin Reference",
                            tint = if (selectedItem.isPinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                if (darkTheme) Color.White else Color(0xFF2E4E80)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Values Study Button
                    IconButton(
                        onClick = onToggleValues,
                        modifier = Modifier.size(36.dp).testTag("single_select_values")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contrast,
                            contentDescription = "Toggle Values Study",
                            tint = if (selectedItem.isValuesEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                if (darkTheme) Color.White else Color(0xFF2E4E80)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(if (darkTheme) Color(0xFF32363F) else Color(0xFFDCDFE7))
                    )

                    // Bring to Front Button
                    IconButton(
                        onClick = onBringToFront,
                        modifier = Modifier.size(36.dp).testTag("single_select_bring_to_front")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipToFront,
                            contentDescription = "Bring to Front",
                            tint = if (darkTheme) Color.White else Color(0xFF2E4E80),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Send to Back Button
                    IconButton(
                        onClick = onSendToBack,
                        modifier = Modifier.size(36.dp).testTag("single_select_send_to_back")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Send to Back",
                            tint = if (darkTheme) Color.White else Color(0xFF2E4E80),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(if (darkTheme) Color(0xFF32363F) else Color(0xFFDCDFE7))
                    )

                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("single_select_delete")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Reference",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(if (darkTheme) Color(0xFF32363F) else Color(0xFFDCDFE7))
                    )

                    // Clear Selection Button
                    IconButton(
                        onClick = onClearSelection,
                        modifier = Modifier.size(36.dp).testTag("single_select_clear")
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
}
