package com.turtlepaw.live_media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat

private const val TAG = "LiveMedia"

class LiveMedia(
    private val context: Context,
    private val serviceComponent: ComponentName
) {
    private var mediaBrowser: MediaBrowserCompat? = null
    private var mediaController: MediaControllerCompat? = null

    var title: String? = null
        private set
    var artist: String? = null
        private set
    var albumArt: Bitmap? = null
        private set
    var isPlaying: Boolean = false
        private set

    fun connect() {
        mediaBrowser = MediaBrowserCompat(
            context,
            serviceComponent,
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    mediaController = MediaControllerCompat(context, mediaBrowser!!.sessionToken)
                    mediaController?.registerCallback(mediaControllerCallback)
                    updatePlaybackState()
                }

                override fun onConnectionSuspended() {
                    // Handle suspension
                }

                override fun onConnectionFailed() {
                    // Handle connection failure
                }
            },
            null
        ).apply { connect() }
    }

    fun disconnect() {
        mediaController?.unregisterCallback(mediaControllerCallback)
        mediaBrowser?.disconnect()
    }

    fun play() {
        mediaController?.transportControls?.play()
    }

    fun pause() {
        mediaController?.transportControls?.pause()
    }

    fun next() {
        mediaController?.transportControls?.skipToNext()
    }

    fun previous() {
        mediaController?.transportControls?.skipToPrevious()
    }

    private fun updatePlaybackState() {
        mediaController?.playbackState?.let { playbackState ->
            isPlaying = playbackState.state == PlaybackStateCompat.STATE_PLAYING
        }
        mediaController?.metadata?.let { metadata ->
            title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            albumArt = metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
        }
    }

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let {
                isPlaying = it.state == PlaybackStateCompat.STATE_PLAYING
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let {
                title = it.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                artist = it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
                albumArt = it.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
            }
        }
    }
}

