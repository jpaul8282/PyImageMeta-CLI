package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cli.CliEngine
import com.example.cli.CommandResult
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseRed
import com.example.ui.theme.Slate800
import com.example.ui.theme.TerminalBlack

@Composable
fun TerminalWindow(
    commandInput: String,
    onCommandInputChanged: (String) -> Unit,
    onRunCommand: (String) -> Unit,
    logs: List<CommandResult>,
    modifier: Modifier = Modifier
) {
    val quickCommands = listOf(
        "pymeta inspect --all",
        "pymeta strip all --gps-only",
        "pymeta export-python",
        "pymeta diff DSC09241.jpg CR3_0014.jpg",
        "pymeta hash IMG_20260728_142010.jpg",
        "pymeta help"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("terminal_window_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            // Header
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PyMeta CLI Terminal Shell",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "PEP 8 Engine v2.4",
                    color = EmeraldGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Quick Command Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickCommands.forEach { cmd ->
                    Surface(
                        onClick = { onRunCommand(cmd) },
                        shape = RoundedCornerShape(14.dp),
                        color = Slate800,
                        modifier = Modifier.testTag("quick_cmd_${cmd.replace(" ", "_")}")
                    ) {
                        Text(
                            text = cmd,
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Terminal Logs Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(8.dp)
            ) {
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Ready. Type a command or select a quick option above.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = false
                    ) {
                        items(logs.reversed()) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0B132B), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$ ${item.command}",
                                        color = CyanAccent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "${item.timestamp} (${item.executionTimeMs}ms)",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatOutputText(item.output),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Slate800, thickness = 1.dp)

            // Prompt Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ ",
                    color = EmeraldGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                )

                OutlinedTextField(
                    value = commandInput,
                    onValueChange = onCommandInputChanged,
                    placeholder = {
                        Text(
                            text = "pymeta inspect --all ...",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cli_prompt_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { onRunCommand(commandInput) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = Slate800,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                IconButton(
                    onClick = { onRunCommand(commandInput) },
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .testTag("run_command_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run Command",
                        tint = EmeraldGreen
                    )
                }
            }
        }
    }
}

private fun formatOutputText(text: String) = buildAnnotatedString {
    text.lines().forEach { line ->
        when {
            line.startsWith("[+]") -> {
                withStyle(SpanStyle(color = EmeraldGreen, fontWeight = FontWeight.Bold)) { append(line) }
            }
            line.startsWith("Error:") || line.contains("EXPOSED") -> {
                withStyle(SpanStyle(color = RoseRed, fontWeight = FontWeight.Bold)) { append(line) }
            }
            line.startsWith("===") -> {
                withStyle(SpanStyle(color = AmberGold, fontWeight = FontWeight.Bold)) { append(line) }
            }
            else -> {
                withStyle(SpanStyle(color = Color(0xFFCBD5E1))) { append(line) }
            }
        }
        append("\n")
    }
}
