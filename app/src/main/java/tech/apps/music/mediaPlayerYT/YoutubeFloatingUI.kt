package tech.apps.music.mediaPlayerYT

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadata
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.apps.music.R
import tech.apps.music.database.Repository
import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.model.YTAudioDataModel


@SuppressLint("InflateParams")
class YoutubeFloatingUI(
    private val foregroundService: MusicService,
    private val repository: Repository,
    private val glide: RequestManager,
    private val mediaSession: MediaSessionCompat
) {

    private val youTubePlayerView: YouTubePlayerView
    private val mView: View
    private var mParams: WindowManager.LayoutParams? = null
    private val mWindowManager: WindowManager =
        foregroundService.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater: LayoutInflater =
        foregroundService.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var musicNotification: MusicNotification
    var youtubePlayer: YouTubePlayer? = null
    private val tracker = YouTubePlayerTracker()

    companion object {
        val currentlyPlayingSong: MutableLiveData<YTAudioDataModel?> = MutableLiveData(null)
        var playlistSongs: MutableList<YTAudioDataModel> = arrayListOf()

        var curSongDuration: MutableLiveData<Float> = MutableLiveData(0F)
            private set
        val bufferingTime: MutableLiveData<Boolean> = MutableLiveData(false)
        val isPlaying: MutableLiveData<Boolean> = MutableLiveData(false)
        val currentTime: MutableLiveData<Float> = MutableLiveData(0F)
        var repeatMode: Boolean = false

        var sleepTimer: MutableLiveData<Long?> = MutableLiveData(null)
        var isYoutubeActiveForPlay: Boolean = false
            private set
    }

    init {
        mView = layoutInflater.inflate(R.layout.youtube_floating_ui, null, false)

        mParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        )

        youTubePlayerView = mView.findViewById(R.id.youtube_player_view)
        musicNotification = MusicNotification(foregroundService, mediaSession)
        musicNotification.startMyOwnForeground()
        initializingYoutubePlayer()
    }

    fun open() {
        try {
            // check if the view is already
            // inflated or present in the window
            if (mView.windowToken == null) {
                if (mView.parent == null) {
                    mWindowManager.addView(mView, mParams)
                }
            }
        } catch (e: Exception) {
            Log.d("Error1", e.toString())
        }

    }

    fun close() {
        youTubePlayerView.release()

        currentlyPlayingSong.removeObserver {}
        playlistSongs.removeAll { true }
        isPlaying.removeObserver {}
        currentTime.removeObserver {}

        // remove the view from the window
//        mWindowManager.removeView(mView)
    }

    private fun initializingYoutubePlayer() {

        val iFramePlayerOptions: IFramePlayerOptions = IFramePlayerOptions.Builder()
            .controls(0)
            .rel(0)
            .ivLoadPolicy(0)
            .ccLoadPolicy(0)
            .build()

        youTubePlayerView.initialize(object : AbstractYouTubePlayerListener() {

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                Toast.makeText(
                    foregroundService,
                    "Oops... Youtube doesn't allow us to play this video",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onReady(youTubePlayer: YouTubePlayer) {

                youtubePlayer = youTubePlayer
                youTubePlayerView.enableBackgroundPlayback(true)
                youtubePlayer?.setVolume(100)
                youtubePlayer?.addListener(tracker)

                isYoutubeActiveForPlay = true
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                super.onVideoId(youTubePlayer, videoId)

                glide.asBitmap().load(currentlyPlayingSong.value?.thumbnailUrl).into(
                    object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            val mediaMetadata = MediaMetadataCompat.Builder()
                            mediaMetadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, currentlyPlayingSong.value?.title)
                            mediaMetadata.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, resource)
                            mediaMetadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, "T - series")
                            mediaSession.setMetadata(mediaMetadata.build())
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            return
                        }
                    }
                )
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                currentTime.postValue(second)
                mediaSession.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            second.toLong(),
                            1F
                        )
                        // isSeekable.
                        // Adding the SEEK_TO action indicates that seeking is supported
                        // and makes the seekbar position marker draggable. If this is not
                        // supplied seek will be disabled but progress will still be shown.
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                )
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                super.onStateChange(youTubePlayer, state)
                when (state) {
                    PlayerConstants.PlayerState.BUFFERING -> {
                        bufferingTime.postValue(true)
                        isPlaying.postValue(true)
                    }
                    PlayerConstants.PlayerState.PLAYING -> {
                        bufferingTime.postValue(false)
                        isPlaying.postValue(true)
                    }
                    PlayerConstants.PlayerState.ENDED -> {
                        playNextSong()
                    }
                    else -> {
                        isPlaying.postValue(false)
                        bufferingTime.postValue(false)
                    }
                }
                savingSongInHistory()
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                super.onVideoDuration(youTubePlayer, duration)
                curSongDuration.postValue(duration)
                currentlyPlayingSong.value?.duration = (duration * 1000).toLong()
                updateMetadata(duration.toLong())
                savingSongInHistory()
            }
        }, true, iFramePlayerOptions)
    }

    private fun updateMetadata(songDuration: Long) {
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                // Title.
                .putString(MediaMetadata.METADATA_KEY_TITLE, currentlyPlayingSong.value?.title)

                // Artist.
                // Could also be the channel name or TV series.
                .putString(MediaMetadata.METADATA_KEY_ARTIST, currentlyPlayingSong.value?.author)

                // Album art.
                // Could also be a screenshot or hero image for video content
                // The URI scheme needs to be "content", "file", or "android.resource".
                .putString(
                    MediaMetadata.METADATA_KEY_ALBUM_ART_URI,
                    currentlyPlayingSong.value?.thumbnailUrl
                )
                .putLong(MediaMetadata.METADATA_KEY_DURATION, songDuration) // 4
                .build()
        )
    }

    fun togglePlayPause() {
        if (isPlaying.value == true) {
            youtubePlayer?.pause()
        } else {
            youtubePlayer?.play()
        }
    }

    fun savingSongInHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            currentlyPlayingSong.value?.let {
                val historySongModel =
                    HistorySongModel(
                        it.mediaId,
                        it.title,
                        it.author,
                        it.duration,
                        System.currentTimeMillis(),
                        (tracker.currentSecond * 1000).toLong()
                    )
                repository.insertSongInHistory(
                    historySongModel
                )
                Firebase.analytics.logEvent("Video_Played") {
                    param("Video_ID", it.mediaId)
                    param("Video_Title", it.title)
                    param("Video_Channel_Name", it.author)
                }
            }
        }
    }

    fun playNextSong() {
        val windowId = playlistSongs.indexOf(currentlyPlayingSong.value) + 1
        if (repeatMode) {
            youtubePlayer?.play()
        } else if (playlistSongs.size - 1 > windowId) {
            currentlyPlayingSong.postValue(playlistSongs[windowId])
            youtubePlayer?.loadVideo(playlistSongs[windowId].mediaId, 0F)
        } else {
            isPlaying.postValue(false)
            togglePlayPause()
        }
    }
}