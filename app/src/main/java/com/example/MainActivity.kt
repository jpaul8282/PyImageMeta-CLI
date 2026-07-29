package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.screens.BatchPrivacyScreen
import com.example.ui.screens.CliTerminalScreen
import com.example.ui.screens.InspectorScreen
import com.example.ui.screens.PythonStudioScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PyMetaTheme
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900

sealed class NavTab(val title: String, val icon: ImageVector, val tag: String) {
    object Inspector : NavTab("Inspector", Icons.Default.ImageSearch, "tab_inspector")
    object Cli : NavTab("CLI Shell", Icons.Default.Terminal, "tab_cli")
    object Python : NavTab("Python PEP 8", Icons.Default.Code, "tab_python")
    object Batch : NavTab("Batch Privacy", Icons.Default.Security, "tab_batch")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PyMetaTheme {
                var currentTab by remember { mutableStateOf<NavTab>(NavTab.Inspector) }

                val images by viewModel.images.collectAsState()
                val selectedImage by viewModel.selectedImage.collectAsState()
                val commandInput by viewModel.commandInput.collectAsState()
                val runtimeLogs by viewModel.runtimeLogs.collectAsState()
                val dbLogs by viewModel.dbCliLogs.collectAsState()
                val pythonBackend by viewModel.pythonBackend.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Slate900,
                    bottomBar = {
                        NavigationBar(
                            containerColor = Slate850,
                            contentColor = CyanAccent,
                            tonalElevation = 8.dp
                        ) {
                            val tabs = listOf(NavTab.Inspector, NavTab.Cli, NavTab.Python, NavTab.Batch)
                            tabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                                    label = { Text(text = tab.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Slate900,
                                        selectedTextColor = CyanAccent,
                                        indicatorColor = CyanAccent,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray
                                    ),
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val contentModifier = Modifier.padding(innerPadding)

                    when (currentTab) {
                        NavTab.Inspector -> InspectorScreen(
                            images = images,
                            selectedImage = selectedImage,
                            onSelectImage = { viewModel.selectImage(it) },
                            onImportUri = { viewModel.loadUserImageUri(it) },
                            onStripGps = { viewModel.stripLocationForSelected() },
                            onStripAll = { viewModel.stripAllForSelected() },
                            onUpdateIptc = { artist, copyright ->
                                viewModel.updateArtistAndCopyrightForSelected(artist, copyright)
                            },
                            modifier = contentModifier
                        )
                        NavTab.Cli -> CliTerminalScreen(
                            commandInput = commandInput,
                            onCommandInputChanged = { viewModel.setCommandInput(it) },
                            onRunCommand = { viewModel.runCommand(it) },
                            logs = runtimeLogs,
                            dbLogs = dbLogs,
                            onClearLogs = { viewModel.clearCliLogs() },
                            modifier = contentModifier
                        )
                        NavTab.Python -> PythonStudioScreen(
                            selectedImage = selectedImage,
                            currentBackend = pythonBackend,
                            onBackendChanged = { viewModel.setPythonBackend(it) },
                            modifier = contentModifier
                        )
                        NavTab.Batch -> BatchPrivacyScreen(
                            images = images,
                            onStripGpsForAll = { viewModel.stripGpsForAll() },
                            modifier = contentModifier
                        )
                    }
                }
            }
        }
    }
}
