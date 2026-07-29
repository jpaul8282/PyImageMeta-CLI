package com.example.model

import android.graphics.Bitmap

enum class PrivacyStatus {
    SECURE_NO_GPS,
    EXPOSED_GPS_TAGGED,
    MODIFIED_SANITIZED
}

data class ExifTagItem(
    val category: String, // e.g. "Camera", "GPS", "Image", "IPTC"
    val tagName: String,
    val tagValue: String,
    val isSensitive: Boolean = false
)

data class ImageMetadata(
    val id: String,
    val title: String,
    val fileName: String,
    val fileSizeKb: Double,
    val mimeType: String = "image/jpeg",
    val width: Int,
    val height: Int,
    val bitmap: Bitmap? = null,
    val sampleResId: Int? = null,
    
    // Camera EXIF
    val make: String = "Unknown",
    val model: String = "Unknown",
    val lensModel: String = "N/A",
    val dateTime: String = "N/A",
    val exposureTime: String = "N/A",
    val aperture: String = "N/A",
    val iso: String = "N/A",
    val focalLength: String = "N/A",
    val flash: String = "Off, Did not fire",
    val whiteBalance: String = "Auto",
    val software: String = "Default Camera",

    // GPS Metadata
    val hasGps: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val locationName: String? = null,

    // IPTC / Custom Tagging
    val artist: String = "",
    val copyright: String = "",
    val userComment: String = "",

    // Technical Digests
    val md5Hash: String = "",
    val perceptualHash: String = "",

    // Tags List
    val allTags: List<ExifTagItem> = emptyList()
) {
    val megapixels: Double
        get() = (width * height) / 1_000_000.0

    val privacyStatus: PrivacyStatus
        get() = when {
            hasGps && latitude != null -> PrivacyStatus.EXPOSED_GPS_TAGGED
            copyright.isNotEmpty() || artist.isNotEmpty() -> PrivacyStatus.MODIFIED_SANITIZED
            else -> PrivacyStatus.SECURE_NO_GPS
        }
}
