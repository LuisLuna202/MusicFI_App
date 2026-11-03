package com.unam.musicfi.domain

import com.unam.musicfi.model.LocalSong

/** Pure business rule: filter the library by title or artist. */
fun filterSongs(songs: List<LocalSong>, query: String): List<LocalSong> {
    val term = query.trim()
    if (term.isEmpty()) return songs
    return songs.filter {
        it.title.contains(term, ignoreCase = true) ||
            it.artist.contains(term, ignoreCase = true)
    }
}

/** The current demo login only checks that both fields are provided. */
fun hasLoginInput(username: String, password: String): Boolean =
    username.isNotBlank() && password.isNotBlank()
