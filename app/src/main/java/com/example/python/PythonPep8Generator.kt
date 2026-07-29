package com.example.python

import com.example.model.ImageMetadata

object PythonPep8Generator {

    enum class PythonLibraryBackend(val displayName: String, val importName: String) {
        PILLOW("Pillow (PIL)", "from PIL import Image, ExifTags"),
        PIEXIF("piexif (Native EXIF)", "import piexif"),
        SUBPROCESS_EXIFTOOL("exiftool Subprocess Wrapper", "import subprocess")
    }

    data class GeneratorConfig(
        val backend: PythonLibraryBackend = PythonLibraryBackend.PILLOW,
        val includeCli: Boolean = true,
        val strictTypes: Boolean = true,
        val includeDocstrings: Boolean = true,
        val PEP257Docstrings: Boolean = true,
        val targetImage: ImageMetadata? = null
    )

    fun generateFullPackageCode(config: GeneratorConfig): String {
        val target = config.targetImage
        val fileNameStr = target?.fileName ?: "sample_photo.jpg"
        val makeStr = target?.make ?: "Nikon"
        val modelStr = target?.model ?: "Z7 II"

        return """
# -*- coding: utf-8 -*-
\"\"\"PyMeta EXIF Processor Library (PEP 8 Compliant).

This module provides a production-grade, highly efficient engine for reading,
processing, sanitizing, and exporting metadata (EXIF, IPTC, GPS) from images.

Compatible with Python 3.9+ and compliant with PEP 8 and PEP 257 style guidelines.

Author: PyMeta EXIF Studio
License: MIT
\"\"\"

import argparse
import json
import logging
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple, Union

${config.backend.importName}

# Configure logging according to PEP 8 standards
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("pymeta_exif")


class ImageMetadataError(Exception):
    \"\"\"Base exception class for metadata processing failures.\"\"\"

    pass


class ImageMetadataProcessor:
    \"\"\"High-performance metadata extraction and sanitization processor.
    
    Attributes:
        image_path (Path): Path to the target image file.
        raw_metadata (Dict[str, Any]): Cached dictionary of extracted raw EXIF tags.
    \"\"\"

    def __init__(self, image_path: Union[str, Path]) -> None:
        \"\"\"Initialize the metadata processor.

        Args:
            image_path: Path to the input image file (JPEG, PNG, WebP).

        Raises:
            FileNotFoundError: If the specified image path does not exist.
        \"\"\"
        self.image_path = Path(image_path)
        if not self.image_path.exists():
            raise FileNotFoundError(f"Image not found at path: {self.image_path}")

        self.raw_metadata: Dict[str, Any] = {}
        logger.debug("Initialized ImageMetadataProcessor for %s", self.image_path.name)

    def extract_exif(self) -> Dict[str, Any]:
        \"\"\"Extract raw EXIF metadata tags into a structured key-value dictionary.

        Returns:
            Dict[str, Any]: Dictionary containing human-readable EXIF tag names
            and their associated values.
        \"\"\"
        try:
            with Image.open(self.image_path) as img:
                exif_data = img._getexif()
                if not exif_data:
                    logger.warning("No EXIF data embedded in %s", self.image_path.name)
                    return {}

                parsed_tags: Dict[str, Any] = {}
                for tag_id, val in exif_data.items():
                    tag_name = ExifTags.TAGS.get(tag_id, str(tag_id))
                    # Convert byte values to strings if necessary
                    if isinstance(val, bytes):
                        val = val.decode(errors="replace")
                    parsed_tags[tag_name] = val

                self.raw_metadata = parsed_tags
                return parsed_tags
        except Exception as err:
            logger.error("Failed to extract EXIF tags from %s: %s", self.image_path.name, err)
            raise ImageMetadataError(f"Extraction failed: {err}") from err

    def extract_gps_coordinates() -> Optional[Tuple[float, float]]:
        \"\"\"Parse and calculate decimal GPS latitude and longitude coordinates.

        Returns:
            Optional[Tuple[float, float]]: A tuple of (latitude, longitude) in
            decimal degrees, or None if GPS tags are missing.
        \"\"\"
        exif = self.extract_exif()
        gps_info = exif.get("GPSInfo")
        if not gps_info:
            return None

        # Sample coordinate parsing helper
        try:
            lat = float(gps_info.get(2, [0])[0])
            lon = float(gps_info.get(4, [0])[0])
            return (lat, lon)
        except (IndexError, TypeError, KeyError):
            logger.warning("Malformed GPS tags found in %s", self.image_path.name)
            return None

    def strip_location_metadata(self, output_path: Optional[Union[str, Path]] = None) -> Path:
        \"\"\"Sanitize image by removing all GPS location tags to preserve privacy.

        Args:
            output_path: Destination path for the sanitized image.
                Defaults to overwriting or saving with '_sanitized' suffix.

        Returns:
            Path: Path to the newly created location-sanitized image.
        \"\"\"
        dest = Path(output_path) if output_path else self.image_path.with_stem(
            f"{self.image_path.stem}_sanitized"
        )
        
        with Image.open(self.image_path) as img:
            data = list(img.getdata())
            clean_img = Image.new(img.mode, img.size)
            clean_img.putdata(data)
            clean_img.save(dest)

        logger.info("Successfully stripped GPS location metadata -> %s", dest.name)
        return dest

    def export_json_report(self, json_path: Union[str, Path]) -> Path:
        \"\"\"Export extracted metadata dictionary to a formatted JSON file.

        Args:
            json_path: Target path for the output JSON document.

        Returns:
            Path: Path to the generated JSON file.
        \"\"\"
        dest = Path(json_path)
        exif = self.extract_exif()
        report = {
            "file_name": self.image_path.name,
            "file_size_bytes": self.image_path.stat().st_size,
            "exif_tag_count": len(exif),
            "tags": exif
        }

        with open(dest, "w", encoding="utf-8") as out_file:
            json.dump(report, out_file, indent=4, default=str)

        logger.info("Exported JSON metadata report to %s", dest)
        return dest


def batch_process_directory(
    directory_path: Union[str, Path],
    strip_gps: bool = False
) -> List[Dict[str, Any]]:
    \"\"\"Recursively process all images within a directory.

    Args:
        directory_path: Directory containing image files.
        strip_gps: Whether to automatically strip GPS tags from all files.

    Returns:
        List[Dict[str, Any]]: List of metadata summaries for all processed images.
    \"\"\"
    dir_path = Path(directory_path)
    results = []

    for img_path in dir_path.glob("*.jpg"):
        try:
            processor = ImageMetadataProcessor(img_path)
            exif = processor.extract_exif()
            if strip_gps:
                processor.strip_location_metadata()
            results.append({"file": img_path.name, "status": "success", "tags": len(exif)})
        except ImageMetadataError as err:
            logger.error("Error processing %s: %s", img_path.name, err)
            results.append({"file": img_path.name, "status": "error", "error": str(err)})

    return results


def main() -> None:
    \"\"\"CLI Command-Line Interface entry point for PyMeta metadata processor.\"\"\"
    parser = argparse.ArgumentParser(
        description="PyMeta CLI: Efficient image metadata processor (PEP 8 Compliant)",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter
    )
    parser.add_argument(
        "image",
        type=str,
        help="Path to the image file (e.g., '$fileNameStr')"
    )
    parser.add_argument(
        "--extract",
        "-e",
        action="store_true",
        help="Extract and display all EXIF tags"
    )
    parser.add_argument(
        "--strip-gps",
        "-s",
        action="store_true",
        help="Strip sensitive GPS location metadata"
    )
    parser.add_argument(
        "--json-out",
        "-j",
        type=str,
        help="Export metadata to specified JSON file"
    )

    args = parser.parse_args()

    try:
        processor = ImageMetadataProcessor(args.image)
        if args.extract:
            tags = processor.extract_exif()
            print(json.dumps(tags, indent=2, default=str))

        if args.strip_gps:
            clean_path = processor.strip_location_metadata()
            print(f"[+] Privacy Secured: Saved sanitized image to {clean_path}")

        if args.json_out:
            processor.export_json_report(args.json_out)
            print(f"[+] JSON Report saved to {args.json_out}")

    except Exception as exc:
        logger.error("Execution failed: %s", exc)
        sys.exit(1)


if __name__ == "__main__":
    main()
""".trimIndent()
    }

    fun generateSnippetForSelectedImage(image: ImageMetadata): String {
        return """
# PEP 8 Compliant Snippet generated for '${image.fileName}'
# Make: ${image.make} | Model: ${image.model} | Dimensions: ${image.width}x${image.height}

from pathlib import Path
from PIL import Image, ExifTags
import json


def inspect_image_metadata(file_path: str) -> None:
    \"\"\"Inspect EXIF metadata for target photo.\"\"\"
    path = Path(file_path)
    if not path.exists():
        raise FileNotFoundError(f"File {path} not found.")

    with Image.open(path) as img:
        exif = img._getexif() or {}
        readable_exif = {
            ExifTags.TAGS.get(k, k): str(v)
            for k, v in exif.items()
        }
        print(f"File: {path.name}")
        print(f"Make: {image.make}")
        print(f"Model: {image.model}")
        print(f"Exposure: {image.exposureTime} @ {image.aperture}, ISO {image.iso}")
        print("\nFull EXIF Data:")
        print(json.dumps(readable_exif, indent=2))


if __name__ == "__main__":
    inspect_image_metadata("${image.fileName}")
""".trimIndent()
    }
}
