package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cli.CommandResult
import com.example.ui.components.TerminalWindow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850

@Composable
fun CliTerminalScreen(
    commandInput: String,
    onCommandInputChanged: (String) -> Unit,
    onRunCommand: (String) -> Unit,
    logs: List<CommandResult>,
    dbLogs: List<CommandResult>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("cli_terminal_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PyMeta CLI Shell",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Efficient Image Metadata Command-Line Engine",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                TextButton(
                    onClick = onClearLogs,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray),
                    modifier = Modifier.testTag("clear_terminal_logs_button")
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Clear History", fontSize = 12.sp)
                }
            }
        }

        // Live Interactive Terminal Component
        item {
            TerminalWindow(
                commandInput = commandInput,
                onCommandInputChanged = onCommandInputChanged,
                onRunCommand = onRunCommand,
                logs = logs
            )
        }

        // Command Reference Cheat Sheet Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate850),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Text(text = "CLI Command Reference (PEP 8 Compliant Syntax)", color = CyanAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    CliRefRow("pymeta inspect <file|--all>", "Extract EXIF tags, GPS location, and camera specs.")
                    CliRefRow("pymeta export-csv <file|--all>", "Export EXIF metadata into CSV format for Pandas/Excel.")
                    CliRefRow("pymeta export-json <file|--all>", "Export structured JSON for APIs and analytical pipelines.")
                    CliRefRow("pymeta strip <file|all> [--gps-only]", "Sanitize sensitive location or strip all metadata.")
                    CliRefRow("pymeta set <file> --artist ...", "Modify author and copyright metadata tags.")
                    CliRefRow("pymeta export-python <file>", "Generate PEP 8 compliant Python script.")
                    CliRefRow("pymeta diff <file1> <file2>", "Compare metadata differences between two photos.")
                    CliRefRow("pymeta hash <file>", "Calculate MD5 and Perceptual hashes.")
                }
            }
        }
    }
}

@Composable
private fun CliRefRow(cmd: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate800, RoundedCornerShape(6.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = cmd,
            color = EmeraldGreen,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = desc,
            color = Color.LightGray,
            fontSize = 11.sp,
            modifier = Modifier.weight(1.2f)
        )
    }
}
