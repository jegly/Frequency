package com.jegly.frequency.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jegly.frequency.loaders.ArtistFetcher
import com.jegly.frequency.model.ArtistModel
import com.jegly.frequency.utils.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(onArtistClick: (ArtistModel) -> Unit) {
    val context = LocalContext.current
    val artistFetcher = remember { ArtistFetcher(context.contentResolver) }
    var artists by remember { mutableStateOf<List<ArtistModel>>(emptyList()) }
    val spanCount = remember { AppSettings.getPortraitGridSpanCount(context) }

    LaunchedEffect(Unit) {
        artists = artistFetcher.fetchArtists()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Artists") }) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(spanCount),
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(artists) { artist ->
                ArtistGridItem(artist) { onArtistClick(artist) }
            }
        }
    }
}

@Composable
fun ArtistGridItem(artist: ArtistModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = artist.artistName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = "${artist.numOfTracks} tracks",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
