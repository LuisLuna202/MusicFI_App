package com.unam.musicfi.model

data class LocalSong(
    val id: Long,
    val title: String,
    val artist: String,
    val uri: String,
    val albumArtUri: String? = null
)
