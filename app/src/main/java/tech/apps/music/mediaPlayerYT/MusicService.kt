package tech.apps.music.mediaPlayerYT

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.bumptech.glide.RequestManager
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.Constants
import tech.apps.music.Constants.MEDIA_ROOT_ID
import tech.apps.music.database.Repository
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var glide: RequestManager
    private lateinit var youtubeFloatingUI: YoutubeFloatingUI
    private lateinit var mediaSession: MediaSessionCompat

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(null)
    }

    override fun onCreate() {
        super.onCreate()

        val activityIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        val stateBuilder: PlaybackStateCompat.Builder? = PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY)
            .setActions(PlaybackStateCompat.ACTION_PAUSE)
            .setActions(PlaybackStateCompat.ACTION_STOP)
            .setActions(PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
            .setActions(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            .setActions(PlaybackStateCompat.ACTION_PAUSE)
            .setState(
                PlaybackStateCompat.STATE_STOPPED,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1f
            )

        mediaSession = MediaSessionCompat(this, Constants.SERVICE_TAG, null, activityIntent).apply {
            setSessionActivity(activityIntent)
            isActive = true
            setPlaybackState(stateBuilder?.build())
        }

        val mediaMetadata = MediaMetadataCompat.Builder()
        mediaMetadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "Fitoor Song | Shamshera")
        mediaMetadata.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, null)
        mediaMetadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, "T - series")
        mediaSession.setMetadata(mediaMetadata.build())

        youtubeFloatingUI = YoutubeFloatingUI(this, repository, glide, mediaSession)
        youtubeFloatingUI.open()

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                youtubeFloatingUI.youtubePlayer?.play()
            }

            override fun onPause() {
                super.onPause()
                youtubeFloatingUI.youtubePlayer?.pause()
            }

            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                youtubeFloatingUI.youtubePlayer?.seekTo(pos.toFloat())
            }

            override fun onSetPlaybackSpeed(speed: Float) {
                super.onSetPlaybackSpeed(speed)
                youtubeFloatingUI.youtubePlayer?.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_1)
            }

            override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                super.onPlayFromMediaId(mediaId, extras)
                extras?.getFloat(Constants.PASSING_SONG_LAST_WATCHED_POS)
                youtubeFloatingUI.youtubePlayer?.loadVideo(
                    mediaId ?: "",
                    extras?.getFloat(Constants.PASSING_SONG_LAST_WATCHED_POS) ?: 0F
                )
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action != null && intent.action.equals(
                Constants.ACTION_PLAY_PAUSE_TOGGLE, true
            )
        ) {
            youtubeFloatingUI.togglePlayPause()
        } else if (intent?.action != null && intent.action.equals(
                Constants.ACTION_STOP, ignoreCase = true
            )
        ) {
            youtubeFloatingUI.close()
            stopService(null)
            stopSelf()
            exitProcess(0)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(null)
        stopSelf()
    }
}
