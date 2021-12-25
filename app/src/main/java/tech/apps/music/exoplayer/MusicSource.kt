package tech.apps.music.exoplayer

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat

interface MusicSource : Iterable<MediaMetadataCompat> {
    suspend fun load()

    fun whenReady(performAction: (Boolean) -> Unit): Boolean

    fun search(query: String, extras: Bundle): List<MediaMetadataCompat>
}

abstract class  AbstractMusicSource: MusicSource