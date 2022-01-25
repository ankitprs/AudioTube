package tech.apps.music.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_URI
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import tech.apps.music.database.Repository
import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.util.VideoData
import javax.inject.Inject

class YTVideoMusicSource @Inject constructor(
    private val musicDatabase: Repository
) {
    companion object {
        var songs = emptyList<MediaMetadataCompat>()
            private set
    }

    fun fetchSong() {
        state = State.STATE_INITIALIZING
        state = State.STATE_INITIALIZED
    }

    fun stateIn(list: List<YTAudioDataModel>) {
        songs = list.toMetaData()
        state = State.STATE_INITIALIZED
    }

//    suspend fun saveSongPosition(watchedPosition: Long, timing: Long, videoID: String) {
//        musicDatabase.updatingSongPosTime(watchedPosition, timing, videoID)
//    }

    suspend fun savingSongInHistory(historySongModel: HistorySongModel) {
        musicDatabase.insertSongInHistory(historySongModel)
    }

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = State.STATE_CREATED
        set(value) {
            if (value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == State.STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun asMediaSource(
        dataSourceFactory: DefaultDataSource.Factory
    ): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()

        songs.forEach {
            if (it.getString(METADATA_KEY_MEDIA_URI) == "") {
                musicDatabase.getSongModelWithLink(
                    VideoData.getYoutubeLinkFromId(
                        it.getString(
                            METADATA_KEY_MEDIA_ID
                        )
                    )
                ) { audioModel ->
                    if (audioModel != null) {
                        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(audioModel.ytSongUrl.toUri()))
                        concatenatingMediaSource.addMediaSource(mediaSource)
                    } else {
                        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(
                                MediaItem.fromUri(
                                    it.getString(METADATA_KEY_MEDIA_URI).toUri()
                                )
                            )
                        concatenatingMediaSource.addMediaSource(mediaSource)
                    }
                }
            } else {
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(
                        MediaItem.fromUri(
                            it.getString(METADATA_KEY_MEDIA_URI).toUri()
                        )
                    )
                concatenatingMediaSource.addMediaSource(mediaSource)
            }

        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map {
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(it.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(it.description.title)
            .setSubtitle(it.description.subtitle)
            .setMediaId(it.description.mediaId)
            .setIconUri(it.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == State.STATE_CREATED || state == State.STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(state == State.STATE_INITIALIZED)
            true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}