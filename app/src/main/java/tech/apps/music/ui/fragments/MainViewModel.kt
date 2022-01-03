package tech.apps.music.ui.fragments

import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.others.Constants.MEDIA_ROOT_ID
import tech.apps.music.others.Resource
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    private val repository: Repository,
    private val ytVideoMusicSource: YTVideoMusicSource
) : ViewModel() {

    private val _mediaItems = MutableLiveData<Resource<List<YTAudioDataModel>>>()

    val mediaItems: LiveData<Resource<List<YTAudioDataModel>>> = _mediaItems

    val getContinueWatchingList: LiveData<List<HistorySongModel>> = repository.getListOfContinue()
    val getRecentList: LiveData<List<HistorySongModel>> = repository.getAllSongsLiveData()
    val getWatchLaterList: LiveData<List<WatchLaterSongModel>> = repository.getListOfWatchLater()

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)

                    val item = children.map {
                        YTAudioDataModel(
                            it.mediaId!!,
                            it.description.title.toString(),
                            it.description.subtitle.toString(),
                            it.description.mediaUri.toString(),
                            it.description.iconUri.toString()
                        )
                    }
                    _mediaItems.postValue(Resource.success(item))
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

    fun fastForwardSong() {
        musicServiceConnection.transportControls.fastForward()
        musicServiceConnection.transportControls.rewind()
    }

    fun replayBackSong() {
        musicServiceConnection.transportControls.rewind()
        musicServiceConnection.transportControls.rewind()
    }

    fun playOrToggleSong(mediaItem: YTAudioDataModel, toggle: Boolean = false) {

        if (ytVideoMusicSource.songs.find {
                it.description.mediaId == mediaItem.mediaId
            } == null) {
            repository.songsData.postValue(listOf(mediaItem))
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
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    fun playOrToggleListOfSongs(
        mediaItem: List<YTAudioDataModel>,
        toggle: Boolean = false,
        position: Int = 0
    ) {
        if (ytVideoMusicSource.songs.find {
                it.description.mediaId == mediaItem[position].mediaId
            } == null) {
            repository.songsData.postValue(mediaItem)
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
            musicServiceConnection.transportControls.playFromMediaId(
                mediaItem[position].mediaId,
                null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }

    fun addSongInRecent(ytLink: String, context: Context, callback: (status: Boolean) -> Unit) {

        repository.getSongModelWithLink(ytLink) { audioModel ->

            Firebase.analytics.logEvent("Video_Played") {
                param("Video_ID", audioModel?.mediaId.toString())
                param("Video_Title", audioModel?.title.toString())
                param("Video_Channel_Name", audioModel?.author.toString())
            }

            if (audioModel != null) {
                viewModelScope.launch {

                    repository.insertSongInHistory(
                        HistorySongModel(
                            audioModel.mediaId,
                            audioModel.title,
                            audioModel.author,
                            audioModel.duration,
                            System.currentTimeMillis(),
                            0
                        )
                    )
                    playOrToggleSong(audioModel, true)
                    callback(true)
                }
            } else {
                callback(false)
                Toast.makeText(
                    context,
                    "This video can't play Try Somethings else",
                    Toast.LENGTH_LONG
                ).show()
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

    fun deleteRecentlyAdded5More(time20More: Long) {
        viewModelScope.launch {
            repository.deleteRecentlyAdded5More(time20More)
        }
    }

    fun getSongFromCache(ytLink: String): YTAudioDataModel?=
        repository.getSongFromCache(ytLink)

}