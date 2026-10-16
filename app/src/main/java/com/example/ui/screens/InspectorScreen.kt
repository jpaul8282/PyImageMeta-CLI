package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.*
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
import com.example.db.ImageMetadataEntity
import com.example.export.ExportHelper.ExportFormat
import com.example.model.ImageMetadata
import com.example.model.PrivacyStatus
import com.example.model.toModel
import com.example.ui.components.ExifExportDialog
import com.example.ui.components.ExifSummaryView
import com.example.ui.components.ExifTable
import com.example.ui.components.ExtractedMetadataListView
import com.example.ui.components.ImageCard
import com.example.ui.components.PrivacyBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseRed
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InspectorScreen(
    images: List<ImageMetadata>,
    selectedImage: ImageMetadata?,
    isExtractingMetadata: Boolean = false,
    extractionStatus: String = "",
    // Batch processing & Room state parameters
    pagedRoomImages: List<ImageMetadataEntity> = emptyList(),
    totalRoomImagesCount: Int = 0,
    currentPage: Int = 1,
    pageSize: Int = 5,
    searchQuery: String = "",
    isBatchProcessing: Boolean = false,
    batchProcessingProgress: Float = 0f,
    batchProcessingStatus: String = "",
    onSelectImage: (ImageMetadata) -> Unit,
    onImportUri: (Uri) -> Unit,
    onBatchImportUris: (List<Uri>) -> Unit = {},
    onSetPage: (Int) -> Unit = {},
    onNextPage: (Int) -> Unit = {},
    onPreviousPage: () -> Unit = {},
    onSetPageSize: (Int) -> Unit = {},
    onSetSearchQuery: (String) -> Unit = {},
    onGenerateAiMetaForEntity: (ImageMetadataEntity, String?) -> Unit = { _, _ -> },
    onBatchGenerateAiMetaForAll: () -> Unit = {},
    onDeleteFromRoom: (String) -> Unit = {},
    onClearRoomDb: () -> Unit = {},
    onReextractMetadata: () -> Unit = {},
    onStripGps: () -> Unit,
    onStripAll: () -> Unit,
    onUpdateIptc: (artist: String, copyright: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportDialogFormat by remember { mutableStateOf(ExportFormat.CSV) }
    var activeDetailTab by remember { mutableStateOf(0) } // 0 = Summary Cards, 1 = Metadata List, 2 = Raw Tags Table
    var customAiPrompt by remember { mutableStateOf("") }
    var activeSectionTab by remember { mutableStateOf(0) } // 0 = Paginated Catalog, 1 = AI Meta Generator Studio, 2 = Deep Inspector

    val totalPages = if (totalRoomImagesCount > 0) {
        ((totalRoomImagesCount + pageSize - 1) / pageSize).coerceAtLeast(1)
    } else 1

    // Batch Multi-Select Photo Picker Launcher: allows selecting multiple images simultaneously!
    val batchPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onBatchImportUris(uris)
            Toast.makeText(context, "Processing ${uris.size} images in batch...", Toast.LENGTH_SHORT).show()
        }
    }

    // Single Photo Picker Launcher
    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportUri(it) }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("inspector_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header: IMAGE META A.I GENERATOR
        item {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "IMAGE META A.I GENERATOR",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "Batch multi-image processing, neural EXIF synthesis & Room persistent catalog",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    // Top Action Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onReextractMetadata,
                            modifier = Modifier
                                .background(Slate800, RoundedCornerShape(20.dp))
                                .size(38.dp)
                                .testTag("rescan_metadata_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Re-scan EXIF Metadata",
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                exportDialogFormat = ExportFormat.CSV
                                showExportDialog = true
                            },
                            modifier = Modifier
                                .background(Slate800, RoundedCornerShape(20.dp))
                                .size(38.dp)
                                .testTag("top_export_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export EXIF Data",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Status Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Room DB: $totalRoomImagesCount Photos", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Slate850,
                            labelColor = CyanAccent
                        )
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Batch Multi-Select Active", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Slate850,
                            labelColor = EmeraldGreen
                        )
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Neural Synthesizer Ready", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Slate850,
                            labelColor = AmberGold
                        )
                    )
                }

                // Primary Batch & Import Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Batch Multi-Select Button
                    Button(
                        onClick = { batchPickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .testTag("batch_select_images_button")
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Batch Select", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Single Import Button
                    OutlinedButton(
                        onClick = { singlePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("import_photo_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Import One", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Batch AI Gen Button
                    Button(
                        onClick = onBatchGenerateAiMetaForAll,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("batch_ai_gen_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Batch AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Batch Processing Banner
        if (isBatchProcessing) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("batch_processing_banner")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = CyanAccent,
                                strokeWidth = 2.5.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Batch Processing Images & Synthesizing AI Meta...",
                                    color = CyanAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = batchProcessingStatus.ifBlank { "Extracting EXIF telemetry and storing in Room DB..." },
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = { batchProcessingProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CyanAccent,
                            trackColor = Slate800
                        )
                    }
                }
            }
        }

        // Single Extraction Banner
        if (isExtractingMetadata && !isBatchProcessing) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("metadata_extraction_loading_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CyanAccent,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = extractionStatus.ifBlank { "Parsing EXIF, IPTC & GPS tags..." },
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Page Scroll Section Navigation Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate850, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SectionTabButton(
                    title = "Room Catalog ($totalRoomImagesCount)",
                    icon = Icons.Default.PhotoLibrary,
                    isSelected = activeSectionTab == 0,
                    onClick = {
                        activeSectionTab = 0
                        coroutineScope.launch { listState.animateScrollToItem(3) }
                    },
                    modifier = Modifier.weight(1.2f)
                )

                SectionTabButton(
                    title = "AI Meta Studio",
                    icon = Icons.Default.AutoAwesome,
                    isSelected = activeSectionTab == 1,
                    onClick = {
                        activeSectionTab = 1
                        coroutineScope.launch { listState.animateScrollToItem(4) }
                    },
                    modifier = Modifier.weight(1.1f)
                )

                SectionTabButton(
                    title = "Deep Inspector",
                    icon = Icons.Default.Dashboard,
                    isSelected = activeSectionTab == 2,
                    onClick = {
                        activeSectionTab = 2
                        coroutineScope.launch { listState.animateScrollToItem(5) }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ==========================================
        // SECTION 1: PAGINATED ROOM DATABASE CATALOG
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("room_paginated_catalog_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.TableRows, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Text(
                                text = "PERSISTENT ROOM CATALOG",
                                color = CyanAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "Page $currentPage of $totalPages ($totalRoomImagesCount Total)",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Search Field in Room
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSetSearchQuery,
                        placeholder = { Text("Search by filename, camera, AI tags, scene...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSetSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Slate800,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("room_search_input")
                    )

                    // Pagination Control Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate800, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // First Page
                        IconButton(
                            onClick = { onSetPage(1) },
                            enabled = currentPage > 1,
                            modifier = Modifier.size(34.dp).testTag("first_page_button")
                        ) {
                            Icon(Icons.Default.FirstPage, contentDescription = "First Page", tint = if (currentPage > 1) CyanAccent else Color.DarkGray)
                        }

                        // Prev Page
                        IconButton(
                            onClick = onPreviousPage,
                            enabled = currentPage > 1,
                            modifier = Modifier.size(34.dp).testTag("prev_page_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Page", tint = if (currentPage > 1) CyanAccent else Color.DarkGray)
                        }

                        // Page Numbers & Page Size Selector
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Page $currentPage / $totalPages",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Page size chips: 5, 10, 20
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(5, 10, 20).forEach { size ->
                                    val isSelectedSize = pageSize == size
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelectedSize) CyanAccent else Slate850)
                                            .clickable { onSetPageSize(size) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$size",
                                            color = if (isSelectedSize) Color.Black else Color.LightGray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Next Page
                        IconButton(
                            onClick = { onNextPage(totalPages) },
                            enabled = currentPage < totalPages,
                            modifier = Modifier.size(34.dp).testTag("next_page_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Page", tint = if (currentPage < totalPages) CyanAccent else Color.DarkGray)
                        }

                        // Last Page
                        IconButton(
                            onClick = { onSetPage(totalPages) },
                            enabled = currentPage < totalPages,
                            modifier = Modifier.size(34.dp).testTag("last_page_button")
                        ) {
                            Icon(Icons.Default.LastPage, contentDescription = "Last Page", tint = if (currentPage < totalPages) CyanAccent else Color.DarkGray)
                        }
                    }

                    // Paginated List of Room Entities
                    if (pagedRoomImages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No photos found matching '$searchQuery'" else "No images in Room database. Use 'Batch Select' above to import photos.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            pagedRoomImages.forEach { entity ->
                                RoomImageItemCard(
                                    entity = entity,
                                    isSelected = entity.id == selectedImage?.id,
                                    onSelect = { onSelectImage(entity.toModel()) },
                                    onGenerateAi = { onGenerateAiMetaForEntity(entity, customAiPrompt.ifBlank { null }) },
                                    onDelete = { onDeleteFromRoom(entity.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 2: AI META GENERATOR STUDIO CARD
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_meta_generator_studio_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                            Text(
                                text = "A.I META GENERATOR STUDIO",
                                color = AmberGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        if (selectedImage != null) {
                            Text(
                                text = "Target: ${selectedImage.title}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = "Synthesize photographic captions, aesthetic lighting profiles, optical metadata, and ready-to-run GenAI prompt directives.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    // Custom creative prompt input
                    OutlinedTextField(
                        value = customAiPrompt,
                        onValueChange = { customAiPrompt = it },
                        placeholder = { Text("Custom creative style prompt (e.g., 'Moody cyberpunk rain reflections on anamorphic 35mm lens')...", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = Slate800,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("ai_custom_prompt_input")
                    )

                    // Preset prompt chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Cinematic 35mm",
                            "Golden Hour Vista",
                            "Cyberpunk Neon",
                            "Studio Editorial",
                            "Forensic PRNU"
                        ).forEach { preset ->
                            SuggestionChip(
                                onClick = { customAiPrompt = preset },
                                label = { Text(preset, fontSize = 10.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Slate800,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    // Action: Synthesize for active photo
                    Button(
                        onClick = {
                            val targetEntity = pagedRoomImages.find { it.id == selectedImage?.id }
                                ?: pagedRoomImages.firstOrNull()
                            if (targetEntity != null) {
                                onGenerateAiMetaForEntity(targetEntity, customAiPrompt.ifBlank { null })
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("synthesize_ai_meta_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Synthesize AI Metadata for Selected Image", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // AI Generated Directives Box
                    val matchingEntity = pagedRoomImages.find { it.id == selectedImage?.id }
                    if (matchingEntity != null && matchingEntity.aiCaption.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate800, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                Text(text = "AI SYNTHESIS ACTIVE", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(text = "Caption: \"${matchingEntity.aiCaption}\"", color = Color.White, fontSize = 12.sp)
                            Text(text = "Scene: ${matchingEntity.aiSceneType} | Lighting: ${matchingEntity.aiLighting}", color = CyanAccent, fontSize = 11.sp)
                            if (matchingEntity.aiPromptStyle.isNotEmpty()) {
                                Text(
                                    text = matchingEntity.aiPromptStyle,
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(Slate900, RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 3: DEEP INSPECTOR & CAROUSEL
        // ==========================================
        item {
            Text(
                text = "IMAGE DETAIL INSPECTOR",
                color = CyanAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
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
                                    exportDialogFormat = ExportFormat.CSV
                                    showExportDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_data_button")
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "Export", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Mode Switcher: Summary Cards vs Readable Metadata List vs Raw Tags Table
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Slate850, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { activeDetailTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeDetailTab == 0) CyanAccent else Color.Transparent,
                            contentColor = if (activeDetailTab == 0) Color.Black else Color.LightGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("tab_summary_cards")
                    ) {
                        Icon(imageVector = Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Summary", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { activeDetailTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeDetailTab == 1) CyanAccent else Color.Transparent,
                            contentColor = if (activeDetailTab == 1) Color.Black else Color.LightGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(40.dp)
                            .testTag("tab_metadata_list")
                    ) {
                        Icon(imageVector = Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Metadata List", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { activeDetailTab = 2 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeDetailTab == 2) CyanAccent else Color.Transparent,
                            contentColor = if (activeDetailTab == 2) Color.Black else Color.LightGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("tab_raw_tags")
                    ) {
                        Icon(imageVector = Icons.Default.TableRows, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Raw Tags (${selectedImage.allTags.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Render either Material 3 Summary Cards View, Clean Metadata List, or Full Table
            item {
                when (activeDetailTab) {
                    0 -> ExifSummaryView(image = selectedImage, modifier = Modifier.fillMaxWidth())
                    1 -> ExtractedMetadataListView(image = selectedImage, modifier = Modifier.fillMaxWidth())
                    2 -> ExifTable(tags = selectedImage.allTags, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }

    // IPTC Metadata Edit Dialog
    if (showEditDialog && selectedImage != null) {
        var editArtist by remember { mutableStateOf(selectedImage.artist) }
        var editCopyright by remember { mutableStateOf(selectedImage.copyright) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit IPTC Attribution", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editArtist,
                        onValueChange = { editArtist = it },
                        label = { Text("Photographer / Artist") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Slate800,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("iptc_artist_input")
                    )

                    OutlinedTextField(
                        value = editCopyright,
                        onValueChange = { editCopyright = it },
                        label = { Text("Copyright Notice") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Slate800,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("iptc_copyright_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateIptc(editArtist, editCopyright)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                    modifier = Modifier.testTag("save_iptc_button")
                ) {
                    Text("Save Changes")
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

    // Export Dialog
    if (showExportDialog && selectedImage != null) {
        ExifExportDialog(
            allImages = images,
            selectedImage = selectedImage,
            initialFormat = exportDialogFormat,
            onDismissRequest = { showExportDialog = false }
        )
    }
}

@Composable
private fun SectionTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) CyanAccent else Color.Transparent,
            contentColor = if (isSelected) Color.Black else Color.LightGray
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(38.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoomImageItemCard(
    entity: ImageMetadataEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onGenerateAi: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Slate800 else Slate900
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .testTag("room_item_${entity.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Title, dimensions, privacy badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entity.fileName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${entity.width}x${entity.height} • ${"%.1f".format(entity.fileSizeKb / 1024.0)} MB • ${entity.make} ${entity.model}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                if (entity.hasGps) {
                    PrivacyBadge(status = PrivacyStatus.EXPOSED_GPS_TAGGED)
                } else {
                    PrivacyBadge(status = PrivacyStatus.SECURE_NO_GPS)
                }
            }

            // AI Metadata Highlights
            if (entity.aiCaption.isNotEmpty()) {
                Text(
                    text = "\"${entity.aiCaption}\"",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // AI Tags & Scene
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (entity.aiSceneType.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(AmberGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = entity.aiSceneType, color = AmberGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                entity.aiTags.split(",").take(4).forEach { tag ->
                    val cleanTag = tag.trim()
                    if (cleanTag.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "#$cleanTag", color = CyanAccent, fontSize = 9.sp)
                        }
                    }
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onGenerateAi,
                    colors = ButtonDefaults.textButtonColors(contentColor = AmberGold),
                    modifier = Modifier.testTag("item_ai_gen_${entity.id}")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Meta", fontSize = 11.sp)
                }

                TextButton(
                    onClick = onSelect,
                    colors = ButtonDefaults.textButtonColors(contentColor = CyanAccent),
                    modifier = Modifier.testTag("item_inspect_${entity.id}")
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspect", fontSize = 11.sp)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("item_delete_${entity.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 10.sp)
        Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
