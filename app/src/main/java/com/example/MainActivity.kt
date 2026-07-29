package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthState
import com.example.ui.MainViewModel
import com.example.ui.screens.BatchPrivacyScreen
import com.example.ui.screens.CliTerminalScreen
import com.example.ui.screens.InspectorScreen
import com.example.ui.screens.PythonStudioScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PyMetaTheme
import com.example.ui.theme.Slate800
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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PyMetaTheme {
                val context = LocalContext.current
                var currentTab by remember { mutableStateOf<NavTab>(NavTab.Inspector) }

                val images by viewModel.images.collectAsState()
                val selectedImage by viewModel.selectedImage.collectAsState()
                val commandInput by viewModel.commandInput.collectAsState()
                val runtimeLogs by viewModel.runtimeLogs.collectAsState()
                val dbLogs by viewModel.dbCliLogs.collectAsState()
                val pythonBackend by viewModel.pythonBackend.collectAsState()
                val authState by viewModel.authState.collectAsState()
                val isExtractingMetadata by viewModel.isExtractingMetadata.collectAsState()
                val extractionStatus by viewModel.extractionStatus.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Slate900,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "PyMeta Engine",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            actions = {
                                AuthHeaderButton(
                                    authState = authState,
                                    onSignIn = { viewModel.signInWithGoogle(context) },
                                    onSignOut = { viewModel.signOut() }
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Slate850,
                                titleContentColor = Color.White
                            )
                        )
                    },
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
                            isExtractingMetadata = isExtractingMetadata,
                            extractionStatus = extractionStatus,
                            onSelectImage = { viewModel.selectImage(it) },
                            onImportUri = { viewModel.loadUserImageUri(it) },
                            onReextractMetadata = { viewModel.reextractSelectedMetadata() },
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

@Composable
private fun AuthHeaderButton(
    authState: AuthState,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    when (authState) {
        is AuthState.SignedIn -> {
            Box {
                Button(
                    onClick = { showMenu = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.padding(end = 8.dp).testTag("user_profile_chip")
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = authState.user.displayName.take(1).uppercase(),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = authState.user.displayName.substringBefore(" "),
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Slate850)
                ) {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(authState.user.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(authState.user.email, color = Color.Gray, fontSize = 11.sp)
                            }
                        },
                        onClick = { },
                        enabled = false
                    )
                    HorizontalDivider(color = Slate800)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                Text("Sign Out", color = Color.White, fontSize = 12.sp)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onSignOut()
                        },
                        modifier = Modifier.testTag("sign_out_menu_item")
                    )
                }
            }
        }
        is AuthState.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 12.dp),
                color = CyanAccent,
                strokeWidth = 2.dp
            )
        }
        else -> {
            Button(
                onClick = onSignIn,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.padding(end = 8.dp).testTag("google_sign_in_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sign in with Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

