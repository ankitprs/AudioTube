package tech.apps.music.database.offline

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "yt_video_table")
data class YTVideoLink(
    @PrimaryKey val videoId: String = "",
    @ColumnInfo(name = "Timing") val time: Long = 0L,
    @ColumnInfo(name = "YTLink") val link: String = ""
)

@Keep
@Entity(tableName = "yt_video_table_liked")
data class YTVideoLinkLiked(
    @PrimaryKey val videoId: String = "",
    @ColumnInfo(name = "Timing") val time: Long = 0L,
    @ColumnInfo(name = "YTLink") val link: String = ""
)