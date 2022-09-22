package tech.apps.music.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.database.offline.WatchLaterSongModel
import tech.apps.music.util.getThumbnailFromId

@Keep
@Entity(tableName = "SongListWithQuery")
data class SongModelForList(
    @PrimaryKey var videoId: String = "",
    var title: String = "",
    var ChannelName: String = "",
    var duration: Long = 0L,
    var time: Long = 0L,
    var watchedPosition: Long = 0,
    var durationText: String = "",
    var query: String = "",
)

fun List<SongModelForList>.toYtAudioDataModel(): List<YTAudioDataModel> {
    val list = ArrayList<YTAudioDataModel>()
    forEach {
        list.add(
            YTAudioDataModel(
                it.videoId,
                it.title,
                it.ChannelName,
                getThumbnailFromId(it.videoId),
                it.duration
            )
        )
    }
    return list
}

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