package net.omergoktas.medyaisleme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import net.omergoktas.medyaisleme.data.model.ToolCategory
import net.omergoktas.medyaisleme.ui.accessibility.accessibleTouchTarget
import net.omergoktas.medyaisleme.ui.components.NavDestination
import net.omergoktas.medyaisleme.ui.components.NavigationDrawerContent
import net.omergoktas.medyaisleme.ui.screens.DocumentCreatorScreen
import net.omergoktas.medyaisleme.ui.screens.DocumentOcrScreen
import net.omergoktas.medyaisleme.ui.screens.FeedbackScreen
import net.omergoktas.medyaisleme.ui.screens.HomeScreen
import net.omergoktas.medyaisleme.ui.screens.MediaToolsScreen
import net.omergoktas.medyaisleme.ui.screens.PrivacyPolicyScreen
import net.omergoktas.medyaisleme.ui.screens.SettingsScreen
import net.omergoktas.medyaisleme.ui.theme.MedyaIslemeTheme
import net.omergoktas.medyaisleme.ui.viewmodel.ProcessingViewModel
import net.omergoktas.medyaisleme.ui.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    private val processingViewModel: ProcessingViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState()

            MedyaIslemeTheme(themeMode = themeMode) {
                // Request Notification Permission for Android 13+
                RequestNotificationPermission()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var currentDestination by remember { mutableStateOf(NavDestination.HOME) }
                var feedbackTool by remember { mutableStateOf("") }
                var feedbackErrorType by remember { mutableStateOf("") }
                var feedbackSourceFormat by remember { mutableStateOf("") }
                var feedbackTargetFormat by remember { mutableStateOf("") }

                BackHandler(enabled = drawerState.isOpen || currentDestination != NavDestination.HOME) {
                    if (drawerState.isOpen) {
                        scope.launch { drawerState.close() }
                    } else {
                        currentDestination = NavDestination.HOME
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            NavigationDrawerContent(
                                currentDestination = currentDestination,
                                onNavigateToDestination = { dest ->
                                    if (dest == NavDestination.FEEDBACK) {
                                        feedbackTool = ""
                                        feedbackErrorType = ""
                                        feedbackSourceFormat = ""
                                        feedbackTargetFormat = ""
                                    }
                                    currentDestination = dest
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = currentDestination.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                },
                                navigationIcon = {
                                    if (currentDestination == NavDestination.HOME) {
                                        val openDrawerDesc = stringResource(R.string.a11y_open_drawer)
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                                }
                                            },
                                            modifier = Modifier
                                                .accessibleTouchTarget()
                                                .semantics {
                                                    contentDescription = openDrawerDesc
                                                }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            onClick = {
                                                currentDestination = NavDestination.HOME
                                            },
                                            modifier = Modifier
                                                .accessibleTouchTarget()
                                                .semantics {
                                                    contentDescription = "Yukarı git"
                                                }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    titleContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            when (currentDestination) {
                                NavDestination.HOME -> {
                                    HomeScreen(
                                        onNavigateToDestination = { currentDestination = it }
                                    )
                                }

                                NavDestination.AUDIO_TOOLS -> {
                                    MediaToolsScreen(
                                        viewModel = processingViewModel,
                                        toolCategory = ToolCategory.AUDIO,
                                        onNavigateToFeedback = { tool, errType, srcFmt, tgtFmt ->
                                            feedbackTool = tool
                                            feedbackErrorType = errType
                                            feedbackSourceFormat = srcFmt
                                            feedbackTargetFormat = tgtFmt
                                            currentDestination = NavDestination.FEEDBACK
                                        },
                                        onNavigateBack = { currentDestination = NavDestination.HOME }
                                    )
                                }

                                NavDestination.VIDEO_TOOLS -> {
                                    MediaToolsScreen(
                                        viewModel = processingViewModel,
                                        toolCategory = ToolCategory.VIDEO,
                                        onNavigateToFeedback = { tool, errType, srcFmt, tgtFmt ->
                                            feedbackTool = tool
                                            feedbackErrorType = errType
                                            feedbackSourceFormat = srcFmt
                                            feedbackTargetFormat = tgtFmt
                                            currentDestination = NavDestination.FEEDBACK
                                        },
                                        onNavigateBack = { currentDestination = NavDestination.HOME }
                                    )
                                }

                                NavDestination.DOCUMENT_TOOLS -> {
                                    DocumentOcrScreen(
                                        viewModel = processingViewModel,
                                        onNavigateToFeedback = { tool, errType, srcFmt, tgtFmt ->
                                            feedbackTool = tool
                                            feedbackErrorType = errType
                                            feedbackSourceFormat = srcFmt
                                            feedbackTargetFormat = tgtFmt
                                            currentDestination = NavDestination.FEEDBACK
                                        },
                                        onNavigateBack = { currentDestination = NavDestination.HOME },
                                        onNavigateToDocCreator = { currentDestination = NavDestination.DOCUMENT_CREATOR }
                                    )
                                }

                                NavDestination.DOCUMENT_CREATOR -> {
                                    DocumentCreatorScreen(viewModel = processingViewModel)
                                }

                                NavDestination.PRIVACY_POLICY -> {
                                    PrivacyPolicyScreen(
                                        onBackToHome = { currentDestination = NavDestination.HOME }
                                    )
                                }

                                NavDestination.SETTINGS -> {
                                    SettingsScreen(themeViewModel = themeViewModel)
                                }

                                NavDestination.FEEDBACK -> {
                                    FeedbackScreen(
                                        preselectedTool = feedbackTool,
                                        preselectedErrorType = feedbackErrorType,
                                        preselectedSourceFormat = feedbackSourceFormat,
                                        preselectedTargetFormat = feedbackTargetFormat
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
