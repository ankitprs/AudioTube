package tech.apps.music.database.offline

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface YTVideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(ytVideoLink: YTVideoLink)

    @Query("DELETE FROM YT_VIDEO_TABLE WHERE Timing <= :time5More")
    suspend fun deleteRecentlyAdded5More(time5More: Long)

    @Delete
    suspend fun deleteLiked(ytVideoLinkLiked: YTVideoLinkLiked)

    @Query("DELETE FROM YT_VIDEO_TABLE")
    suspend fun deleteAllHistory()

    @Query("SELECT * FROM YT_VIDEO_TABLE ORDER BY Timing DESC")
    fun getAllSongs(): List<YTVideoLink>

    @Query("SELECT * FROM YT_VIDEO_TABLE_LIKED ORDER BY Timing DESC")
    fun getAllLikedSongs(): LiveData<List<YTVideoLinkLiked>>

    @Query("SELECT * FROM YT_VIDEO_TABLE ORDER BY Timing DESC")
    fun getAllSongsLiveData(): LiveData<List<YTVideoLink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkToLiked(ytVideoLinkLiked: YTVideoLinkLiked)
}