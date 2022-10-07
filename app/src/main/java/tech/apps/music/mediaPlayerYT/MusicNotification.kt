package tech.apps.music.mediaPlayerYT

import android.app.PendingIntent
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import tech.apps.music.Constants
import tech.apps.music.R
import tech.apps.music.ui.HomeActivity

class MusicNotification(
    private val musicService: MusicService,
    private val mediaSession: MediaSessionCompat
) {

    private var playPausePendingIntent: PendingIntent
    private var stopPendingIntent: PendingIntent
    private lateinit var notification: NotificationCompat.Builder

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

            playPausePendingIntent = PendingIntent
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

            val pendingIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, HomeActivity::class.java).apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                },
                PendingIntent.FLAG_IMMUTABLE
            )

            val controller = mediaSession.controller
            val mediaMetadata = controller.metadata
            val description = mediaMetadata.description

            notification =
                NotificationCompat.Builder(musicService, Constants.NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(description.title)
                    .setContentText(description.subtitle)
                    .setSubText(description.description)
                    .setLargeIcon(description.iconBitmap)

//                    .setContentIntent(controller.sessionActivity)
                    .setContentIntent(pendingIntent)

                    .setDeleteIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            musicService,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )

                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_play_audio)

                    .addAction(
                        NotificationCompat.Action.Builder(
                            R.drawable.ic_round_play_arrow_24,
                            "pause",
                            playPausePendingIntent
                        ).build()

                    )
//                    .addAction(R.drawable.ic_round_pause_24, "play", playPausePendingIntent)

                    .addAction(
                        R.drawable.ic_round_clear_24,
                        "Stop",
                        stopPendingIntent
                    )

                    .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0)
                            .setMediaSession(mediaSession.sessionToken)
                            .setShowCancelButton(true)
                            .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                    musicService,
                                    PlaybackStateCompat.ACTION_STOP
                                )
                            )

                    )

            startForeground(Constants.NOTIFICATION_ID, notification.build())
        }

    }

    fun toggleNotification(isPlaying: Boolean) {

        notification.clearActions()
        if (isPlaying) {
            notification.addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_round_pause_24,
                    "play",
                    playPausePendingIntent
                ).build()
            )
        } else {
            notification.addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_round_play_arrow_24,
                    "pause",
                    playPausePendingIntent
                ).build()
            )
        }
        notification.addAction(R.drawable.ic_round_clear_24, "Stop", stopPendingIntent)

        musicService.startForeground(Constants.NOTIFICATION_ID, notification.build())
    }
}