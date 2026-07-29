package com.example.export

import com.example.model.ImageMetadata

object ExifCsvExporter {

    private val HEADERS = listOf(
        "File Name",
        "Title",
        "Width",
        "Height",
        "Megapixels",
        "File Size (KB)",
        "Make",
        "Model",
        "Lens Model",
        "Date Time",
        "Exposure Time",
        "Aperture",
        "ISO",
        "Focal Length",
        "Flash",
        "White Balance",
        "Software",
        "GPS Embedded",
        "Latitude",
        "Longitude",
        "Altitude",
        "Artist",
        "Copyright",
        "User Comment",
        "MD5 Hash",
        "Perceptual Hash"
    )

    fun generateCsv(images: List<ImageMetadata>): String {
        val sb = StringBuilder()
        // Header
        sb.appendLine(HEADERS.joinToString(",") { escapeCsv(it) })

        // Rows
        images.forEach { img ->
            val row = listOf(
                img.fileName,
                img.title,
                img.width.toString(),
                img.height.toString(),
                "%.2f".format(img.megapixels),
                "%.1f".format(img.fileSizeKb),
                img.make,
                img.model,
                img.lensModel,
                img.dateTime,
                img.exposureTime,
                img.aperture,
                img.iso,
                img.focalLength,
                img.flash,
                img.whiteBalance,
                img.software,
                img.hasGps.toString(),
                img.latitude?.toString() ?: "",
                img.longitude?.toString() ?: "",
                img.altitude?.toString() ?: "",
                img.artist,
                img.copyright,
                img.userComment,
                img.md5Hash,
                img.perceptualHash
            )
            sb.appendLine(row.joinToString(",") { escapeCsv(it) })
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
