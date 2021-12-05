package tech.apps.music.exoplayer.callbacks

import android.widget.Toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import tech.apps.music.database.network.YTVideoExtractor
import tech.apps.music.exoplayer.MusicService

class MusicPlayerEventListener(
    private val musicService: MusicService
) : Player.Listener {

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if (playbackState == Player.STATE_READY && !playWhenReady) {
            musicService.stopForeground(false)
        }
        MusicService.bufferingTime.value = playbackState == Player.STATE_BUFFERING
    }

//    override fun onPlayerError(error: ExoPlaybackException) {
//        super.onPlayerError(error)
//        Toast.makeText(musicService, "An unknown error occurred", Toast.LENGTH_LONG).show()
//    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService, "Please... Retry Again", Toast.LENGTH_LONG).show()
        YTVideoExtractor.clearCache()

    }
}