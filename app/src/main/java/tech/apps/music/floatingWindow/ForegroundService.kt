package tech.apps.music.floatingWindow

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.database.Repository
import tech.apps.music.others.Constants
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class ForegroundService : Service() {

    @Inject
    lateinit var repository: Repository

//    @Inject
//    lateinit var glide: RequestManager
    private lateinit var youtubeFloatingUI: YoutubeFloatingUI

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
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
