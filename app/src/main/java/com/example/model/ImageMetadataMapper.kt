package com.example.model

import com.example.db.ImageMetadataEntity

fun ImageMetadata.toEntity(
    aiCaption: String = "",
    aiTags: String = "",
    aiSceneType: String = "Standard Scene",
    aiLighting: String = "Natural Ambient",
    aiPromptStyle: String = "",
    isAiSynthesized: Boolean = false,
    uriString: String? = null
): ImageMetadataEntity {
    return ImageMetadataEntity(
        id = this.id,
        fileName = this.fileName,
        title = this.title,
        fileSizeKb = this.fileSizeKb,
        mimeType = this.mimeType,
        width = this.width,
        height = this.height,
        uriString = uriString,
        make = this.make,
        model = this.model,
        lensModel = this.lensModel,
        dateTime = this.dateTime,
        exposureTime = this.exposureTime,
        aperture = this.aperture,
        iso = this.iso,
        focalLength = this.focalLength,
        flash = this.flash,
        whiteBalance = this.whiteBalance,
        software = this.software,
        hasGps = this.hasGps,
        latitude = this.latitude,
        longitude = this.longitude,
        altitude = this.altitude,
        locationName = this.locationName,
        artist = this.artist,
        copyright = this.copyright,
        userComment = this.userComment,
        md5Hash = this.md5Hash,
        perceptualHash = this.perceptualHash,
        aiCaption = aiCaption,
        aiTags = aiTags,
        aiSceneType = aiSceneType,
        aiLighting = aiLighting,
        aiPromptStyle = aiPromptStyle,
        isAiSynthesized = isAiSynthesized
    )
}

fun ImageMetadataEntity.toModel(): ImageMetadata {
    val tags = mutableListOf<ExifTagItem>()
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
    if (hasGps && latitude != null && longitude != null) {
        tags.add(ExifTagItem("GPS", "Latitude", "$latitude°", isSensitive = true))
        tags.add(ExifTagItem("GPS", "Longitude", "$longitude°", isSensitive = true))
        if (altitude != null) tags.add(ExifTagItem("GPS", "Altitude", "${altitude}m", isSensitive = true))
    }
    if (aiCaption.isNotEmpty()) {
        tags.add(ExifTagItem("AI", "Caption", aiCaption))
    }
    if (aiTags.isNotEmpty()) {
        tags.add(ExifTagItem("AI", "Tags", aiTags))
    }
    if (aiSceneType.isNotEmpty()) {
        tags.add(ExifTagItem("AI", "SceneType", aiSceneType))
    }

    return ImageMetadata(
        id = id,
        title = title,
        fileName = fileName,
        fileSizeKb = fileSizeKb,
        mimeType = mimeType,
        width = width,
        height = height,
        bitmap = null, // Dynamically loaded if needed or generated
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
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        locationName = locationName,
        artist = artist,
        copyright = copyright,
        userComment = userComment,
        md5Hash = md5Hash,
        perceptualHash = perceptualHash,
        allTags = tags
    )
}
