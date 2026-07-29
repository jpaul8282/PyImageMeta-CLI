package com.example.data

import android.graphics.*
import com.example.model.ExifTagItem
import com.example.model.ImageMetadata
import java.security.MessageDigest
import java.util.UUID

object SampleDataProvider {

    fun generateSampleImages(): List<ImageMetadata> {
        val img1 = createSampleBitmap("Alpine Summit Vista", Color.parseColor("#1E3A8A"), Color.parseColor("#06B6D4"))
        val img2 = createSampleBitmap("Neon Cyber Street", Color.parseColor("#4C1D95"), Color.parseColor("#EC4899"))
        val img3 = createSampleBitmap("Golden Gate Architecture", Color.parseColor("#7C2D12"), Color.parseColor("#F59E0B"))
        val img4 = createSampleBitmap("Minimalist Studio Portrait", Color.parseColor("#18181B"), Color.parseColor("#71717A"))

        return listOf(
            ImageMetadata(
                id = "sample_1",
                title = "Alpine Summit Vista",
                fileName = "IMG_20260728_142010.jpg",
                fileSizeKb = 3420.5,
                mimeType = "image/jpeg",
                width = 4032,
                height = 3024,
                bitmap = img1,
                make = "Nikon",
                model = "Nikon Z7 II",
                lensModel = "NIKKOR Z 24-70mm f/2.8 S",
                dateTime = "2026-07-28 14:20:10",
                exposureTime = "1/1000s",
                aperture = "f/4.0",
                iso = "100",
                focalLength = "35mm",
                flash = "Off, Did not fire",
                whiteBalance = "Daylight",
                software = "Adobe Lightroom v13.2",
                hasGps = true,
                latitude = 46.5197,
                longitude = 6.6323,
                altitude = 2450.0,
                locationName = "Swiss Alps (Lat 46.5197, Lon 6.6323)",
                artist = "Elena Rostova",
                copyright = "© 2026 Elena Rostova Photography",
                userComment = "High altitude HDR panorama shot",
                md5Hash = "a3f9e871b209d84c56e7f123456789ab",
                perceptualHash = "1100101011010001",
                allTags = listOf(
                    ExifTagItem("Camera", "Make", "Nikon"),
                    ExifTagItem("Camera", "Model", "Nikon Z7 II"),
                    ExifTagItem("Lens", "LensModel", "NIKKOR Z 24-70mm f/2.8 S"),
                    ExifTagItem("Exposure", "ExposureTime", "1/1000s"),
                    ExifTagItem("Exposure", "FNumber", "f/4.0"),
                    ExifTagItem("Exposure", "ISO", "100"),
                    ExifTagItem("GPS", "Latitude", "46.5197° N", isSensitive = true),
                    ExifTagItem("GPS", "Longitude", "6.6323° E", isSensitive = true),
                    ExifTagItem("GPS", "Altitude", "2450.0 m", isSensitive = true),
                    ExifTagItem("IPTC", "Artist", "Elena Rostova"),
                    ExifTagItem("IPTC", "Copyright", "© 2026 Elena Rostova Photography")
                )
            ),
            ImageMetadata(
                id = "sample_2",
                title = "Neon Cyber Street",
                fileName = "DSC09241.jpg",
                fileSizeKb = 5120.0,
                mimeType = "image/jpeg",
                width = 6000,
                height = 4000,
                bitmap = img2,
                make = "Sony",
                model = "ILCE-7M4 (Sony A7 IV)",
                lensModel = "FE 85mm F1.4 GM",
                dateTime = "2026-07-25 22:15:44",
                exposureTime = "1/160s",
                aperture = "f/1.4",
                iso = "1600",
                focalLength = "85mm",
                flash = "Off",
                whiteBalance = "Custom (3200K)",
                software = "Sony Imaging Edge",
                hasGps = true,
                latitude = 35.6895,
                longitude = 139.6917,
                altitude = 45.2,
                locationName = "Shinjuku, Tokyo (Lat 35.6895, Lon 139.6917)",
                artist = "Kenji Sato",
                copyright = "© 2026 CyberLens Media",
                userComment = "Low light urban rain reflection",
                md5Hash = "9d8c7b6a5f4e3d2c1b0a9f8e7d6c5b4a",
                perceptualHash = "0110110010011100",
                allTags = listOf(
                    ExifTagItem("Camera", "Make", "Sony"),
                    ExifTagItem("Camera", "Model", "ILCE-7M4"),
                    ExifTagItem("Lens", "LensModel", "FE 85mm F1.4 GM"),
                    ExifTagItem("Exposure", "ExposureTime", "1/160s"),
                    ExifTagItem("Exposure", "FNumber", "f/1.4"),
                    ExifTagItem("Exposure", "ISO", "1600"),
                    ExifTagItem("GPS", "Latitude", "35.6895° N", isSensitive = true),
                    ExifTagItem("GPS", "Longitude", "139.6917° E", isSensitive = true),
                    ExifTagItem("IPTC", "Artist", "Kenji Sato"),
                    ExifTagItem("IPTC", "Copyright", "© 2026 CyberLens Media")
                )
            ),
            ImageMetadata(
                id = "sample_3",
                title = "Golden Gate Architecture",
                fileName = "CR3_0014.jpg",
                fileSizeKb = 4100.8,
                mimeType = "image/jpeg",
                width = 8192,
                height = 5464,
                bitmap = img3,
                make = "Canon",
                model = "Canon EOS R5",
                lensModel = "RF 15-35mm F2.8 L IS USM",
                dateTime = "2026-06-12 18:45:00",
                exposureTime = "1/30s",
                aperture = "f/8.0",
                iso = "200",
                focalLength = "24mm",
                flash = "Off",
                whiteBalance = "Auto",
                software = "Digital Photo Professional",
                hasGps = false,
                latitude = null,
                longitude = null,
                altitude = null,
                locationName = "San Francisco, CA (GPS Stripped)",
                artist = "Marcus Vance",
                copyright = "CC BY-SA 4.0",
                userComment = "Long exposure sunset mist",
                md5Hash = "7f6e5d4c3b2a109876543210fedcba98",
                perceptualHash = "1010101001010101",
                allTags = listOf(
                    ExifTagItem("Camera", "Make", "Canon"),
                    ExifTagItem("Camera", "Model", "Canon EOS R5"),
                    ExifTagItem("Lens", "LensModel", "RF 15-35mm F2.8 L IS USM"),
                    ExifTagItem("Exposure", "ExposureTime", "1/30s"),
                    ExifTagItem("Exposure", "FNumber", "f/8.0"),
                    ExifTagItem("Exposure", "ISO", "200"),
                    ExifTagItem("IPTC", "Artist", "Marcus Vance"),
                    ExifTagItem("IPTC", "Copyright", "CC BY-SA 4.0")
                )
            ),
            ImageMetadata(
                id = "sample_4",
                title = "Minimalist Studio Portrait",
                fileName = "STUDIO_881.jpg",
                fileSizeKb = 6800.2,
                mimeType = "image/jpeg",
                width = 11656,
                height = 8742,
                bitmap = img4,
                make = "Hasselblad",
                model = "Hasselblad X2D 100C",
                lensModel = "XCD 2,5/90V",
                dateTime = "2026-07-01 11:00:15",
                exposureTime = "1/250s",
                aperture = "f/5.6",
                iso = "64",
                focalLength = "90mm",
                flash = "Fired, Strobe Sync",
                whiteBalance = "Flash (5500K)",
                software = "Phocus v3.7",
                hasGps = false,
                latitude = null,
                longitude = null,
                artist = "Studio Apex",
                copyright = "© 2026 Apex Commercial Rights",
                userComment = "100MP Medium Format Portrait",
                md5Hash = "e1d2c3b4a5968778695041322100ffaa",
                perceptualHash = "1111000011110000",
                allTags = listOf(
                    ExifTagItem("Camera", "Make", "Hasselblad"),
                    ExifTagItem("Camera", "Model", "Hasselblad X2D 100C"),
                    ExifTagItem("Lens", "LensModel", "XCD 2,5/90V"),
                    ExifTagItem("Exposure", "ExposureTime", "1/250s"),
                    ExifTagItem("Exposure", "FNumber", "f/5.6"),
                    ExifTagItem("Exposure", "ISO", "64"),
                    ExifTagItem("IPTC", "Artist", "Studio Apex"),
                    ExifTagItem("IPTC", "Copyright", "© 2026 Apex Commercial Rights")
                )
            )
        )
    }

    private fun createSampleBitmap(title: String, startColor: Int, endColor: Int): Bitmap {
        val width = 800
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), startColor, endColor, Shader.TileMode.CLAMP)
        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Draw geometric grid / aperture patterns
        paint.shader = null
        paint.color = Color.WHITE
        paint.alpha = 30
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f

        for (i in 50..750 step 80) {
            canvas.drawCircle(width / 2f, height / 2f, i.toFloat() / 2, paint)
        }

        // Draw title text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 4f, Color.BLACK)
        }
        canvas.drawText(title, width / 2f, height / 2f, textPaint)

        // Draw aperture overlay badge
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
            textSize = 22f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("[ EXIF METADATA EMBEDDED ]", width / 2f, height / 2f + 50f, badgePaint)

        return bitmap
    }
}
