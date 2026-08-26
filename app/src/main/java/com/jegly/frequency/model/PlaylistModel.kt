package com.jegly.frequency.model

import java.io.Serializable

data class PlaylistModel(
    val id: Long,
    var name: String,
    val trackIds: MutableList<Long> = mutableListOf(),
    val dateCreated: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    var isSystemPlaylist: Boolean = false,
    var category: String? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
