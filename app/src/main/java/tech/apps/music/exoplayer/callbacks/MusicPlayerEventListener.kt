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
        when (playbackState) {
            Player.STATE_BUFFERING,
            Player.STATE_READY -> {
                musicService.musicNotificationManager.showNotification(musicService.exoPlayer)
                if (playbackState == Player.STATE_READY) {

                    // When playing/paused save the current media item in persistent
                    // storage so that playback can be resumed between device reboots.
                    // Search for "media resumption" for more information.
                    musicService.saveRecentSongDataToStorage()

                    if (!playWhenReady) {
                        // If playback is paused we remove the foreground state which allows the
                        // notification to be dismissed. An alternative would be to provide a
                        // "close" button in the notification which stops playback and clears
                        // the notification.
                        musicService.stopForeground(false)
                        musicService.isForegroundService = false
                    }
                }
            }
            else -> {
                musicService.musicNotificationManager.hideNotification()
            }
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