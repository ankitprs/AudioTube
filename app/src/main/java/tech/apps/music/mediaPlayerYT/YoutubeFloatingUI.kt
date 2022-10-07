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

    private val tracker = YouTubePlayerTracker()

    companion object {
        var youtubePlayer: YouTubePlayer? = null
        val currentlyPlayingSong: MutableLiveData<YTAudioDataModel?> = MutableLiveData(null)
        var playlistSongs: MutableList<YTAudioDataModel> = arrayListOf()

        var curSongDuration: MutableLiveData<Float> = MutableLiveData(0F)
            private set
        val bufferingTime: MutableLiveData<Boolean> = MutableLiveData(false)
        val isPlaying: MutableLiveData<Boolean> = MutableLiveData(false)
        val currentTime: MutableLiveData<Float> = MutableLiveData(0F)
        var repeatMode: Boolean = false

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
        initializingYoutubePlayer()
    }

    fun open() {
        try {
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

                val mediaMetadata = MediaMetadataCompat.Builder()
                mediaMetadata.putString(
                    MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,
                    currentlyPlayingSong.value?.title
                )
                mediaMetadata.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, null)
                mediaMetadata.putString(
                    MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,
                    currentlyPlayingSong.value?.author
                )
                mediaSession.setMetadata(mediaMetadata.build())
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                currentTime.postValue(second)
                mediaSession.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PAUSED,
                            second.toLong(),
                            1F
                        )
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
                        mediaSession.controller.transportControls.play()
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
        glide.asBitmap().centerCrop().load(currentlyPlayingSong.value?.thumbnailUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                    mediaSession.setMetadata(
                        MediaMetadataCompat.Builder()
                            .putString(
                                MediaMetadata.METADATA_KEY_TITLE,
                                currentlyPlayingSong.value?.title
                            )
                            .putString(
                                MediaMetadata.METADATA_KEY_ARTIST,
                                currentlyPlayingSong.value?.author
                            )
                            .putBitmap(
                                MediaMetadata.METADATA_KEY_ART,
                                resource
                            )
                            .putLong(MediaMetadata.METADATA_KEY_DURATION, songDuration) // 4
                            .build()
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    fun togglePlayPause() {
        if (isPlaying.value == true) {
            mediaSession.controller.transportControls.pause()
        } else {
            mediaSession.controller.transportControls.play()
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