package tech.apps.music.ui.fragments

import android.app.Application
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import tech.apps.music.database.Repository
import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.database.offline.WatchLaterSongModel
import tech.apps.music.exoplayer.*
import tech.apps.music.model.EpisodesListModel
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.others.Constants
import tech.apps.music.others.Constants.MEDIA_ROOT_ID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val musicServiceConnection: MusicServiceConnection,
    private val repository: Repository,
    private val ytVideoMusicSource: YTVideoMusicSource
): AndroidViewModel(application) {

    //    val getContinueWatchingList: LiveData<List<HistorySongModel>> = repository.getListOfContinue()
    val getRecentList: LiveData<List<HistorySongModel>> = repository.getAllSongsLiveData()
    val getWatchLaterList: LiveData<List<WatchLaterSongModel>> = repository.getListOfWatchLater()
    val getLast5RecentList: LiveData<List<HistorySongModel>> = repository.getLast5RecentList()

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState

    var listOfAudioBooks: MutableLiveData<List<EpisodesListModel>> = MutableLiveData()

    init {
        musicServiceConnection.subscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                }
            })

    }

    fun skipToNextSong() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun setPlaybackSpeed(speed: Float) {
        musicServiceConnection.transportControls.setPlaybackSpeed(speed)
    }
    fun repeat(){
        musicServiceConnection.transportControls.setRepeatMode(REPEAT_MODE_ALL)
    }

    fun fastForwardSong() {
        musicServiceConnection.transportControls.fastForward()
        musicServiceConnection.transportControls.rewind()
    }

    fun replayBackSong() {
        musicServiceConnection.transportControls.rewind()
        musicServiceConnection.transportControls.rewind()
    }

    fun playOrToggleSong(
        mediaItem: YTAudioDataModel,
        toggle: Boolean = false,
        watchedPosition: Long = 0L
    ) {
        if (YTVideoMusicSource.songs.find {
                it.description.mediaId == mediaItem.mediaId
            } == null) {
            ytVideoMusicSource.stateIn(listOf(mediaItem))
        }

        val isPrepared = playbackState.value?.isPrepared ?: false

        if (isPrepared && mediaItem.mediaId == curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let {
                when {
                    it.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    it.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            val bundle = Bundle()
            bundle.putLong(Constants.PASSING_SONG_LAST_WATCHED_POS, watchedPosition)
            if(mediaItem.mediaId.isNotEmpty()){
                musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, bundle)
            }
        }
    }

    fun playOrToggleListOfSongs(
        mediaItem: List<YTAudioDataModel>,
        toggle: Boolean = false,
        position: Int = 0,
        watchedPosition: Long = 0L
    ) {
        if (YTVideoMusicSource.songs.find {
                it.description.mediaId == mediaItem[position].mediaId
            } == null) {
            ytVideoMusicSource.stateIn(mediaItem)
        }

        val isPrepared = playbackState.value?.isPrepared ?: false

        if (isPrepared && mediaItem[position].mediaId == curPlayingSong.value?.getString(
                METADATA_KEY_MEDIA_ID
            )
        ) {
            playbackState.value?.let {
                when {
                    it.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    it.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            val bundle = Bundle()
            bundle.putLong(Constants.PASSING_SONG_LAST_WATCHED_POS, watchedPosition)
            musicServiceConnection.transportControls.playFromMediaId(
                mediaItem[position].mediaId,
                bundle
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }

    fun addSongInRecent(ytLink: String, callback: (status: Boolean) -> Unit) {

        repository.getSongModelWithLink(ytLink) { audioModel ->

            Firebase.analytics.logEvent("Video_Played") {
                param("Video_ID", audioModel?.mediaId.toString())
                param("Video_Title", audioModel?.title.toString())
                param("Video_Channel_Name", audioModel?.author.toString())
            }
            if (audioModel != null) {
                playOrToggleSong(audioModel, true)
                changeIsYoutubeVideoCurSong(true)
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

    fun isYoutubeVideoCurSong(): Boolean = MusicService.isYoutubeVideoCurSong

    fun changeIsYoutubeVideoCurSong(isYoutubeVideoCurSong: Boolean) {
        MusicService.isYoutubeVideoCurSong = isYoutubeVideoCurSong
    }

    fun currentlyPlayingPlaylist() = YTVideoMusicSource.songs

    fun gotoIndex(index: Long){
        musicServiceConnection.transportControls.skipToQueueItem(index)
    }
}