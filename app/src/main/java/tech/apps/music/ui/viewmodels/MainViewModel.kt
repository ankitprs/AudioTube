package tech.apps.music.ui.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import tech.apps.music.database.Repository
import tech.apps.music.database.offline.YTVideoLink
import tech.apps.music.database.offline.YTVideoLinkLiked
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

    val getLikedList: LiveData<List<YTVideoLinkLiked>> = repository.getAllLikedSongs()
    val getRecentList: LiveData<List<YTVideoLink>> = repository.getAllSongsLiveData()

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState

    var repeatAll = false


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

    fun repeatAll() {
        musicServiceConnection.transportControls.setRepeatMode(REPEAT_MODE_ALL)
    }

    fun repeatAllOff() {
        musicServiceConnection.transportControls.setRepeatMode(REPEAT_MODE_NONE)
    }

    fun getRepeatStatus(): Boolean {
        return musicServiceConnection.playbackState.value?.state?.equals(REPEAT_MODE_ALL) == true
    }


    @SuppressLint("LogNotTimber")
    fun playOrToggleSong(mediaItem: YTAudioDataModel, toggle: Boolean = false) {

        if (ytVideoMusicSource.songs.find {
                it.description.mediaId == mediaItem.mediaId
            } == null) {
            repository.songsData.postValue(mediaItem)
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

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }

    fun addSongInRecent(ytLink: String, context: Context, callback: (status: Boolean) -> Unit) {

        repository.getSongModelWithLink(ytLink) { audioModel ->
            if (audioModel != null) {
                viewModelScope.launch {

                    repository.insertLink(
                        YTVideoLink(
                            audioModel.mediaId,
                            System.currentTimeMillis(),
                            ytLink
                        )
                    )
                    playOrToggleSong(audioModel, true)
                    callback(true)
                }
            } else {
                callback(false)
                Toast.makeText(context, "Use only Youtube links", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun songLiked(ytLink: String, mediaId: String) {
        viewModelScope.launch {
            repository.insertLinkToLiked(
                YTVideoLinkLiked(
                    mediaId,
                    System.currentTimeMillis(),
                    ytLink
                )
            )
        }
    }

    fun songDisLiked(ytLink: String, mediaId: String) {
        viewModelScope.launch {
            repository.deleteLiked(
                YTVideoLinkLiked(
                    mediaId,
                    System.currentTimeMillis(),
                    ytLink
                )
            )
        }
    }

    fun deleteRecentlyAdded5More(time5More: Long){
        viewModelScope.launch {
            repository.deleteRecentlyAdded5More(time5More)
        }
    }

}