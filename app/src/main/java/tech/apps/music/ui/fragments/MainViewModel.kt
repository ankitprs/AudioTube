package tech.apps.music.ui.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import tech.apps.music.database.Repository
import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.database.offline.WatchLaterSongModel
import tech.apps.music.floatingWindow.MusicServiceConnection
import tech.apps.music.floatingWindow.YoutubeFloatingUI
import tech.apps.music.model.YTAudioDataModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val musicServiceConnection: MusicServiceConnection,
    private val repository: Repository,
) : AndroidViewModel(application) {

    //    val getContinueWatchingList: LiveData<List<HistorySongModel>> = repository.getListOfContinue()
    val getRecentList: LiveData<List<HistorySongModel>> = repository.getAllSongsLiveData()
    val getWatchLaterList: LiveData<List<WatchLaterSongModel>> = repository.getListOfWatchLater()
    val getLast5RecentList: LiveData<List<HistorySongModel>> = repository.getLast5RecentList()

    //    val currentlyPlayingPlaylist: List<YTAudioDataModel>? =
//        musicServiceConnection.currentlyPlayingPlaylist()
    val bufferingTime: LiveData<Boolean> = YoutubeFloatingUI.bufferingTime


    fun skipToNextSong() {
        musicServiceConnection.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.skipToPrevious()
    }

//    fun setRepeatMode(isRepeat: Boolean) {
//        if (isRepeat) {
//            musicServiceConnection.setRepeatMode(REPEAT_MODE_ALL)
//        } else {
//            musicServiceConnection.setRepeatMode(REPEAT_MODE_NONE)
//        }
//    }

//    fun getRepeatState(): Boolean {
//        return musicServiceConnection.repeatMode()
//    }
//
//    fun fastForwardSong() {
//        musicServiceConnection.fastForward()
//    }
//
//    fun replayBackSong() {
//        musicServiceConnection.rewind()
//    }

    fun playPauseToggleSong(
        mediaId: String
    ) {
        if (YoutubeFloatingUI.youtubePlayer == null) {
            viewModelScope.launch {
                musicServiceConnection.playFromVideoId(mediaId)
            }
        } else {
            if (YoutubeFloatingUI.isPlaying.value == true) {
                YoutubeFloatingUI.youtubePlayer?.pause()
            } else {
                YoutubeFloatingUI.youtubePlayer?.play()
            }
        }
    }

    fun playOrToggleListOfSongs(
        mediaItem: List<YTAudioDataModel>,
        toggle: Boolean = false,
        position: Int = 0,
        watchedPosition: Long = 0L
    ) {
        viewModelScope.launch {
            musicServiceConnection.playFromVideoId(
                mediaItem[position].mediaId,
                (watchedPosition / 1000).toFloat(),
                mediaItem[position].title,
                mediaItem[position].author
            )
        }
    }

    fun addSongInRecent(ytLink: String, callback: (status: Boolean) -> Unit) {

        repository.getSongModelWithLink(ytLink) { audioModel ->

            Firebase.analytics.logEvent("Video_Played") {
                param("Video_ID", audioModel?.mediaId.toString())
                param("Video_Title", audioModel?.title.toString())
                param("Video_Channel_Name", audioModel?.author.toString())
            }
            if (audioModel != null) {
                playOrToggleListOfSongs(listOf(audioModel), true)
                callback(true)
            } else {
                callback(false)
            }
        }
    }

    fun songListenLater(watchLaterSongModel: WatchLaterSongModel) {
        viewModelScope.launch {
            repository.insertSongIntoWatchLater(watchLaterSongModel)
        }
    }

    fun removeSongListenLater(mediaId: String) {
        viewModelScope.launch {
            repository.deleteSongFromWatchLater(
                mediaId
            )
        }
    }

//    fun gotoIndex(index: Long) {
//        musicServiceConnection.skipToQueueItem(index)
//    }

    fun insertSearchQuery(queryText: String) {
        viewModelScope.launch {
            repository.insertSearchQuery(queryText)
        }
    }

    fun getListSearchHistory(callback: (list: List<String>) -> Unit) {
        val list: ArrayList<String> = ArrayList()
        viewModelScope.launch {
//            repository.getListSearchHistory().forEach {
//                list.add(it.queryText)
//            }
            callback(list)
        }
    }

    val getCurrentlyPlayingYTAudioModel: LiveData<YTAudioDataModel?> =
        YoutubeFloatingUI.currentlyPlayingSong

}