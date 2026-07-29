package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExifTagItem
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RoseRed
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900

@Composable
fun ExifTable(
    tags: List<ExifTagItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val grouped = tags.groupBy { it.category }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exif_table_container"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (tags.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate850, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No EXIF tags embedded (Sanitized / Clean)",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        } else {
            grouped.forEach { (category, categoryTags) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = category.uppercase(),
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        categoryTags.forEachIndexed { idx, item ->
                            if (idx > 0) HorizontalDivider(color = Slate800, thickness = 1.dp)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (item.isSensitive) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Sensitive Tag",
                                            tint = RoseRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = item.tagName,
                                        color = Color.LightGray,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = item.tagValue,
                                        color = if (item.isSensitive) RoseRed else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText(item.tagName, item.tagValue))
                                            Toast.makeText(context, "Copied ${item.tagName}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy tag value",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
