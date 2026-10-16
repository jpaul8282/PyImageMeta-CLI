package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.export.ExportHelper.ExportFormat
import com.example.model.ImageMetadata
import com.example.model.PrivacyStatus
import com.example.ui.components.ExifExportDialog
import com.example.ui.components.PrivacyBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseRed
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850

@Composable
fun BatchPrivacyScreen(
    images: List<ImageMetadata>,
    onStripGpsForAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var exportDialogFormat by remember { mutableStateOf(ExportFormat.CSV) }

    val gpsCount = images.count { it.hasGps }
    val secureCount = images.count { !it.hasGps }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("batch_privacy_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Batch Privacy & Catalog Studio",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Security audit & bulk metadata sanitization across all loaded photos",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        // Catalog Privacy Audit Dashboard Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "SECURITY AUDIT SUMMARY",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        AuditMetricBox("Total Photos", "${images.size}", Color.White)
                        AuditMetricBox("GPS Exposed", "$gpsCount", if (gpsCount > 0) RoseRed else EmeraldGreen)
                        AuditMetricBox("Clean / Secure", "$secureCount", EmeraldGreen)
                    }

                    HorizontalDivider(color = Slate800, thickness = 1.dp)

                    // Batch Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onStripGpsForAll,
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("batch_strip_gps_button"),
                            enabled = gpsCount > 0
                        ) {
                            Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Strip All GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                exportDialogFormat = ExportFormat.CSV
                                showExportDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_catalog_csv_button")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Export CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                exportDialogFormat = ExportFormat.JSON
                                showExportDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_catalog_json_button")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Export JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Catalog File List
        item {
            Text(
                text = "Catalog File Manifest",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(images) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.fileName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(text = "${item.make} ${item.model} • ${item.width}x${item.height}", color = Color.Gray, fontSize = 11.sp)
                    }
                    PrivacyBadge(status = item.privacyStatus)
                }
            }
        }
    }

    // Export Dialog (CSV / JSON with direct download, share and copy)
    if (showExportDialog) {
        ExifExportDialog(
            allImages = images,
            selectedImage = null,
            initialFormat = exportDialogFormat,
            onDismissRequest = { showExportDialog = false }
        )
    }
}

@Composable
private fun AuditMetricBox(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
        Text(text = value, color = valueColor, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

private fun buildCatalogJson(images: List<ImageMetadata>): String {
    val sb = StringBuilder()
    sb.appendLine("[")
    images.forEachIndexed { idx, img ->
        sb.appendLine("  {")
        sb.appendLine("    \"fileName\": \"${img.fileName}\",")
        sb.appendLine("    \"make\": \"${img.make}\",")
        sb.appendLine("    \"model\": \"${img.model}\",")
        sb.appendLine("    \"dimensions\": \"${img.width}x${img.height}\",")
        sb.appendLine("    \"hasGps\": ${img.hasGps},")
        sb.appendLine("    \"latitude\": ${img.latitude},")
        sb.appendLine("    \"longitude\": ${img.longitude},")
        sb.appendLine("    \"md5\": \"${img.md5Hash}\"")
        sb.append("  }")
        if (idx < images.size - 1) sb.appendLine(",") else sb.appendLine()
    }
    sb.appendLine("]")
    return sb.toString()
}
