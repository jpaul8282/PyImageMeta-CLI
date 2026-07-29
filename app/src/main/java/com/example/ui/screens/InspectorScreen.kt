package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ImageMetadata
import com.example.ui.components.ExifTable
import com.example.ui.components.ImageCard
import com.example.ui.components.PrivacyBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseRed
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850

@Composable
fun InspectorScreen(
    images: List<ImageMetadata>,
    selectedImage: ImageMetadata?,
    isExtractingMetadata: Boolean = false,
    extractionStatus: String = "",
    onSelectImage: (ImageMetadata) -> Unit,
    onImportUri: (Uri) -> Unit,
    onReextractMetadata: () -> Unit = {},
    onStripGps: () -> Unit,
    onStripAll: () -> Unit,
    onUpdateIptc: (artist: String, copyright: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportUri(it) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("inspector_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header & Photo Selector Carousel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Image Metadata Inspector",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${images.size} Photos Loaded in Catalog",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onReextractMetadata,
                        modifier = Modifier
                            .background(Slate800, RoundedCornerShape(20.dp))
                            .testTag("rescan_metadata_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Re-scan EXIF Metadata",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("import_photo_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Import Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Metadata Extraction Progress Banner
        if (isExtractingMetadata) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("metadata_extraction_loading_banner")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = CyanAccent,
                                strokeWidth = 2.5.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Extracting Metadata...",
                                    color = CyanAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = extractionStatus.ifBlank { "Parsing EXIF, IPTC, and GPS binary tags..." },
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = CyanAccent,
                            trackColor = Slate800
                        )
                    }
                }
            }
        }

        // Horizontal Photo Carousel
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(images) { img ->
                    ImageCard(
                        image = img,
                        isSelected = img.id == selectedImage?.id,
                        onClick = { onSelectImage(img) },
                        modifier = Modifier.width(260.dp)
                    )
                }
            }
        }

        // Selected Image Detail Banner & Actions
        if (selectedImage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedImage.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            PrivacyBadge(status = selectedImage.privacyStatus)
                        }

                        // Image Preview Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate800),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImage.bitmap != null) {
                                Image(
                                    bitmap = selectedImage.bitmap.asImageBitmap(),
                                    contentDescription = selectedImage.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Camera Specs Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate800, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SpecItem("Make/Model", "${selectedImage.make} ${selectedImage.model}")
                            SpecItem("Exposure", "${selectedImage.exposureTime} @ ${selectedImage.aperture}")
                            SpecItem("ISO", selectedImage.iso)
                            SpecItem("Focal", selectedImage.focalLength)
                        }

                        // GPS Location Banner (if available)
                        if (selectedImage.hasGps && selectedImage.latitude != null && selectedImage.longitude != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(RoseRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = RoseRed, modifier = Modifier.size(16.dp))
                                        Text(text = "GPS Coordinates Embedded", color = RoseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "${selectedImage.latitude}°, ${selectedImage.longitude}° (${selectedImage.altitude ?: 0.0}m)",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val geoUri = Uri.parse("geo:${selectedImage.latitude},${selectedImage.longitude}?q=${selectedImage.latitude},${selectedImage.longitude}")
                                        val intent = Intent(Intent.ACTION_VIEW, geoUri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Location: ${selectedImage.latitude}, ${selectedImage.longitude}", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseRed),
                                    modifier = Modifier.testTag("open_map_button")
                                ) {
                                    Text(text = "Map Link", fontSize = 11.sp)
                                }
                            }
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = onStripGps,
                                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("strip_gps_button"),
                                enabled = selectedImage.hasGps
                            ) {
                                Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "Strip GPS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onStripAll,
                                colors = ButtonDefaults.buttonColors(containerColor = RoseRed, contentColor = Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("strip_all_button")
                            ) {
                                Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "Strip All", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showEditDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = CyanAccent),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_iptc_button")
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "Edit IPTC", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val csvData = com.example.export.ExifCsvExporter.generateCsv(listOf(selectedImage))
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("EXIF CSV", csvData))
                                    Toast.makeText(context, "EXIF CSV for '${selectedImage.fileName}' copied!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_csv_button")
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "Export CSV", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // EXIF Tags Breakdown Table
            item {
                Text(
                    text = "Embedded EXIF Tags",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ExifTable(tags = selectedImage.allTags)
            }
        }
    }

    // Edit IPTC Dialog Modal
    if (showEditDialog && selectedImage != null) {
        var artistText by remember { mutableStateOf(selectedImage.artist) }
        var copyrightText by remember { mutableStateOf(selectedImage.copyright) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(text = "Edit Author & Copyright (IPTC)", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = artistText,
                        onValueChange = { artistText = it },
                        label = { Text("Artist / Photographer") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = copyrightText,
                        onValueChange = { copyrightText = it },
                        label = { Text("Copyright Notice") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateIptc(artistText, copyrightText)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                ) {
                    Text("Save Metadata")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Slate850
        )
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label.uppercase(), color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
