package tech.apps.music.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import tech.apps.music.database.CacheRepository
import tech.apps.music.database.Repository
import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.database.offline.SearchHistory
import tech.apps.music.database.offline.WatchLaterSongModel
import tech.apps.music.mediaPlayerYT.MusicServiceConnection
import tech.apps.music.mediaPlayerYT.YoutubeFloatingUI
import tech.apps.music.model.SongModelForList
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.util.Resource
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    private val repository: Repository,
    private val cacheRepository: CacheRepository
) : ViewModel() {

    val recentList: LiveData<List<HistorySongModel>> by lazy { repository.getLast5RecentList() }
    var recommendationList = cacheRepository.getListOfSongTending()
    val playbackState = musicServiceConnection.playbackState

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

    val bufferingTime: LiveData<Boolean> = YoutubeFloatingUI.bufferingTime


    fun getListOfSongWithKeyword(query: String): Flow<Resource<out List<SongModelForList>>> {
        val recommendList = cacheRepository.getListOfSongWithKeyword(query)
        recommendationList = recommendList
        return recommendList
    }

    fun getTrendingList(): Flow<Resource<out List<SongModelForList>>> {
        val recommendList = cacheRepository.getListOfSongTending()
        recommendationList = recommendList
        return recommendList
    }

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

    fun deleteSearchByQuery(queryText: String) {
        viewModelScope.launch {
            repository.deleteSearchByQuery(queryText)
        }
    }

    suspend fun getVideoData(url: String): Pair<String, String>? {
        return cacheRepository.getVideoUri(url)
    }

    fun getVideoIdFromUrl(ytUrl: String): String? {
        return cacheRepository.getVideoIdFromUrl(ytUrl)
    }
}