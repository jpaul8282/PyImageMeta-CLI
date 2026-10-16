package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_metadata")
data class ImageMetadataEntity(
    @PrimaryKey
    val id: String,
    val fileName: String,
    val title: String,
    val fileSizeKb: Double,
    val mimeType: String = "image/jpeg",
    val width: Int,
    val height: Int,
    val uriString: String? = null,

    // Camera EXIF
    val make: String = "Unknown",
    val model: String = "Unknown",
    val lensModel: String = "N/A",
    val dateTime: String = "N/A",
    val exposureTime: String = "N/A",
    val aperture: String = "N/A",
    val iso: String = "N/A",
    val focalLength: String = "N/A",
    val flash: String = "Off",
    val whiteBalance: String = "Auto",
    val software: String = "Default Camera",

    // GPS Telemetry
    val hasGps: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val locationName: String? = null,

    // IPTC & Attribution
    val artist: String = "",
    val copyright: String = "",
    val userComment: String = "",

    // Technical Hashes
    val md5Hash: String = "",
    val perceptualHash: String = "",

    // AI Meta Generator Attributes
    val aiCaption: String = "",
    val aiTags: String = "",
    val aiSceneType: String = "Standard Scene",
    val aiLighting: String = "Natural Ambient",
    val aiPromptStyle: String = "",
    val isAiSynthesized: Boolean = false,

    val timestamp: Long = System.currentTimeMillis()
)
