package com.tunes.player.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.tunes.player.ui.theme.TunesTheme
import com.tunes.player.viewmodel.MainViewModel
import com.tunes.player.utils.AppSettings
import com.tunes.player.ui.screens.*
import com.tunes.player.model.PlaylistModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            viewModel = viewModel()
            val isBiometricEnabled = remember { AppSettings.isBiometricEnabled(context) }
            var isAuthenticated by remember { mutableStateOf(!isBiometricEnabled) }
            
            // Fixed: No READ_PHONE_STATE. Only Audio files permission.
            val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            var hasPermission by remember {
                mutableStateOf(
                    permissionsToRequest.all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }
                )
            }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions.values.all { it }) {
                    hasPermission = true
                    viewModel.loadLibrary()
                }
            }

            LaunchedEffect(Unit) {
                if (!hasPermission) {
                    launcher.launch(permissionsToRequest)
                }
            }

            TunesTheme(dynamicColor = true) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (!hasPermission) {
                        PermissionDeniedScreen { launcher.launch(permissionsToRequest) }
                    } else if (!isAuthenticated) {
                        BiometricAuthScreen { isAuthenticated = true }
                    } else {
                        MainContainer(viewModel)
                    }
                }
            }
        }
    }

    @Composable
    fun MainContainer(viewModel: MainViewModel) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val currentTrack by viewModel.currentTrack.collectAsState()

        // Main content area with padding for bottom navigation
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "library",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("library") { LibraryScreen(viewModel) }
                        composable("playlists") { 
                            PlaylistsScreen(viewModel) { playlist ->
                                navController.navigate("playlist/${playlist.id}/${playlist.name}")
                            }
                        }
                        composable("settings") { SettingsScreen() }
                        composable("now_playing") { NowPlayingScreen(viewModel) }
                        composable("playlist/{playlistId}/{playlistName}") { backStackEntry ->
                            val playlistName = backStackEntry.arguments?.getString("playlistName") ?: "Unknown"
                            PlaylistScreen(viewModel, playlistName) {
                                navController.popBackStack()
                            }
                        }
                    }
                }

                // Mini Player visible globally above bottom bar
                AnimatedVisibility(
                    modifier = Modifier.padding(bottom = 16.dp),
                    visible = currentTrack != null && currentDestination?.route != "now_playing",
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    MiniPlayer(viewModel) {
                        navController.navigate("now_playing")
                    }
                }

                // Floating Material 3 Bottom Navigation Bar
                FloatingBottomNavigationBar(
                    currentDestination = currentDestination?.route,
                    onNavigationSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

    @Composable
    fun FloatingBottomNavigationBar(
        currentDestination: String?,
        onNavigationSelected: (String) -> Unit
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 8.dp,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Library
                NavigationItem(
                    icon = Icons.Default.LibraryMusic,
                    label = "Library",
                    selected = currentDestination == "library",
                    onClick = { onNavigationSelected("library") }
                )

                // Playlists
                NavigationItem(
                    icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                    label = "Playlists",
                    selected = currentDestination == "playlists",
                    onClick = { onNavigationSelected("playlists") }
                )

                // Settings
                NavigationItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    selected = currentDestination == "settings",
                    onClick = { onNavigationSelected("settings") }
                )
            }
        }
    }

    @Composable
    fun NavigationItem(
        icon: ImageVector,
        label: String,
        selected: Boolean,
        onClick: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun MiniPlayer(viewModel: MainViewModel, onClick: () -> Unit) {
        val track by viewModel.currentTrack.collectAsState()
        val playbackState by viewModel.playbackState.collectAsState()

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable { onClick() },
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlideImage(
                    model = track?.albumArtUrl ?: "",
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(text = track?.songName ?: "Unknown", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = track?.artist ?: "Unknown Artist", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = { viewModel.skipToPrevious() }) { Icon(Icons.Default.SkipPrevious, null) }
                IconButton(onClick = { viewModel.togglePlayPause() }) {
                    Icon(if (playbackState == android.media.session.PlaybackState.STATE_PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                }
                IconButton(onClick = { viewModel.skipToNext() }) { Icon(Icons.Default.SkipNext, null) }
            }
        }
    }

    @Composable
    fun PermissionDeniedScreen(onRetry: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Access Required", style = MaterialTheme.typography.headlineMedium)
            Text("Tunes only needs access to your music files.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("Grant Access") }
        }
    }

    @Composable
    fun BiometricAuthScreen(onAuthenticated: () -> Unit) {
        val context = LocalContext.current
        val executor = remember { ContextCompat.getMainExecutor(context) }
        val biometricPrompt = remember {
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Refresh playback state after biometric authentication to prevent desynchronization
                    if (::viewModel.isInitialized) {
                        viewModel.refreshPlaybackState()
                    }
                    onAuthenticated()
                }
            })
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Tunes Lock").setSubtitle("Authenticate to open").setNegativeButtonText("Cancel").build()
        LaunchedEffect(Unit) { biometricPrompt.authenticate(promptInfo) }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { biometricPrompt.authenticate(promptInfo) }) { Text("Unlock Tunes") }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Force refresh playback state when app resumes to prevent desynchronization
        if (::viewModel.isInitialized) {
            viewModel.refreshPlaybackState()
        }
    }
}
