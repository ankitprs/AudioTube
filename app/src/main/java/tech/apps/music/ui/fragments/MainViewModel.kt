package tech.apps.music.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import tech.apps.music.database.Repository
import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.database.offline.SearchHistory
import tech.apps.music.database.offline.WatchLaterSongModel
import tech.apps.music.floatingWindow.MusicServiceConnection
import tech.apps.music.floatingWindow.YoutubeFloatingUI
import tech.apps.music.model.YTAudioDataModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    private val repository: Repository,
) : ViewModel() {

    fun getRecentList(callback: (list: List<HistorySongModel>) -> Unit) {
        viewModelScope.launch {
            callback(repository.getAllSongsLiveData())
        }
    }

    fun getWatchLaterList(callback: (list: List<WatchLaterSongModel>) -> Unit) {
        viewModelScope.launch {
            callback(repository.getListOfWatchLater())
        }
    }

    fun getLast5RecentList(callback: (list: List<HistorySongModel>) -> Unit) {
        viewModelScope.launch {
            callback(repository.getLast5RecentList())
        }
    }

    val bufferingTime: LiveData<Boolean> = YoutubeFloatingUI.bufferingTime


    fun skipToNextSong() {
        musicServiceConnection.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.skipToPrevious()
    }

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
            YoutubeFloatingUI.playlistSongs = mediaItem.toMutableList()
            musicServiceConnection.playFromVideoId(
                mediaItem[position].mediaId,
                (watchedPosition / 1000).toFloat(),
                mediaItem[position].title,
                mediaItem[position].author
            )
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

    fun gotoIndex(index: Int) {
        musicServiceConnection.gotoIndex(index)
    }

    fun insertSearchQuery(queryText: String) {
        viewModelScope.launch {
            repository.insertSearchQuery(queryText)
        }
    }

    fun getListSearchHistory(callback: (list: List<SearchHistory>) -> Unit) {
        viewModelScope.launch {
            callback(repository.getListSearchHistory())
        }
    }

    val getCurrentlyPlayingYTAudioModel: LiveData<YTAudioDataModel?> =
        YoutubeFloatingUI.currentlyPlayingSong

}