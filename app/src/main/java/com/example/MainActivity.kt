package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GeminiViewModel
import com.example.ui.screens.KeysScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.GeminiBluePrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusNeutral
import com.example.ui.theme.StatusSuccess

enum class AppScreen(val title: String) {
    STUDIO("Gemini Studio"),
    KEYS("Gemini API Keys"),
    SETTINGS("Settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: GeminiViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val state by viewModel.uiState.collectAsState()
                var currentScreen by remember { mutableStateOf(AppScreen.STUDIO) }
                val snackbarHostState = remember { SnackbarHostState() }

                // Handle back button when on sub-screens
                if (currentScreen != AppScreen.STUDIO) {
                    BackHandler {
                        currentScreen = AppScreen.STUDIO
                    }
                }

                // Show snackbars from ViewModel
                LaunchedEffect(state.snackbarMessage) {
                    state.snackbarMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.dismissSnackbar()
                    }
                }

                val activeKeysCount = listOf(state.savedKey1, state.savedKey2, state.savedKey3)
                    .count { it.isNotBlank() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = currentScreen.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            },
                            actions = {
                                // Status pill in TopBar
                                Surface(
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                    color = if (activeKeysCount > 0) StatusSuccess.copy(alpha = 0.15f)
                                    else StatusNeutral.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Text(
                                        text = if (activeKeysCount > 0) "$activeKeysCount/3 Keys Active" else "Not configured",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (activeKeysCount > 0) StatusSuccess else StatusNeutral,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("main_bottom_nav"),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.STUDIO,
                                onClick = { currentScreen = AppScreen.STUDIO },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.STUDIO) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                        contentDescription = "Studio",
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = { Text("Studio", fontSize = 12.sp) },
                                modifier = Modifier.testTag("nav_tab_studio")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.KEYS,
                                onClick = { currentScreen = AppScreen.KEYS },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.KEYS) Icons.Filled.Key else Icons.Outlined.Key,
                                        contentDescription = "Gemini API Keys",
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = { Text("API Keys", fontSize = 12.sp) },
                                modifier = Modifier.testTag("nav_tab_keys")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.SETTINGS,
                                onClick = { currentScreen = AppScreen.SETTINGS },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                        contentDescription = "Settings",
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = { Text("Settings", fontSize = 12.sp) },
                                modifier = Modifier.testTag("nav_tab_settings")
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.STUDIO -> {
                                StudioScreen(
                                    state = state,
                                    onPromptChange = viewModel::onPromptInputChanged,
                                    onSendPrompt = viewModel::sendPrompt,
                                    onNavigateToKeys = { currentScreen = AppScreen.KEYS },
                                    onClearChat = viewModel::clearChat
                                )
                            }
                            AppScreen.KEYS -> {
                                KeysScreen(
                                    state = state,
                                    onDraftChange = viewModel::onDraftKeyChanged,
                                    onToggleVisibility = viewModel::toggleKeyVisibility,
                                    onSaveKey = viewModel::saveKey,
                                    onRemoveKey = viewModel::removeKey,
                                    onTestKey = viewModel::testApiKey,
                                    onTestAllKeys = viewModel::testAllKeys,
                                    onClearAllKeys = viewModel::clearAllKeys
                                )
                            }
                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    state = state,
                                    onNavigateToKeysSection = { currentScreen = AppScreen.KEYS },
                                    onDraftChange = viewModel::onDraftKeyChanged,
                                    onToggleVisibility = viewModel::toggleKeyVisibility,
                                    onSaveKey = viewModel::saveKey,
                                    onRemoveKey = viewModel::removeKey,
                                    onTestKey = viewModel::testApiKey,
                                    onSelectModel = viewModel::selectModel,
                                    onUpdateSystemPrompt = viewModel::updateSystemPrompt,
                                    onUpdateTemperature = viewModel::updateTemperature
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
