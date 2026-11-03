package com.unam.musicfi

import android.net.Uri
import com.unam.musicfi.model.LocalSong\nimport com.unam.musicfi.domain.filterSongs\nimport com.unam.musicfi.domain.hasLoginInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicFiLogicTest {
    private val songs = listOf(
        LocalSong(1, "Luna", "Latin Mafia", "content://audio/1"),
        LocalSong(2, "Azul", "Zoé", "content://audio/2"),
        LocalSong(3, "Brillas", "León Larregui", "content://audio/3")
    )

    @Test
    fun emptySearchReturnsEntireLibrary() {
        assertEquals(songs, filterSongs(songs, "  "))
    }

    @Test
    fun searchFindsTitleIgnoringCase() {
        assertEquals(listOf(songs[0]), filterSongs(songs, "lUnA"))
    }

    @Test
    fun searchFindsArtistAndTrimsWhitespace() {
        assertEquals(listOf(songs[0]), filterSongs(songs, "  latin mafia "))
    }

    @Test
    fun searchWithoutMatchesReturnsEmptyList() {
        assertTrue(filterSongs(songs, "Taylor Swift").isEmpty())
    }

    @Test
    fun loginRequiresBothFields() {
        assertTrue(hasLoginInput("luis", "password"))
        assertFalse(hasLoginInput("", "password"))
        assertFalse(hasLoginInput("luis", "  "))
        assertFalse(hasLoginInput("", ""))
    }
}
