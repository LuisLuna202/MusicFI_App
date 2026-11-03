package com.unam.musicfi.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.unam.musicfi.model.LocalSong

interface AudioRepository {
    fun getLocalSongs(): List<LocalSong>
}

class MediaStoreAudioRepository(context: Context) : AudioRepository {
    private val resolver = context.applicationContext.contentResolver

    override fun getLocalSongs(): List<LocalSong> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val songs = mutableListOf<LocalSong>()

        resolver.query(
            collection,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val albumId = cursor.getLong(albumIndex)
                val songUri = ContentUris.withAppendedId(collection, id)
                val artworkUri = if (albumId > 0L) {
                    // MediaStore exposes embedded album art through this content URI.
                    android.net.Uri.parse("content://media/external/audio/albumart/$albumId")
                } else null

                songs += LocalSong(
                    id = id,
                    title = cursor.getString(titleIndex)?.takeIf { it.isNotBlank() } ?: "Título desconocido",
                    artist = cursor.getString(artistIndex)?.takeIf { it.isNotBlank() && it != "<unknown>" }
                        ?: "Artista desconocido",
                    uri = songUri.toString(),
                    albumArtUri = artworkUri?.toString()
                )
            }
        }
        return songs
    }
}
