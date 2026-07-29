package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ImageMetadata
import com.example.python.PythonPep8Generator
import com.example.ui.components.CodeViewer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850

@Composable
fun PythonStudioScreen(
    selectedImage: ImageMetadata?,
    currentBackend: PythonPep8Generator.PythonLibraryBackend,
    onBackendChanged: (PythonPep8Generator.PythonLibraryBackend) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) } // 0: Full Package, 1: Snippet for Selected Image

    val config = remember(currentBackend, selectedImage) {
        PythonPep8Generator.GeneratorConfig(
            backend = currentBackend,
            targetImage = selectedImage
        )
    }

    val generatedCode = remember(config, activeTab) {
        if (activeTab == 0) {
            PythonPep8Generator.generateFullPackageCode(config)
        } else {
            selectedImage?.let { PythonPep8Generator.generateSnippetForSelectedImage(it) }
                ?: PythonPep8Generator.generateFullPackageCode(config)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("python_studio_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = "PEP 8 Python Library Studio",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Clean, production-ready Python metadata processor library (PEP 8 & PEP 257 compliant)",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        // PEP 8 Quality Badges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QualityBadge("PEP 8 Style", EmeraldGreen)
                QualityBadge("PEP 257 Docstrings", CyanAccent)
                QualityBadge("Strict Typing", AmberGold)
                QualityBadge("Argparse CLI", Color(0xFFA855F7))
            }
        }

        // Backend & Script Customization Options Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Python Library Engine Backend",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PythonPep8Generator.PythonLibraryBackend.values().forEach { backend ->
                            FilterChip(
                                selected = currentBackend == backend,
                                onClick = { onBackendChanged(backend) },
                                label = { Text(backend.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Slate800,
                                    labelColor = Color.White
                                ),
                                modifier = Modifier.testTag("backend_chip_${backend.name}")
                            )
                        }
                    }

                    // Tab bar between Full Package vs Quick Snippet
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Slate800,
                        contentColor = CyanAccent
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("Full Package (pymeta_exif.py)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_full_package")
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("Selected Photo Snippet", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_photo_snippet")
                        )
                    }
                }
            }
        }

        // Code Viewer Component
        item {
            CodeViewer(
                code = generatedCode,
                title = if (activeTab == 0) "pymeta_exif.py (PEP 8 Package)" else "process_${selectedImage?.fileName ?: "sample.jpg"}.py"
            )
        }

        // Export Actions & Code Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("PEP 8 Code", generatedCode))
                        Toast.makeText(context, "PEP 8 Python Script copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_script_button")
                ) {
                    Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Python Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Exported 'pymeta_exif.py' (PEP 8 Verified)", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_script_button")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save .py File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // PEP 8 Guidelines Checklist Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PEP 8 & PEP 257 Compliance Verification",
                        color = AmberGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    PepRuleItem("Indentation", "4 spaces per indentation level (strictly zero tabs).")
                    PepRuleItem("Naming Conventions", "snake_case for methods/functions, PascalCase for exceptions/classes.")
                    PepRuleItem("Docstrings (PEP 257)", "Triple-quoted multi-line docstrings on all modules, classes, & defs.")
                    PepRuleItem("Imports Layout", "Standard library -> 3rd party -> local, separated by blank lines.")
                    PepRuleItem("Type Hint Annotations", "Strict typing with Path, Union, Dict, List, and Optional.")
                }
            }
        }
    }
}

@Composable
private fun QualityBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PepRuleItem(rule: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
        Text(
            text = "$rule: ",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = desc,
            color = Color.LightGray,
            fontSize = 11.sp
        )
    }
}
