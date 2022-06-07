package tech.apps.music.floatingWindow

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
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
import tech.apps.music.others.Constants
import tech.apps.music.ui.HomeActivity
import androidx.media.app.NotificationCompat as MediaNotificationCompat


@SuppressLint("InflateParams")
class YoutubeFloatingUI(
    private val context: Context,
    private val foregroundService: ForegroundService,
    private val repository: Repository,
//    private val glide: RequestManager
) {

    private val youTubePlayerView: YouTubePlayerView
    private val mView: View
    private var mParams: WindowManager.LayoutParams? = null
    private val mWindowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var notification: NotificationCompat.Builder =
        NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
    private val tracker = YouTubePlayerTracker()
    private val manager =
        context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

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
        curSongDuration.removeObserver {}
        bufferingTime.removeObserver {}
        isPlaying.removeObserver {}
        currentTime.removeObserver {}

        // remove the view from the window
        mWindowManager.removeView(mView)
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

    private fun initializingYoutubePlayer() {

        val iFramePlayerOptions: IFramePlayerOptions = IFramePlayerOptions.Builder()
            .controls(0)
            .rel(0)
            .ivLoadPolicy(0)
            .ccLoadPolicy(0)
            .build()

        startMyOwnForeground()

        youTubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                Toast.makeText(
                    context,
                    "Oops... Youtube doesn't allow us to play this video",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onReady(youTubePlayer: YouTubePlayer) {

                youtubePlayer = youTubePlayer
                youTubePlayerView.enableBackgroundPlayback(true)
                youtubePlayer?.setVolume(100)
                youtubePlayer?.addListener(tracker)
            }

            override fun onPlaybackQualityChange(
                youTubePlayer: YouTubePlayer,
                playbackQuality: PlayerConstants.PlaybackQuality
            ) {
                super.onPlaybackQualityChange(youTubePlayer, playbackQuality)
                Log.i("YoutubeUIPLAYER", playbackQuality.toString())
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                currentTime.postValue(second)
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
                savingSongInHistory()
            }
        }, true, iFramePlayerOptions)
    }

    private fun startMyOwnForeground() {
        foregroundService.apply {
            val channelName = "Background Service"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    channelName,
                    NotificationManager.IMPORTANCE_MIN
                )
                manager.createNotificationChannel(channel)
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, HomeActivity::class.java).apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                },
                PendingIntent.FLAG_IMMUTABLE
            )
            notification.setOngoing(true)
                .setSmallIcon(R.drawable.ic_play_audio)
                .setStyle(
                    MediaNotificationCompat.MediaStyle()
                        .setShowCancelButton(true)
                        .setShowActionsInCompactView(0)
                )
                .setContentTitle("")
                .setContentText("")
                .setColorized(true).setColor(
                    Color.parseColor("#181818")
                )
                .setContentIntent(pendingIntent)
            funAddingActionsInNotification()

            startForeground(Constants.NOTIFICATION_ID, notification.build())
            manager.notify(Constants.NOTIFICATION_ID, notification.build())

            currentlyPlayingSong.observeForever {
                notification.setContentTitle(it?.title)
                notification.setContentText(it?.author)
                manager.notify(Constants.NOTIFICATION_ID, notification.build())
//
//                if (it != null) {
//                    playlistSongs.remove(playlistSongs.find { list ->
//                        list.mediaId == it.mediaId
//                    })
//                    playlistSongs.addAll(0, listOf(it))
//                }
            }
        }
    }

    fun togglePlayPause() {
        if (isPlaying.value == true) {
            youtubePlayer?.pause()
        } else {
            youtubePlayer?.play()
        }
    }

    private fun funAddingActionsInNotification() {
        foregroundService.apply {
            val stopIntent: PendingIntent = PendingIntent.getService(
                this,
                1,
                Intent(this, ForegroundService::class.java).apply {
                    action = Constants.ACTION_STOP
                },
                PendingIntent.FLAG_IMMUTABLE
            )

            val playPendingIntent: PendingIntent = PendingIntent
                .getService(
                    this, 2,
                    Intent(this, ForegroundService::class.java).apply {
                        action = Constants.ACTION_PLAY_PAUSE_TOGGLE
                    },
                    PendingIntent.FLAG_IMMUTABLE
                )

            notification.addAction(R.drawable.ic_round_pause_24, null, playPendingIntent)
                .addAction(R.drawable.ic_round_clear_24, null, stopIntent)

            isPlaying.observeForever {
                notification.clearActions()
                if (it) {
                    notification.addAction(R.drawable.ic_round_pause_24, null, playPendingIntent)
                        .addAction(R.drawable.ic_round_clear_24, null, stopIntent)
                } else {
                    notification.addAction(
                        R.drawable.ic_round_play_arrow_24,
                        null,
                        playPendingIntent
                    )
                        .addAction(R.drawable.ic_round_clear_24, null, stopIntent)
                }
                manager.notify(Constants.NOTIFICATION_ID, notification.build())
            }

        }
    }

    private fun savingSongInHistory() {
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

    private fun playNextSong() {
        val windowId = playlistSongs.indexOf(currentlyPlayingSong.value) + 1

        if (playlistSongs.size - 1 > windowId) {
            currentlyPlayingSong.postValue(playlistSongs[windowId])
            youtubePlayer?.loadVideo(playlistSongs[windowId].mediaId, 0F)
        } else {
            if (repeatMode) {
                youtubePlayer?.play()
            } else {
                isPlaying.postValue(false)
                togglePlayPause()
            }
        }
    }
}