package com.example.engine

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.example.model.ExifTagItem
import com.example.model.ImageMetadata
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID

object ExifEngine {

    fun parseImageFromUri(context: Context, uri: Uri): ImageMetadata? {
        return try {
            val contentResolver = context.contentResolver
            
            // Get dimensions
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, boundsOptions)
            }
            val width = if (boundsOptions.outWidth > 0) boundsOptions.outWidth else 1920
            val height = if (boundsOptions.outHeight > 0) boundsOptions.outHeight else 1080

            // Load bitmap sample for preview
            val sampleOptions = BitmapFactory.Options().apply { inSampleSize = 4 }
            val bitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, sampleOptions)
            }

            // Calculate MD5 hash
            val md5 = contentResolver.openInputStream(uri)?.use { calculateMd5(it) } ?: "unknown_hash"

            // Parse EXIF
            val tags = mutableListOf<ExifTagItem>()
            var make = "Unknown"
            var model = "Unknown"
            var lensModel = "N/A"
            var dateTime = "N/A"
            var exposureTime = "N/A"
            var aperture = "N/A"
            var iso = "N/A"
            var focalLength = "N/A"
            var flash = "N/A"
            var whiteBalance = "Auto"
            var software = "N/A"
            var artist = ""
            var copyright = ""
            var userComment = ""
            var hasGps = false
            var lat: Double? = null
            var lon: Double? = null
            var alt: Double? = null

            contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)

                make = exif.getAttribute(ExifInterface.TAG_MAKE) ?: "Unknown"
                model = exif.getAttribute(ExifInterface.TAG_MODEL) ?: "Unknown"
                lensModel = exif.getAttribute(ExifInterface.TAG_LENS_MODEL) ?: "N/A"
                dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME) ?: "N/A"
                exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let { "${it}s" } ?: "N/A"
                aperture = exif.getAttribute(ExifInterface.TAG_F_NUMBER)?.let { "f/$it" } ?: "N/A"
                iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
                    ?: @Suppress("DEPRECATION") exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)
                    ?: "N/A"
                focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let { "${it}mm" } ?: "N/A"
                flash = exif.getAttribute(ExifInterface.TAG_FLASH) ?: "Off"
                whiteBalance = exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE) ?: "Auto"
                software = exif.getAttribute(ExifInterface.TAG_SOFTWARE) ?: "N/A"
                artist = exif.getAttribute(ExifInterface.TAG_ARTIST) ?: ""
                copyright = exif.getAttribute(ExifInterface.TAG_COPYRIGHT) ?: ""
                userComment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT) ?: ""

                val latLong = exif.latLong
                if (latLong != null && latLong.size == 2) {
                    hasGps = true
                    lat = latLong[0]
                    lon = latLong[1]
                    alt = exif.getAltitude(0.0)
                }

                // Populate tags list
                tags.add(ExifTagItem("Camera", "Make", make))
                tags.add(ExifTagItem("Camera", "Model", model))
                if (lensModel != "N/A") tags.add(ExifTagItem("Lens", "LensModel", lensModel))
                tags.add(ExifTagItem("Exposure", "ExposureTime", exposureTime))
                tags.add(ExifTagItem("Exposure", "FNumber", aperture))
                tags.add(ExifTagItem("Exposure", "ISO", iso))
                if (focalLength != "N/A") tags.add(ExifTagItem("Exposure", "FocalLength", focalLength))
                if (dateTime != "N/A") tags.add(ExifTagItem("Image", "DateTime", dateTime))
                if (artist.isNotEmpty()) tags.add(ExifTagItem("IPTC", "Artist", artist))
                if (copyright.isNotEmpty()) tags.add(ExifTagItem("IPTC", "Copyright", copyright))

                if (hasGps && lat != null && lon != null) {
                    tags.add(ExifTagItem("GPS", "Latitude", "$lat°", isSensitive = true))
                    tags.add(ExifTagItem("GPS", "Longitude", "$lon°", isSensitive = true))
                    if (alt != null) tags.add(ExifTagItem("GPS", "Altitude", "${alt}m", isSensitive = true))
                }
            }

            val fileName = getFileNameFromUri(uri)
            val fileSizeKb = (contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L) / 1024.0

            ImageMetadata(
                id = UUID.randomUUID().toString(),
                title = fileName.substringBeforeLast("."),
                fileName = fileName,
                fileSizeKb = if (fileSizeKb > 0) fileSizeKb else 1024.0,
                width = width,
                height = height,
                bitmap = bitmap,
                make = make,
                model = model,
                lensModel = lensModel,
                dateTime = dateTime,
                exposureTime = exposureTime,
                aperture = aperture,
                iso = iso,
                focalLength = focalLength,
                flash = flash,
                whiteBalance = whiteBalance,
                software = software,
                hasGps = hasGps,
                latitude = lat,
                longitude = lon,
                altitude = alt,
                locationName = if (hasGps) "Lat: $lat, Lon: $lon" else null,
                artist = artist,
                copyright = copyright,
                userComment = userComment,
                md5Hash = md5,
                perceptualHash = md5.take(16),
                allTags = tags
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        val path = uri.path ?: return "imported_image.jpg"
        return path.substringAfterLast("/")
    }

    private fun calculateMd5(inputStream: InputStream): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
            val digest = md.digest()
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "d41d8cd98f00b204e9800998ecf8427e"
        }
    }

    fun stripGpsData(image: ImageMetadata): ImageMetadata {
        val updatedTags = image.allTags.filterNot { it.category == "GPS" }
        return image.copy(
            hasGps = false,
            latitude = null,
            longitude = null,
            altitude = null,
            locationName = null,
            allTags = updatedTags
        )
    }

    fun stripAllMetadata(image: ImageMetadata): ImageMetadata {
        return image.copy(
            make = "Sanitized",
            model = "Sanitized",
            lensModel = "Sanitized",
            dateTime = "Sanitized",
            artist = "",
            copyright = "",
            userComment = "",
            hasGps = false,
            latitude = null,
            longitude = null,
            altitude = null,
            locationName = null,
            allTags = emptyList()
        )
    }

    fun setArtistAndCopyright(image: ImageMetadata, artist: String, copyright: String): ImageMetadata {
        val newTags = image.allTags.filterNot { it.tagName == "Artist" || it.tagName == "Copyright" }.toMutableList()
        if (artist.isNotEmpty()) newTags.add(ExifTagItem("IPTC", "Artist", artist))
        if (copyright.isNotEmpty()) newTags.add(ExifTagItem("IPTC", "Copyright", copyright))

        return image.copy(
            artist = artist,
            copyright = copyright,
            allTags = newTags
        )
    }
}
