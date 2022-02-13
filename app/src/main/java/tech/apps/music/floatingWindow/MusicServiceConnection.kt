package tech.apps.music.floatingWindow

import android.content.Context
import kotlinx.coroutines.delay
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.util.VideoData

class MusicServiceConnection constructor(
    private val context: Context,
) {

    suspend fun playFromVideoId(
        mediaId: String,
        watchedPosition: Float = 0F,
        title: String = "",
        channel: String = ""
    ) {
        while (YoutubeFloatingUI.youtubePlayer==null){
            delay(500)
        }

        YoutubeFloatingUI.currentlyPlayingSong.postValue(
            YTAudioDataModel(
                mediaId,
                title,
                channel,
                VideoData.getThumbnailFromId(mediaId)
            )
        )

        YoutubeFloatingUI.youtubePlayer?.loadVideo(mediaId, watchedPosition)
    }

    fun skipToNext() {
    }

    fun skipToPrevious() {
        YoutubeFloatingUI.youtubePlayer?.seekTo(0F)
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