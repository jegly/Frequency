package com.tunes.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tunes.player.R
import com.tunes.player.utils.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var biometricEnabled by remember { mutableStateOf(AppSettings.isBiometricEnabled(context)) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            item {
                ListItem(
                    headlineContent = { Text("Biometric Lock") },
                    supportingContent = { Text("Require authentication to open the app") },
                    trailingContent = {
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { it ->
                                biometricEnabled = it
                                AppSettings.setBiometricEnabled(context, it)
                            }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("About") },
                    supportingContent = { 
                        Column {
                            Text("Dev: jegly")
                            Text("www.jegly.xyz")
                        }
                    }
                )
            }
        }
    }
}
