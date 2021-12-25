package tech.apps.music.database.offline

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface YTVideoDao {

    // History Table Dao
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongIntoHistory(historySongModel: HistorySongModel)

    @Query("DELETE FROM History_Song_Model")
    suspend fun deleteAllSongsFromHistory()

    @Query("DELETE FROM History_Song_Model WHERE Timing <= :time20More")
    suspend fun deleteFromHistory20More(time20More: Long)

    @Query("SELECT * FROM History_Song_Model ORDER BY Timing DESC")
    fun getListOfHistory(): LiveData<List<HistorySongModel>>



    //Watch Later Table Dao
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongIntoWatchLater(watchLaterSongModel: WatchLaterSongModel)

    @Query("DELETE FROM WatchLater_Song_Model")
    suspend fun deleteAllSongsFromWatchLater()

    @Query("SELECT * FROM WatchLater_Song_Model ORDER BY Timing DESC")
    fun getListOfWatchLater() : LiveData<List<WatchLaterSongModel>>

    @Query("DELETE FROM WatchLater_Song_Model WHERE videoId <= :mediaId")
    suspend fun deleteSongFromWatchLater(mediaId: String)



    // list for continue song
    @Query("SELECT * FROM History_Song_Model WHERE WatchedPosition != 0 ORDER BY Timing DESC ")
    fun getListOfContinue(): LiveData<List<HistorySongModel>>



    // updating the position
    @Query("UPDATE History_Song_Model SET WatchedPosition = :watchedPosition, Timing = :timing  WHERE videoId = :videoID ")
    suspend fun updatingSongPosTime(watchedPosition: Long,timing: Long, videoID: String)
}