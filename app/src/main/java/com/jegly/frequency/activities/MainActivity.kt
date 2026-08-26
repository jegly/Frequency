package com.jegly.frequency.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.jegly.frequency.BuildConfig
import com.jegly.frequency.R
import com.jegly.frequency.ui.theme.TunesTheme
import com.jegly.frequency.utils.TamperDetection
import com.jegly.frequency.viewmodel.MainViewModel
import com.jegly.frequency.utils.AppSettings
import com.jegly.frequency.ui.screens.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG && !TamperDetection.isValid(this)) {
            finishAndRemoveTask()
            return
        }
        setTheme(R.style.Theme_Tunes)
        supportActionBar?.hide()
        enableEdgeToEdge()
        splashScreen.setOnExitAnimationListener { it.remove() }

        setContent {
            val context = LocalContext.current
            viewModel = viewModel()

            val themeMode by viewModel.themeMode.collectAsState()
            val catppuccinAccent by viewModel.catppuccinAccent.collectAsState()
            val catppuccinFlavor by viewModel.catppuccinFlavor.collectAsState()
            val draculaAccent by viewModel.draculaAccent.collectAsState()
            val ptyxisPalette by viewModel.ptyxisPalette.collectAsState()
            val monochromeAccents by viewModel.monochromeAccents.collectAsState()
            val appFont by viewModel.appFont.collectAsState()

            val isBiometricEnabled = remember { AppSettings.isBiometricEnabled(context) }
            var isAuthenticated by remember { mutableStateOf(!isBiometricEnabled) }

            var hasPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO)
                            == PackageManager.PERMISSION_GRANTED
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
                    launcher.launch(arrayOf(Manifest.permission.READ_MEDIA_AUDIO))
                }
                // Handle intent that launched the activity (e.g. opening a music file)
                handleIncomingIntent(intent)
            }

            TunesTheme(
                themeMode = themeMode,
                catppuccinFlavor = catppuccinFlavor,
                catppuccinAccent = catppuccinAccent,
                draculaAccent = draculaAccent,
                ptyxisPalette = ptyxisPalette,
                monochromeAccents = monochromeAccents,
                appFont = appFont
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when {
                        !hasPermission -> PermissionDeniedScreen { launcher.launch(arrayOf(Manifest.permission.READ_MEDIA_AUDIO)) }
                        !isAuthenticated -> BiometricAuthScreen { isAuthenticated = true }
                        else -> MainContainer(viewModel)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (::viewModel.isInitialized) {
            handleIncomingIntent(intent)
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                if (::viewModel.isInitialized) {
                    viewModel.playFromUri(uri)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.refreshPlaybackState()
        }
    }

    @Composable
    fun MainContainer(viewModel: MainViewModel) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val currentTrack by viewModel.currentTrack.collectAsState()
        val miniPlayerHidden by viewModel.miniPlayerHidden.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
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
                composable("settings") { SettingsScreen(viewModel) }
                composable("frequency") { FrequencyScreen() }
                composable("now_playing") { NowPlayingScreen(viewModel) }
                composable("playlist/{playlistId}/{playlistName}") { backStackEntry ->
                    val playlistName = backStackEntry.arguments?.getString("playlistName") ?: "Unknown"
                    PlaylistScreen(viewModel, playlistName) { navController.popBackStack() }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                AnimatedVisibility(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    visible = currentTrack != null && currentDestination?.route != "now_playing" && currentDestination?.route != "frequency" && !miniPlayerHidden,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    MiniPlayer(viewModel) { navController.navigate("now_playing") }
                }
                FloatingBottomNavigationBar(
                    currentDestination = currentDestination?.route,
                    onNavigationSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp), clip = false),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationItem(Icons.Default.MusicNote, "Sound",
                    currentDestination == "now_playing", Modifier.weight(1f)) { onNavigationSelected("now_playing") }
                NavigationItem(Icons.Default.LibraryMusic, "Library",
                    currentDestination == "library", Modifier.weight(1f)) { onNavigationSelected("library") }
                NavigationItem(Icons.AutoMirrored.Filled.PlaylistPlay, "Playlists",
                    currentDestination == "playlists", Modifier.weight(1f)) { onNavigationSelected("playlists") }
                NavigationItem(Icons.Default.Waves, "Frequency",
                    currentDestination == "frequency", Modifier.weight(1f)) { onNavigationSelected("frequency") }
                NavigationItem(Icons.Default.Settings, "Settings",
                    currentDestination == "settings", Modifier.weight(1f)) { onNavigationSelected("settings") }
            }
        }
    }

    @Composable
    fun NavigationItem(
        icon: ImageVector,
        label: String,
        selected: Boolean,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        val tint = if (selected) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant
        Column(
            modifier = modifier
                .clickable { onClick() }
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            AutoResizeText(
                text = label,
                baseStyle = MaterialTheme.typography.labelSmall,
                color = tint,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    /**
     * A single-line [Text] that shrinks its font size until the label fits the
     * available width. Keeps bottom-nav labels ("Settings", "Playlists", …) from
     * being truncated at larger system font / display-size scales, adapting
     * automatically instead of clipping.
     */
    @Composable
    fun AutoResizeText(
        text: String,
        baseStyle: TextStyle,
        color: Color,
        modifier: Modifier = Modifier
    ) {
        var fontSizeSp by remember(text) { mutableFloatStateOf(baseStyle.fontSize.value) }
        var readyToDraw by remember(text) { mutableStateOf(false) }
        Text(
            text = text,
            color = color,
            style = baseStyle.copy(fontSize = fontSizeSp.sp),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            modifier = modifier.drawWithContent { if (readyToDraw) drawContent() },
            onTextLayout = { result ->
                if (result.didOverflowWidth && fontSizeSp > 7f) {
                    fontSizeSp *= 0.92f
                } else {
                    readyToDraw = true
                }
            }
        )
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
                    Text(
                        text = track?.songName ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track?.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { viewModel.skipToPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, null)
                }
                IconButton(onClick = { viewModel.togglePlayPause() }) {
                    Icon(
                        if (playbackState == android.media.session.PlaybackState.STATE_PLAYING)
                            Icons.Default.Pause else Icons.Default.PlayArrow,
                        null
                    )
                }
                IconButton(onClick = { viewModel.skipToNext() }) {
                    Icon(Icons.Default.SkipNext, null)
                }
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
            Icon(Icons.Default.Storage, contentDescription = null,
                modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Access Required", style = MaterialTheme.typography.headlineMedium)
            Text("Frequency only needs access to your music files.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
                    if (::viewModel.isInitialized) viewModel.refreshPlaybackState()
                    onAuthenticated()
                }
            })
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Frequency Lock")
            .setSubtitle("Authenticate to open")
            .setNegativeButtonText("Cancel")
            .build()
        LaunchedEffect(Unit) { biometricPrompt.authenticate(promptInfo) }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { biometricPrompt.authenticate(promptInfo) }) { Text("Unlock Frequency") }
        }
    }
}
