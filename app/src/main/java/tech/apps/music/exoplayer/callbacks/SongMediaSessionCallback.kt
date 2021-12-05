package tech.apps.music.exoplayer.callbacks

import android.support.v4.media.session.MediaSessionCompat
import android.util.Log

object SongMediaSessionCallback: MediaSessionCompat.Callback()  {
    override fun onSetPlaybackSpeed(speed: Float) {
        super.onSetPlaybackSpeed(speed)
        Log.i("SongMediaSessionCallback","speedcalled")
    }

    override fun onFastForward() {
        super.onFastForward()
    }
}