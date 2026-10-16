package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Iso
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ImageMetadata
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.RoseRed
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import java.util.Locale

/**
 * Filter categories for metadata items.
 */
enum class MetadataCategory(val displayName: String, val icon: ImageVector) {
    ALL("All Metadata", Icons.Default.Info),
    EXPOSURE("Exposure (ISO/Shutter)", Icons.Default.Timer),
    LOCATION("Location & GPS", Icons.Default.LocationOn),
    HARDWARE("Camera & Lens", Icons.Default.PhotoCamera),
    FILE_INFO("Image Geometry", Icons.Default.Image),
    ATTRIBUTION("IPTC & Copyright", Icons.Default.Person)
}

/**
 * Represents a cleanly structured, readable metadata entry.
 */
data class MetadataItem(
    val id: String,
    val label: String,
    val value: String,
    val category: MetadataCategory,
    val icon: ImageVector,
    val iconTint: Color,
    val secondaryInfo: String? = null,
    val isSensitive: Boolean = false,
    val tagCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

/**
 * A clean, readable list Composable that displays extracted image metadata
 * (such as ISO, shutter speed, location, aperture, camera hardware, and file geometry).
 */
@Composable
fun ExtractedMetadataListView(
    image: ImageMetadata?,
    modifier: Modifier = Modifier,
    showSearchAndFilters: Boolean = true,
    onOpenMap: ((Double, Double) -> Unit)? = null
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(MetadataCategory.ALL) }

    if (image == null) {
        EmptyMetadataPlaceholder(modifier = modifier)
        return
    }

    // Build the structured list of extracted metadata
    val allItems = remember(image) { buildMetadataItems(image) }

    // Filter items based on search query and category
    val filteredItems = remember(allItems, searchQuery, selectedCategory) {
        allItems.filter { item ->
            val matchesCategory = selectedCategory == MetadataCategory.ALL || item.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                item.label.contains(searchQuery, ignoreCase = true) ||
                item.value.contains(searchQuery, ignoreCase = true) ||
                (item.secondaryInfo?.contains(searchQuery, ignoreCase = true) == true) ||
                (item.tagCode?.contains(searchQuery, ignoreCase = true) == true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("extracted_metadata_list_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with count and copy all action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(CyanAccent)
                )
                Text(
                    text = "EXTRACTED METADATA (${allItems.size})",
                    color = CyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            TextButton(
                onClick = {
                    val fullText = buildFullMetadataExportText(image, allItems)
                    copyToClipboard(context, "All Extracted Metadata", fullText)
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.testTag("copy_all_metadata_list_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy all metadata list",
                    tint = CyanAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Copy All List",
                    color = CyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Search & Category Filters
        if (showSearchAndFilters) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_metadata_input"),
                placeholder = {
                    Text(
                        text = "Search ISO, shutter, GPS, lens, model...",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = Slate700,
                    focusedContainerColor = Slate850,
                    unfocusedContainerColor = Slate850,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Category Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MetadataCategory.entries.toTypedArray()) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isSelected) Color.Black else CyanAccent
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = Color.Black,
                            containerColor = Slate850,
                            labelColor = Color.LightGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) CyanAccent else Slate700
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
                    )
                }
            }
        }

        // List of Extracted Metadata Items
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate850, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No metadata matching \"$searchQuery\"",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredItems.forEach { item ->
                    MetadataRowCard(
                        item = item,
                        onCopy = {
                            copyToClipboard(context, item.label, item.value)
                        },
                        onOpenMap = { lat, lon ->
                            if (onOpenMap != null) {
                                onOpenMap(lat, lon)
                            } else {
                                val geoUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
                                val intent = Intent(Intent.ACTION_VIEW, geoUri)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Location: $lat, $lon", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Clean readable list row card for a single metadata entry.
 */
@Composable
private fun MetadataRowCard(
    item: MetadataItem,
    onCopy: () -> Unit,
    onOpenMap: (Double, Double) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSensitive) RoseRed.copy(alpha = 0.08f) else Slate850
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (item.isSensitive) {
            CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(RoseRed.copy(alpha = 0.35f))
            )
        } else {
            CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(Slate800)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("metadata_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = item.iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Primary Label and Extracted Value
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.label.uppercase(),
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    if (item.tagCode != null) {
                        Box(
                            modifier = Modifier
                                .background(Slate800, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = item.tagCode,
                                color = Color.LightGray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (item.isSensitive) {
                        Box(
                            modifier = Modifier
                                .background(RoseRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "SENSITIVE",
                                color = RoseRed,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Extracted Value (Prominent, High-Readability Font)
                Text(
                    text = item.value,
                    color = if (item.isSensitive) RoseRed else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Secondary explanatory text if available
                if (item.secondaryInfo != null) {
                    Text(
                        text = item.secondaryInfo,
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }

            // Actions: Map or Copy
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (item.latitude != null && item.longitude != null) {
                    IconButton(
                        onClick = { onOpenMap(item.latitude, item.longitude) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(RoseRed.copy(alpha = 0.15f), CircleShape)
                            .testTag("metadata_map_btn_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "View Location on Map",
                            tint = RoseRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Slate800, CircleShape)
                        .testTag("metadata_copy_btn_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy ${item.label}",
                        tint = CyanAccent,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * Empty state when no image is loaded.
 */
@Composable
private fun EmptyMetadataPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Slate850, RoundedCornerShape(16.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "No Photo Selected",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select or import a photo to inspect its extracted ISO, shutter speed, location, and hardware tags.",
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Constructs the structured list of metadata items from [ImageMetadata].
 * Features ISO, shutter speed, location prominently along with all camera, geometry, and IPTC tags.
 */
private fun buildMetadataItems(image: ImageMetadata): List<MetadataItem> {
    val items = mutableListOf<MetadataItem>()

    // 1. ISO Sensitivity (High Priority - Requested by User)
    items.add(
        MetadataItem(
            id = "iso",
            label = "ISO Sensitivity",
            value = if (image.iso.isNotBlank() && image.iso != "N/A") "ISO ${image.iso}" else "N/A (Auto / Not Reported)",
            category = MetadataCategory.EXPOSURE,
            icon = Icons.Default.Iso,
            iconTint = EmeraldGreen,
            secondaryInfo = "Sensor amplification sensitivity level",
            tagCode = "EXIF #34855"
        )
    )

    // 2. Shutter Speed / Exposure Time (High Priority - Requested by User)
    items.add(
        MetadataItem(
            id = "shutter_speed",
            label = "Shutter Speed (Exposure)",
            value = if (image.exposureTime.isNotBlank() && image.exposureTime != "N/A") image.exposureTime else "N/A",
            category = MetadataCategory.EXPOSURE,
            icon = Icons.Default.Timer,
            iconTint = AmberGold,
            secondaryInfo = "Duration of camera shutter curtain open",
            tagCode = "EXIF #33434"
        )
    )

    // 3. GPS Location (High Priority - Requested by User)
    if (image.hasGps && image.latitude != null && image.longitude != null) {
        val latStr = String.format(Locale.US, "%.5f°", image.latitude)
        val lonStr = String.format(Locale.US, "%.5f°", image.longitude)
        val locationDesc = if (!image.locationName.isNullOrBlank()) {
            "${image.locationName} · Alt: ${image.altitude ?: 0.0}m"
        } else {
            "WGS-84 Geodetic Coordinates · Alt: ${image.altitude ?: 0.0}m"
        }

        items.add(
            MetadataItem(
                id = "location",
                label = "GPS Location (Geotag)",
                value = "$latStr, $lonStr",
                category = MetadataCategory.LOCATION,
                icon = Icons.Default.LocationOn,
                iconTint = RoseRed,
                secondaryInfo = locationDesc,
                isSensitive = true,
                tagCode = "GPS #0002 / #0004",
                latitude = image.latitude,
                longitude = image.longitude
            )
        )
    } else {
        items.add(
            MetadataItem(
                id = "location_clean",
                label = "GPS Location (Geotag)",
                value = "No GPS Embedded (Sanitized / Private)",
                category = MetadataCategory.LOCATION,
                icon = Icons.Default.Shield,
                iconTint = EmeraldGreen,
                secondaryInfo = "Zero geodetic coordinates detected in EXIF header",
                tagCode = "GPS Clean"
            )
        )
    }

    // 4. Aperture (F-Number)
    items.add(
        MetadataItem(
            id = "aperture",
            label = "Aperture (F-Stop)",
            value = if (image.aperture.isNotBlank() && image.aperture != "N/A") image.aperture else "N/A",
            category = MetadataCategory.EXPOSURE,
            icon = Icons.Default.CenterFocusStrong,
            iconTint = CyanAccent,
            secondaryInfo = "Lens optical diaphragm opening ratio",
            tagCode = "EXIF #33437"
        )
    )

    // 5. Focal Length
    items.add(
        MetadataItem(
            id = "focal_length",
            label = "Focal Length",
            value = if (image.focalLength.isNotBlank() && image.focalLength != "N/A") image.focalLength else "N/A",
            category = MetadataCategory.EXPOSURE,
            icon = Icons.Default.ZoomIn,
            iconTint = PurpleAccent,
            secondaryInfo = "Physical distance from lens optical center to sensor",
            tagCode = "EXIF #37386"
        )
    )

    // 6. Camera Manufacturer & Model
    val cameraHardware = if (image.model != "Unknown" && image.model.isNotBlank()) {
        "${image.make} ${image.model}"
    } else {
        image.make.ifBlank { "Unknown Camera" }
    }
    items.add(
        MetadataItem(
            id = "camera_hardware",
            label = "Camera Hardware",
            value = cameraHardware,
            category = MetadataCategory.HARDWARE,
            icon = Icons.Default.PhotoCamera,
            iconTint = CyanAccent,
            secondaryInfo = "Manufacturer Make & Body Model",
            tagCode = "EXIF #010F / #0110"
        )
    )

    // 7. Lens Profile
    if (image.lensModel.isNotBlank() && image.lensModel != "N/A") {
        items.add(
            MetadataItem(
                id = "lens_model",
                label = "Lens Model",
                value = image.lensModel,
                category = MetadataCategory.HARDWARE,
                icon = Icons.Default.Lens,
                iconTint = AmberGold,
                secondaryInfo = "Optical glass specifications & profile",
                tagCode = "EXIF #A434"
            )
        )
    }

    // 8. Capture Date & Time
    if (image.dateTime.isNotBlank() && image.dateTime != "N/A") {
        items.add(
            MetadataItem(
                id = "date_time",
                label = "Date & Time Taken",
                value = image.dateTime,
                category = MetadataCategory.FILE_INFO,
                icon = Icons.Default.CalendarToday,
                iconTint = EmeraldGreen,
                secondaryInfo = "Timestamp written by camera firmware",
                tagCode = "EXIF #9003"
            )
        )
    }

    // 9. Dimensions & Megapixels
    val mpText = String.format(Locale.US, "%.1f MP", image.megapixels)
    items.add(
        MetadataItem(
            id = "dimensions",
            label = "Image Dimensions",
            value = "${image.width} × ${image.height} px ($mpText)",
            category = MetadataCategory.FILE_INFO,
            icon = Icons.Default.Image,
            iconTint = CyanAccent,
            secondaryInfo = "Pixel raster resolution",
            tagCode = "EXIF #A002 / #A003"
        )
    )

    // 10. File Size & MIME
    val sizeText = if (image.fileSizeKb >= 1024.0) {
        String.format(Locale.US, "%.2f MB", image.fileSizeKb / 1024.0)
    } else {
        String.format(Locale.US, "%.1f KB", image.fileSizeKb)
    }
    items.add(
        MetadataItem(
            id = "file_size",
            label = "File Size & Format",
            value = "$sizeText (${image.mimeType})",
            category = MetadataCategory.FILE_INFO,
            icon = Icons.Default.Storage,
            iconTint = PurpleAccent,
            secondaryInfo = "On-disk compressed binary storage",
            tagCode = "Header"
        )
    )

    // 11. Flash Mode
    items.add(
        MetadataItem(
            id = "flash",
            label = "Flash Mode",
            value = image.flash,
            category = MetadataCategory.EXPOSURE,
            icon = Icons.Default.FlashOn,
            iconTint = AmberGold,
            secondaryInfo = "Strobe illumination trigger status",
            tagCode = "EXIF #9209"
        )
    )

    // 12. White Balance
    items.add(
        MetadataItem(
            id = "white_balance",
            label = "White Balance",
            value = image.whiteBalance,
            category = MetadataCategory.EXPOSURE,
            icon = Icons.Default.WbSunny,
            iconTint = CyanAccent,
            secondaryInfo = "Color temperature calibration setting",
            tagCode = "EXIF #A405"
        )
    )

    // 13. Software / Camera Firmware
    if (image.software.isNotBlank() && image.software != "N/A") {
        items.add(
            MetadataItem(
                id = "software",
                label = "Software / Engine",
                value = image.software,
                category = MetadataCategory.HARDWARE,
                icon = Icons.Default.CameraAlt,
                iconTint = Color.LightGray,
                secondaryInfo = "Camera firmware or post-processing tool",
                tagCode = "EXIF #0131"
            )
        )
    }

    // 14. Artist / Author (IPTC)
    if (image.artist.isNotBlank()) {
        items.add(
            MetadataItem(
                id = "artist",
                label = "Artist / Photographer",
                value = image.artist,
                category = MetadataCategory.ATTRIBUTION,
                icon = Icons.Default.Person,
                iconTint = PurpleAccent,
                secondaryInfo = "IPTC creator metadata field",
                tagCode = "EXIF #013B"
            )
        )
    }

    // 15. Copyright Notice
    if (image.copyright.isNotBlank()) {
        items.add(
            MetadataItem(
                id = "copyright",
                label = "Copyright Notice",
                value = image.copyright,
                category = MetadataCategory.ATTRIBUTION,
                icon = Icons.Default.Copyright,
                iconTint = AmberGold,
                secondaryInfo = "Intellectual property ownership record",
                tagCode = "EXIF #8298"
            )
        )
    }

    return items
}

/**
 * Copies plain text to Android Clipboard with a user Toast.
 */
private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
}

/**
 * Builds a clean, readable text export of all metadata items.
 */
private fun buildFullMetadataExportText(image: ImageMetadata, items: List<MetadataItem>): String {
    val sb = StringBuilder()
    sb.appendLine("══════════════════════════════════════════════════")
    sb.appendLine("📷 EXTRACTED IMAGE METADATA: ${image.fileName}")
    sb.appendLine("══════════════════════════════════════════════════")
    items.forEach { item ->
        val tagPrefix = if (item.tagCode != null) "[${item.tagCode}] " else ""
        sb.appendLine("• $tagPrefix${item.label}: ${item.value}")
        if (item.secondaryInfo != null) {
            sb.appendLine("    ↳ ${item.secondaryInfo}")
        }
    }
    sb.appendLine("══════════════════════════════════════════════════")
    return sb.toString()
}
