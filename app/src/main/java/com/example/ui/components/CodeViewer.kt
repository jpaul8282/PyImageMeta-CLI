package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.Slate800
import com.example.ui.theme.TerminalBlack

@Composable
fun CodeViewer(
    code: String,
    title: String = "pymeta_exif.py",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("code_viewer_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate800)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFEF4444), RoundedCornerShape(5.dp)))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFF59E0B), RoundedCornerShape(5.dp)))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF10B981), RoundedCornerShape(5.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("PEP 8 Python Code", code)
                        clipboard.setPrimaryClip(clip)
                        copied = true
                        Toast.makeText(context, "PEP 8 Python Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("copy_code_button")
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = if (copied) EmeraldGreen else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Code Content Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .padding(8.dp)
            ) {
                val lines = code.lines()

                Row(
                    modifier = Modifier
                        .verticalScroll(verticalScroll)
                        .horizontalScroll(horizontalScroll)
                ) {
                    // Line numbers column
                    Column(
                        modifier = Modifier
                            .background(Color(0xFF0F172A))
                            .padding(end = 12.dp, start = 4.dp)
                    ) {
                        lines.indices.forEach { idx ->
                            Text(
                                text = "%3d".format(idx + 1),
                                color = Color(0xFF475569),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Code text column
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        lines.forEach { line ->
                            Text(
                                text = highlightPythonLine(line),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun highlightPythonLine(line: String) = buildAnnotatedString {
    val trimmed = line.trimStart()
    if (trimmed.startsWith("#")) {
        withStyle(SpanStyle(color = Color(0xFF64748B))) { append(line) }
        return@buildAnnotatedString
    }
    if (trimmed.startsWith("\"\"\"") || trimmed.startsWith("'''")) {
        withStyle(SpanStyle(color = EmeraldGreen)) { append(line) }
        return@buildAnnotatedString
    }

    val keywords = setOf("def", "class", "import", "from", "return", "if", "else", "elif", "try", "except", "raise", "with", "as", "for", "in", "None", "True", "False")

    val parts = line.split(Regex("(?<=\\s|\\b|\\(|\\)|:|,)|(?=\\s|\\b|\\(|\\)|:|,)"))
    for (part in parts) {
        when {
            keywords.contains(part) -> {
                withStyle(SpanStyle(color = CyanAccent, fontWeight = FontWeight.Bold)) { append(part) }
            }
            part.startsWith("\"") || part.startsWith("'") || part.endsWith("\"") || part.endsWith("'") -> {
                withStyle(SpanStyle(color = EmeraldGreen)) { append(part) }
            }
            part.firstOrNull()?.isUpperCase() == true -> {
                withStyle(SpanStyle(color = AmberGold, fontWeight = FontWeight.SemiBold)) { append(part) }
            }
            part == "self" -> {
                withStyle(SpanStyle(color = PurpleAccent)) { append(part) }
            }
            else -> {
                withStyle(SpanStyle(color = Color(0xFFE2E8F0))) { append(part) }
            }
        }
    }
}
