package tech.apps.music.mediaPlayerYT

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.Constants
import tech.apps.music.database.Repository
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var repository: Repository

//    @Inject
//    lateinit var glide: RequestManager
    private lateinit var youtubeFloatingUI: YoutubeFloatingUI
    private lateinit var mediaSession : MediaSessionCompat

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return null
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
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

        mediaSession = MediaSessionCompat(this, Constants.SERVICE_TAG,null , activityIntent).apply {
            setSessionActivity(activityIntent)
        }

        youtubeFloatingUI = YoutubeFloatingUI(this, this, repository)
        youtubeFloatingUI.open()
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
            stopForeground(true)
            stopSelf()
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        stopSelf()
    }
}
