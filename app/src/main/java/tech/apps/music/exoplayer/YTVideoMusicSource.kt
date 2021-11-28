package tech.apps.music.exoplayer

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_URI
import androidx.core.net.toUri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.apps.music.database.Repository
import javax.inject.Inject

class YTVideoMusicSource @Inject constructor(
    private val musicDatabase: Repository
) {
    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchSong() = withContext(Dispatchers.IO) {

        state = State.STATE_INITIALIZING

        withContext(Dispatchers.Main) {
            state = State.STATE_INITIALIZING
            musicDatabase.songsData.observeForever {
                if (it != null) {
                    songs = listOf(it.toMetaData())
//                    + songs

                    state = State.STATE_INITIALIZED
                }
            }
        }
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
        dataSourceFactory: DefaultDataSourceFactory
    ): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach {
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(it.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

//    fun asMediaItems()=songs.map{
//        val desc= MediaDescriptionCompat.Builder()
//            .setMediaUri(it.getString(METADATA_KEY_MEDIA_URI).toUri())
//            .setTitle(it.description.title)
//            .setSubtitle(it.description.subtitle)
//            .setMediaId(it.description.mediaId)
//            .setIconUri(it.description.iconUri)
//            .build()
//        MediaBrowserCompat.MediaItem(desc,FLAG_PLAYABLE)
//    }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == State.STATE_CREATED || state == State.STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(state == State.STATE_INITIALIZED)
            false
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}