package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Iso
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun ExifSummaryView(
    image: ImageMetadata,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exif_summary_view_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Header Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CyanAccent)
                )
                Text(
                    text = "KEY EXIF SUMMARY",
                    color = CyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            TextButton(
                onClick = {
                    val summaryText = buildSummaryText(image)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("EXIF Summary", summaryText))
                    Toast.makeText(context, "EXIF Summary copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("copy_summary_text_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Copy All Summary",
                    color = CyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 1. Camera & Hardware Card
        CameraHardwareCard(image = image, context = context)

        // 2. Exposure & Optics Card (Focal Length, Shutter, Aperture, ISO)
        ExposureOpticsCard(image = image, context = context)

        // 3. Date, Time & Image Geometry Card
        DateTimeGeometryCard(image = image, context = context)

        // 4. Geo-Location & Privacy Telemetry Card
        LocationTelemetryCard(image = image, context = context)

        // 5. Attribution & IPTC Copyright Card
        AttributionIptcCard(image = image, context = context)
    }
}

@Composable
private fun CameraHardwareCard(image: ImageMetadata, context: Context) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("summary_card_camera")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(
                icon = Icons.Default.PhotoCamera,
                iconTint = CyanAccent,
                iconBg = CyanAccent.copy(alpha = 0.15f),
                title = "Camera & Equipment",
                badge = image.make.ifBlank { "Unknown" }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoBlock(
                    label = "CAMERA MODEL",
                    value = if (image.model != "Unknown" && image.model.isNotBlank()) "${image.make} ${image.model}" else "Unknown Camera",
                    icon = Icons.Default.CameraAlt,
                    modifier = Modifier.weight(1f),
                    onCopy = { copyToClipboard(context, "Camera Model", "${image.make} ${image.model}") }
                )
            }

            if (image.lensModel != "N/A" && image.lensModel.isNotBlank()) {
                InfoBlock(
                    label = "LENS PROFILE",
                    value = image.lensModel,
                    icon = Icons.Default.CenterFocusStrong,
                    modifier = Modifier.fillMaxWidth(),
                    onCopy = { copyToClipboard(context, "Lens Model", image.lensModel) }
                )
            }

            if (image.software != "N/A" && image.software.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate800, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Software / Engine", color = Color.Gray, fontSize = 11.sp)
                    Text(text = image.software, color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun ExposureOpticsCard(image: ImageMetadata, context: Context) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("summary_card_exposure")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(
                icon = Icons.Default.Tune,
                iconTint = AmberGold,
                iconBg = AmberGold.copy(alpha = 0.15f),
                title = "Exposure & Optics",
                badge = if (image.focalLength != "N/A") image.focalLength else "Standard"
            )

            // 2x2 Metric Callout Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCallout(
                    label = "SHUTTER SPEED",
                    value = image.exposureTime,
                    icon = Icons.Default.Timer,
                    accentColor = AmberGold,
                    modifier = Modifier.weight(1f),
                    onCopy = { copyToClipboard(context, "Shutter Speed", image.exposureTime) }
                )

                MetricCallout(
                    label = "APERTURE",
                    value = image.aperture,
                    icon = Icons.Default.CenterFocusStrong,
                    accentColor = CyanAccent,
                    modifier = Modifier.weight(1f),
                    onCopy = { copyToClipboard(context, "Aperture", image.aperture) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCallout(
                    label = "ISO SENSITIVITY",
                    value = image.iso,
                    icon = Icons.Default.Iso,
                    accentColor = EmeraldGreen,
                    modifier = Modifier.weight(1f),
                    onCopy = { copyToClipboard(context, "ISO", image.iso) }
                )

                MetricCallout(
                    label = "FOCAL LENGTH",
                    value = image.focalLength,
                    icon = Icons.Default.ZoomIn,
                    accentColor = PurpleAccent,
                    modifier = Modifier.weight(1f),
                    onCopy = { copyToClipboard(context, "Focal Length", image.focalLength) }
                )
            }

            // Flash & White Balance Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate800, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(text = "Flash: ${image.flash}", color = Color.White, fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(text = "WB: ${image.whiteBalance}", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun DateTimeGeometryCard(image: ImageMetadata, context: Context) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("summary_card_timestamp")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(
                icon = Icons.Default.CalendarToday,
                iconTint = EmeraldGreen,
                iconBg = EmeraldGreen.copy(alpha = 0.15f),
                title = "Date & File Geometry",
                badge = "${String.format(Locale.US, "%.1f", image.megapixels)} MP"
            )

            // Date Time Taken
            InfoBlock(
                label = "CAPTURE TIMESTAMP (EXIF DATETIME)",
                value = if (image.dateTime != "N/A" && image.dateTime.isNotBlank()) image.dateTime else "No Timestamp Embedded",
                icon = Icons.Default.CalendarToday,
                modifier = Modifier.fillMaxWidth(),
                onCopy = { copyToClipboard(context, "Capture Date", image.dateTime) }
            )

            // Dimensions and File Size Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoBlock(
                    label = "DIMENSIONS",
                    value = "${image.width} × ${image.height} px",
                    icon = Icons.Default.Image,
                    modifier = Modifier.weight(1f),
                    onCopy = { copyToClipboard(context, "Dimensions", "${image.width}x${image.height}") }
                )

                val fileSizeText = if (image.fileSizeKb >= 1024.0) {
                    String.format(Locale.US, "%.2f MB", image.fileSizeKb / 1024.0)
                } else {
                    String.format(Locale.US, "%.1f KB", image.fileSizeKb)
                }

                InfoBlock(
                    label = "FILE SIZE & MIME",
                    value = "$fileSizeText (${image.mimeType.substringAfter("/")})",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f),
                    onCopy = { copyToClipboard(context, "File Size", fileSizeText) }
                )
            }
        }
    }
}

