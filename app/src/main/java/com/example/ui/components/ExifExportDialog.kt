package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.export.ExifCsvExporter
import com.example.export.ExifJsonExporter
import com.example.export.ExportHelper
import com.example.export.ExportHelper.ExportFormat
import com.example.model.ImageMetadata
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import java.util.Locale

@Composable
fun ExifExportDialog(
    allImages: List<ImageMetadata>,
    selectedImage: ImageMetadata?,
    initialFormat: ExportFormat = ExportFormat.CSV,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var exportFormat by remember { mutableStateOf(initialFormat) }
    var exportAllCatalog by remember { mutableStateOf(selectedImage == null) }
    var includeRawTags by remember { mutableStateOf(true) }
    var prettyPrintJson by remember { mutableStateOf(true) }

    // Target images for export
    val targetImages = remember(exportAllCatalog, selectedImage, allImages) {
        if (exportAllCatalog || selectedImage == null) {
            allImages
        } else {
            listOf(selectedImage)
        }
    }

    // Generated export text content
    val exportData = remember(targetImages, exportFormat, includeRawTags, prettyPrintJson) {
        when (exportFormat) {
            ExportFormat.CSV -> ExifCsvExporter.generateCsv(targetImages)
            ExportFormat.JSON -> ExifJsonExporter.generateJson(
                images = targetImages,
                prettyPrint = prettyPrintJson,
                includeAllTags = includeRawTags
            )
        }
    }

    // File saving launcher
    val fileSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(exportFormat.mimeType)
    ) { uri: Uri? ->
        if (uri != null) {
            ExportHelper.writeDataToUri(context, uri, exportData)
        }
    }

    val suggestedFileName = remember(exportFormat, targetImages, exportAllCatalog) {
        val prefix = if (exportAllCatalog || targetImages.size > 1) {
            "pymeta_catalog_export"
        } else {
            "pymeta_exif_${targetImages.firstOrNull()?.fileName?.substringBeforeLast(".") ?: "image"}"
        }
        ExportHelper.getDefaultFileName(exportFormat, prefix)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Slate900,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(12.dp)
                .border(1.dp, Slate700, RoundedCornerShape(20.dp))
                .testTag("exif_export_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Export EXIF Data",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Download analysis-ready datasets compatible with Pandas, Excel, R & APIs",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp).testTag("close_export_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.LightGray
                        )
                    }
                }

                HorizontalDivider(color = Slate800, thickness = 1.dp)

                // Controls Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Scope Selection (Single vs Catalog)
                    if (selectedImage != null && allImages.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate850, RoundedCornerShape(10.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { exportAllCatalog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!exportAllCatalog) CyanAccent else Color.Transparent,
                                    contentColor = if (!exportAllCatalog) Color.Black else Color.LightGray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("export_scope_single_button")
                            ) {
                                Text(
                                    text = "Selected Photo (${selectedImage.fileName.take(14)}...)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            Button(
                                onClick = { exportAllCatalog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (exportAllCatalog) CyanAccent else Color.Transparent,
                                    contentColor = if (exportAllCatalog) Color.Black else Color.LightGray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("export_scope_all_button")
                            ) {
                                Text(
                                    text = "All Catalog Photos (${allImages.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Format Selector: CSV vs JSON
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FormatCard(
                            title = "CSV File",
                            subtitle = "Excel, Pandas, R & Sheets",
                            icon = Icons.Default.TableChart,
                            accentColor = EmeraldGreen,
                            isSelected = exportFormat == ExportFormat.CSV,
                            onClick = { exportFormat = ExportFormat.CSV },
                            modifier = Modifier.weight(1f)
                        )

                        FormatCard(
                            title = "JSON File",
                            subtitle = "REST APIs, JQ & Analytics",
                            icon = Icons.Default.DataObject,
                            accentColor = CyanAccent,
                            isSelected = exportFormat == ExportFormat.JSON,
                            onClick = { exportFormat = ExportFormat.JSON },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Format specific options
                    if (exportFormat == ExportFormat.JSON) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate850, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = includeRawTags,
                                    onCheckedChange = { includeRawTags = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = CyanAccent,
                                        checkmarkColor = Color.Black
                                    )
                                )
                                Text("Include raw EXIF dictionary", color = Color.White, fontSize = 11.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = prettyPrintJson,
                                    onCheckedChange = { prettyPrintJson = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = CyanAccent,
                                        checkmarkColor = Color.Black
                                    )
                                )
                                Text("Pretty Print", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Data Preview Box
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate850)
                        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DATA PREVIEW (${suggestedFileName})",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${exportData.lines().size} lines • ${String.format(Locale.US, "%.1f", exportData.length / 1024.0)} KB",
                            color = CyanAccent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val hScroll = rememberScrollState()
                    val vScroll = rememberScrollState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = exportData,
                            color = if (exportFormat == ExportFormat.JSON) CyanAccent else EmeraldGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .verticalScroll(vScroll)
                                .horizontalScroll(hScroll)
                                .testTag("export_preview_text")
                        )
                    }
                }

                // Action Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy
                    OutlinedButton(
                        onClick = {
                            ExportHelper.copyToClipboard(context, "${exportFormat.displayName} Export", exportData)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("export_dialog_copy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Copy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Share
                    Button(
                        onClick = {
                            ExportHelper.shareExportData(
                                context = context,
                                data = exportData,
                                title = "PyMeta EXIF Export ($suggestedFileName)",
                                mimeType = exportFormat.mimeType
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Slate800,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("export_dialog_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Download / Save File
                    Button(
                        onClick = {
                            fileSaveLauncher.launch(suggestedFileName)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (exportFormat == ExportFormat.CSV) EmeraldGreen else CyanAccent,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(44.dp)
                            .testTag("export_dialog_download_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Download ${exportFormat.extension.uppercase()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.15f) else Slate850
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) accentColor else Slate800
        ),
        modifier = modifier.height(68.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) accentColor else Slate800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.Black else accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
