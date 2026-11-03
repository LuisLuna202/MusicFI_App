package com.unam.musicfi.data

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

interface AudioPlayer {
    fun play(uri: Uri, onCompleted: () -> Unit = {})
    fun toggle()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
}

class AndroidAudioPlayer(context: Context) : AudioPlayer {
    private val appContext = context.applicationContext
    private var player: MediaPlayer? = null

    override fun play(uri: Uri, onCompleted: () -> Unit) {
        release()
        val newPlayer = MediaPlayer()
        player = newPlayer

        newPlayer.setOnCompletionListener {
            release()
            onCompleted()
        }
        newPlayer.setOnErrorListener { _, _, _ ->
            release()
            true
        }

        try {
            newPlayer.setDataSource(appContext, uri)
            newPlayer.prepare()
            newPlayer.start()
        } catch (error: Exception) {
            release()
            throw error
        }
    }

    override fun toggle() {
        player?.let { current ->
            runCatching {
                if (current.isPlaying) current.pause() else current.start()
            }
        }
    }

    override fun stop() = release()

    override fun release() {
        val current = player
        player = null
        if (current != null) {
            runCatching { current.reset() }
            runCatching { current.release() }
        }
    }

    override fun isPlaying(): Boolean = runCatching { player?.isPlaying == true }.getOrDefault(false)
}
