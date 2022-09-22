package tech.apps.music.mediaPlayerYT

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import okhttp3.internal.notify
import tech.apps.music.Constants
import tech.apps.music.R
import tech.apps.music.ui.HomeActivity

class MusicNotification(
    private val musicService: MusicService,
    private val mediaSession: MediaSessionCompat
) {

    private var playPendingIntent: PendingIntent
    private var pausePendingIntent: PendingIntent? = null
    private var nextPendingIntent: PendingIntent? = null
    private var stopPendingIntent: PendingIntent
    private lateinit var notification : NotificationCompat.Builder

    private val manager =
        musicService.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager


    fun updateNotification(title: String, imageBitmap: Bitmap?) {
        notification.setContentTitle(title)
        notification.setLargeIcon(imageBitmap)
        notification.build().notify()

        YoutubeFloatingUI.isPlaying.observeForever {
            notification.clearActions()
            if (it) {
                notification.addAction(R.drawable.ic_round_pause_24, null, playPendingIntent)
                    .addAction(R.drawable.ic_round_clear_24, null, stopPendingIntent)
            } else {
                notification.addAction(
                    R.drawable.ic_round_play_arrow_24,
                    null,
                    playPendingIntent
                )
                    .addAction(R.drawable.ic_round_clear_24, null, stopPendingIntent)
            }
            manager.notify(Constants.NOTIFICATION_ID, notification.build())
        }

        YoutubeFloatingUI.currentlyPlayingSong.observeForever {
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

    init {
        musicService.apply {
            stopPendingIntent = PendingIntent.getService(
                this,
                1,
                Intent(this, MusicService::class.java).apply {
                    action = Constants.ACTION_STOP
                },
                PendingIntent.FLAG_IMMUTABLE
            )

            playPendingIntent = PendingIntent
                .getService(
                    this, 2,
                    Intent(this, MusicService::class.java).apply {
                        action = Constants.ACTION_PLAY_PAUSE_TOGGLE
                    },
                    PendingIntent.FLAG_IMMUTABLE
                )
        }
    }

    fun startMyOwnForeground() {
        musicService.apply {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    Constants.CHANNEL_NAME,
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

            notification = NotificationCompat.Builder(musicService, Constants.NOTIFICATION_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_play_audio)
                // Add media control buttons that invoke intents in your media service
                .addAction(R.drawable.ic_round_play_arrow_24, "play", playPendingIntent) // #0
                .addAction(R.drawable.ic_round_pause_24, "Pause", pausePendingIntent) // #1
                .addAction(
                    R.drawable.cast_ic_expanded_controller_skip_next,
                    "Next",
                    nextPendingIntent
                ) // #2
                .addAction(R.drawable.ic_round_clear_24, "Stop", stopPendingIntent)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1)
                        .setMediaSession(mediaSession.sessionToken)
                )
                .setContentTitle("Wonderful music")
                .setLargeIcon(null)
                .setContentIntent(pendingIntent)


//            notification.setOngoing(true)
//                .setColorized(true).setColor(
//                    Color.parseColor("#181818")
//                )

            startForeground(Constants.NOTIFICATION_ID, notification.build())
            manager.notify(Constants.NOTIFICATION_ID, notification.build())
        }
    }

}