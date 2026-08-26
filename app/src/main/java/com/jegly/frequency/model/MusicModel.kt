package com.jegly.frequency.model

import java.io.Serializable

data class MusicModel(
    val id: Long,
    val songName: String,
    val artist: String,
    val album: String,
    var songPath: String,
    var albumArtUrl: String?,
    val duration: Long,
    val dateAdded: Long
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
