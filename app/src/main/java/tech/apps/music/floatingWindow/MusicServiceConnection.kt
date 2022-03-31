package tech.apps.music.floatingWindow

import android.content.Context
import kotlinx.coroutines.delay
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.util.getThumbnailFromId

class MusicServiceConnection constructor(
    private val context: Context,
) {

    suspend fun playFromVideoId(
        mediaId: String,
        watchedPosition: Float = 0F,
        title: String = "",
        channel: String = ""
    ) {
        while (YoutubeFloatingUI.youtubePlayer == null) {
            delay(500)
        }

        YoutubeFloatingUI.currentlyPlayingSong.postValue(
            YTAudioDataModel(
                mediaId,
                title,
                channel,
                getThumbnailFromId(mediaId)
            )
        )

        YoutubeFloatingUI.youtubePlayer?.loadVideo(mediaId, watchedPosition)
    }


    fun skipToNext() {
        val windowId =
            YoutubeFloatingUI.playlistSongs.indexOf(YoutubeFloatingUI.currentlyPlayingSong.value) + 1

        if (YoutubeFloatingUI.playlistSongs.size - 1 >= windowId) {
            YoutubeFloatingUI.currentlyPlayingSong.postValue(YoutubeFloatingUI.playlistSongs[windowId])
            YoutubeFloatingUI.youtubePlayer?.loadVideo(
                YoutubeFloatingUI.playlistSongs[windowId].mediaId,
                0F
            )
        }
    }

    fun skipToPrevious() {
        if (YoutubeFloatingUI.currentTime.value ?: 2F > 1F) {
            YoutubeFloatingUI.youtubePlayer?.seekTo(0F)
            return
        }

        val windowId =
            YoutubeFloatingUI.playlistSongs.indexOf(YoutubeFloatingUI.currentlyPlayingSong.value) - 1

        if (windowId >= 0) {
            YoutubeFloatingUI.currentlyPlayingSong.postValue(YoutubeFloatingUI.playlistSongs[windowId])
            YoutubeFloatingUI.youtubePlayer?.loadVideo(
                YoutubeFloatingUI.playlistSongs[windowId].mediaId,
                0F
            )
        }
    }

    fun gotoIndex(index: Int) {
        val windowId =
            YoutubeFloatingUI.playlistSongs.indexOf(YoutubeFloatingUI.currentlyPlayingSong.value)
        if (index == windowId)
            return
        if (YoutubeFloatingUI.playlistSongs.size - 1 >= index) {
            YoutubeFloatingUI.currentlyPlayingSong.postValue(YoutubeFloatingUI.playlistSongs[index])
            YoutubeFloatingUI.youtubePlayer?.loadVideo(
                YoutubeFloatingUI.playlistSongs[index].mediaId,
                0F
            )
        }
    }

//    fun fastForwardSong() {
//    }
//
//    fun setRepeatMode(repeatModeAll: Int) {
//
//    }
//
//    fun fastForward() {
//
//    }
//
//    fun rewind() {
//    }
//
//    fun unsubscribe() {
//    }
//
//    fun repeatMode(): Boolean {
//        return false
//    }
//
//    fun skipToQueueItem(index: Long) {
//    }
//
//    fun currentlyPlayingPlaylist(): List<YTAudioDataModel>? {
//        return null
//    }
}