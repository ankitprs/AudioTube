package tech.apps.music.model

import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.database.offline.WatchLaterSongModel

fun List<HistorySongModel>.toSongModelForList(): List<SongModelForList> {
    return let { listSongs ->
        val list: MutableList<SongModelForList> = mutableListOf()
        listSongs.forEach {
            list += SongModelForList(
                it.videoId,
                it.title,
                it.ChannelName,
                it.duration,
                it.time,
                it.watchedPosition
            )
        }
        list
    }
}

fun List<WatchLaterSongModel>.toSongForList(): List<SongModelForList> {
    return let { listSongs ->
        val list: MutableList<SongModelForList> = mutableListOf()
        listSongs.forEach {
            list += SongModelForList(
                it.videoId,
                it.title,
                it.ChannelName,
                it.duration,
                it.time,
                it.watchedPosition
            )
        }
        list
    }
}