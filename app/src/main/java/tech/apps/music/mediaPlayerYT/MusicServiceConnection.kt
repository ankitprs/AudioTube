package tech.apps.music.mediaPlayerYT

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.util.Event
import tech.apps.music.util.Resource
import tech.apps.music.util.getThumbnailFromId

class MusicServiceConnection(
    context: Context
) {

    suspend fun playFromVideoId(
        mediaId: String,
        watchedPosition: Float = 0F,
        title: String = "",
        channel: String = ""
    ) {
        while (YoutubeFloatingUI.youtubePlayer == null) {
            delay(500)
        }

        YoutubeFloatingUI.currentlyPlayingSong.postValue(
            YTAudioDataModel(
                mediaId,
                title,
                channel,
                getThumbnailFromId(mediaId)
            )
        )

        YoutubeFloatingUI.youtubePlayer?.loadVideo(mediaId, watchedPosition)
    }


    fun skipToNext() {
        val windowId =
            YoutubeFloatingUI.playlistSongs.indexOf(YoutubeFloatingUI.currentlyPlayingSong.value) + 1

        if (YoutubeFloatingUI.playlistSongs.size - 1 >= windowId) {
            YoutubeFloatingUI.currentlyPlayingSong.postValue(YoutubeFloatingUI.playlistSongs[windowId])
            YoutubeFloatingUI.youtubePlayer?.loadVideo(
                YoutubeFloatingUI.playlistSongs[windowId].mediaId,
                0F
            )
        }
    }

    fun skipToPrevious() {
        if (YoutubeFloatingUI.currentTime.value ?: 2F > 1F) {
            YoutubeFloatingUI.youtubePlayer?.seekTo(0F)
            return
        }

        val windowId =
            YoutubeFloatingUI.playlistSongs.indexOf(YoutubeFloatingUI.currentlyPlayingSong.value) - 1

        if (windowId >= 0) {
            YoutubeFloatingUI.currentlyPlayingSong.postValue(YoutubeFloatingUI.playlistSongs[windowId])
            YoutubeFloatingUI.youtubePlayer?.loadVideo(
                YoutubeFloatingUI.playlistSongs[windowId].mediaId,
                0F
            )
        }
    }

    fun gotoIndex(index: Int) {
        val windowId =
            YoutubeFloatingUI.playlistSongs.indexOf(YoutubeFloatingUI.currentlyPlayingSong.value)
        if (index == windowId)
            return
        if (YoutubeFloatingUI.playlistSongs.size - 1 >= index) {
            YoutubeFloatingUI.currentlyPlayingSong.postValue(YoutubeFloatingUI.playlistSongs[index])
            YoutubeFloatingUI.youtubePlayer?.loadVideo(
                YoutubeFloatingUI.playlistSongs[index].mediaId,
                0F
            )
        }
    }

    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError


    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    private val _curPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val curPlayingSong: LiveData<MediaMetadataCompat?> = _curPlayingSong


    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls


    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
    }

    lateinit var mediaController: MediaControllerCompat

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.postValue(Event(Resource.Success(true)))
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(
                Event(
                    Resource.Error(
                        "The connection was suspended", false
                    )
                )
            )
        }

        override fun onConnectionFailed() {
            _isConnected.postValue(
                Event(
                    Resource.Error(
                        "Couldn't Connect to media browser", false
                    )
                )
            )
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
//            _curPlayingSong.postValue(
//                if (metadata?.description?.mediaId == null) {
//                    NOTHING_PLAYING
//                } else {
//                    metadata
//                }
//            )
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when (event) {
//                NETWORK_ERROR -> _networkError.postValue(
//                    Event(
//                        Resource.Error(
//                            "Couldn't connect to the server. Please check your internet connection",
//                            null
//                        )
//                    )
//                )
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}

@Suppress("PropertyName")
val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

@Suppress("PropertyName")
val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()
