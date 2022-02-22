package tech.apps.music.database.offline

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "History_Song_Model")
data class HistorySongModel(
    @PrimaryKey val videoId: String = "",
    @ColumnInfo(name = "Title") val title: String = "",
    @ColumnInfo(name = "Channel") val ChannelName: String = "",
    @ColumnInfo(name = "Duration")val duration: Long = 0L,
    @ColumnInfo(name = "Timing") val time: Long = 0L,
    @ColumnInfo(name = "WatchedPosition") val watchedPosition: Long = 0
)

@Keep
@Entity(tableName = "WatchLater_Song_Model")
data class WatchLaterSongModel(
    @PrimaryKey val videoId: String = "",
    @ColumnInfo(name = "Title") val title: String = "",
    @ColumnInfo(name = "Channel") val ChannelName: String = "",
    @ColumnInfo(name = "Duration")val duration: Long = 0L,
    @ColumnInfo(name = "Timing") val time: Long = 0L,
    @ColumnInfo(name = "WatchedPosition") val watchedPosition: Long = 0
)
@Keep
@Entity(tableName = "Search_History")
data class SearchHistory(
    @PrimaryKey val queryText: String = "",
    @ColumnInfo(name = "Timing") val time: Long = 0L,
)