@Composable
private fun LocationTelemetryCard(image: ImageMetadata, context: Context) {
    val hasGps = image.hasGps && image.latitude != null && image.longitude != null

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("summary_card_location")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(
                icon = if (hasGps) Icons.Default.GpsFixed else Icons.Default.Shield,
                iconTint = if (hasGps) RoseRed else EmeraldGreen,
                iconBg = if (hasGps) RoseRed.copy(alpha = 0.15f) else EmeraldGreen.copy(alpha = 0.15f),
                title = "Geotag & Location Privacy",
                badge = if (hasGps) "GEOTAGGED" else "CLEAN"
            )

            if (hasGps) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoseRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .border(1.dp, RoseRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GPS Coordinates Exposed",
                            color = RoseRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${image.altitude ?: 0.0}m alt",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "${image.latitude}°, ${image.longitude}°",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    if (!image.locationName.isNullOrBlank()) {
                        Text(
                            text = image.locationName,
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val geoUri = Uri.parse("geo:${image.latitude},${image.longitude}?q=${image.latitude},${image.longitude}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Location: ${image.latitude}, ${image.longitude}", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseRed),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("summary_map_button")
                        ) {
                            Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "View Map", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                copyToClipboard(context, "GPS Coordinates", "${image.latitude}, ${image.longitude}")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("summary_copy_gps_button")
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Copy GPS", fontSize = 11.sp)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(22.dp))
                    Column {
                        Text(text = "Privacy Protected (No GPS)", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "No geographical coordinates or altitude records found in EXIF header.", color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttributionIptcCard(image: ImageMetadata, context: Context) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("summary_card_iptc")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CardHeader(
                icon = Icons.Default.Person,
                iconTint = PurpleAccent,
                iconBg = PurpleAccent.copy(alpha = 0.15f),
                title = "Attribution & Rights",
                badge = if (image.artist.isNotBlank()) "IPTC EMBEDDED" else "UNSET"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoBlock(
                    label = "ARTIST / PHOTOGRAPHER",
                    value = image.artist.ifBlank { "Not Specified" },
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f),
                    onCopy = { if (image.artist.isNotBlank()) copyToClipboard(context, "Artist", image.artist) }
                )

                InfoBlock(
                    label = "COPYRIGHT NOTICE",
                    value = image.copyright.ifBlank { "None (Public / Unclaimed)" },
                    icon = Icons.Default.Copyright,
                    modifier = Modifier.weight(1f),
                    onCopy = { if (image.copyright.isNotBlank()) copyToClipboard(context, "Copyright", image.copyright) }
                )
            }

            if (image.userComment.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate800, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(text = "EMBEDDED COMMENT", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = image.userComment, color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CardHeader(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    badge: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .background(Slate800, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = badge.uppercase(),
                color = iconTint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun MetricCallout(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onCopy: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Slate800)
            .clickable(onClick = onCopy)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = if (value.isNotBlank()) value else "N/A",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun InfoBlock(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onCopy: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Slate800)
            .clickable(onClick = onCopy)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(12.dp)
                )
            }

            Text(
                text = value,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Copied $label: $text", Toast.LENGTH_SHORT).show()
}

private fun buildSummaryText(img: ImageMetadata): String {
    val sb = StringBuilder()
    sb.appendLine("══════════════════════════════════════")
    sb.appendLine("📷 EXIF METADATA SUMMARY: ${img.fileName}")
    sb.appendLine("══════════════════════════════════════")
    sb.appendLine("• Camera: ${img.make} ${img.model}")
    if (img.lensModel != "N/A") sb.appendLine("• Lens: ${img.lensModel}")
    sb.appendLine("• Date Taken: ${img.dateTime}")
    sb.appendLine("• Shutter Speed: ${img.exposureTime}")
    sb.appendLine("• Aperture: ${img.aperture}")
    sb.appendLine("• ISO: ${img.iso}")
    sb.appendLine("• Focal Length: ${img.focalLength}")
    sb.appendLine("• Dimensions: ${img.width}x${img.height} (${String.format(Locale.US, "%.1f", img.megapixels)} MP)")
    sb.appendLine("• File Size: ${String.format(Locale.US, "%.1f", img.fileSizeKb)} KB")
    if (img.hasGps && img.latitude != null && img.longitude != null) {
        sb.appendLine("• GPS Location: ${img.latitude}°, ${img.longitude}° (${img.altitude ?: 0.0}m)")
    } else {
        sb.appendLine("• GPS Location: None (Sanitized/No Geotag)")
    }
    if (img.artist.isNotBlank()) sb.appendLine("• Artist: ${img.artist}")
    if (img.copyright.isNotBlank()) sb.appendLine("• Copyright: ${img.copyright}")
    sb.appendLine("══════════════════════════════════════")
    return sb.toString()
}
