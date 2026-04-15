package com.tunes.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.tunes.player.loaders.AlbumFetcher
import com.tunes.player.model.AlbumModel
import com.tunes.player.utils.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(onAlbumClick: (AlbumModel) -> Unit) {
    val context = LocalContext.current
    val albumFetcher = remember { AlbumFetcher(context.contentResolver) }
    var albums by remember { mutableStateOf<List<AlbumModel>>(emptyList()) }
    val spanCount = remember { AppSettings.getPortraitGridSpanCount(context) }

    LaunchedEffect(Unit) {
        albums = albumFetcher.fetchAlbums()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Albums") }) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(spanCount),
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(albums) { album ->
                AlbumGridItem(album) { onAlbumClick(album) }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AlbumGridItem(album: AlbumModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable { onClick() }
    ) {
        Column {
            GlideImage(
                model = album.albumArt ?: "",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            Text(
                text = album.albumName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "${album.songsCount} songs",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        }
    }
}
