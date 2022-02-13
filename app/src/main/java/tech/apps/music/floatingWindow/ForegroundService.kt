package tech.apps.music.floatingWindow

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.database.Repository
import tech.apps.music.others.Constants
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundService : Service() {

    @Inject
    lateinit var repository: Repository
    private lateinit var youtubeFloatingUI: YoutubeFloatingUI

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        youtubeFloatingUI = YoutubeFloatingUI(this,this,repository)
        youtubeFloatingUI.open()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (intent.action != null && intent.action.equals(
                Constants.ACTION_STOP, ignoreCase = true)) {
            stopForeground(true)
            stopSelf()
            youtubeFloatingUI.close()
            android.os.Process.killProcess(android.os.Process.myPid())
        }else if(intent.action!=null && intent.action.equals(Constants.ACTION_PLAY_PAUSE_TOGGLE,true)){
            youtubeFloatingUI.togglePlayPause()
        }

        return START_STICKY_COMPATIBILITY
    }


    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        stopSelf()
    }
}
