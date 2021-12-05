package tech.apps.music.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import tech.apps.music.R
import tech.apps.music.others.Constants.NOTIFICATION_CHANNEL_ID
import tech.apps.music.others.Constants.NOTIFICATION_ID

class MusicNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback: () -> Unit
) {

    private val notificationManager: PlayerNotificationManager

    init {

        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setNotificationListener(notificationListener)
            .setSmallIconResourceId(R.drawable.ic_play_audio)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
            .setChannelNameResourceId(R.string.notification_channel_name)
            .setPlayActionIconResourceId(R.drawable.ic_round_play_circle_24)
            .setPauseActionIconResourceId(R.drawable.ic_round_pause_circle_24)
            .build()

        notificationManager.setMediaSessionToken(sessionToken)
        notificationManager.setUseFastForwardAction(false)
        notificationManager.setUseRewindAction(false)
    }


    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            newSongCallback()
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence {
            return mediaController.metadata.description.subtitle.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            var thumbnail: Bitmap? =null
            Glide.with(context)
                .asBitmap()
                .load(mediaController.metadata.description.iconUri)
                .centerCrop()
                .override(1600, 900)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        callback.onBitmap(resource)
                        thumbnail = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                })
            return thumbnail
        }
    }

}