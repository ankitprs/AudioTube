package tech.apps.music.database.offline

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

    @Query("DELETE FROM History_Song_Model where videoId NOT IN (SELECT videoId from History_Song_Model ORDER BY timing DESC LIMIT 20)")
    suspend fun deleteFromHistory20More()

    @Query("SELECT * FROM History_Song_Model ORDER BY Timing DESC")
    suspend fun getListOfHistory(): List<HistorySongModel>

    @Query("SELECT * FROM History_Song_Model ORDER BY timing DESC  LIMIT 5")
    suspend fun getLast5RecentList(): List<HistorySongModel>


    //Watch Later Table Dao
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongIntoWatchLater(watchLaterSongModel: WatchLaterSongModel)

    @Query("DELETE FROM WatchLater_Song_Model")
    suspend fun deleteAllSongsFromWatchLater()

    @Query("SELECT * FROM WatchLater_Song_Model ORDER BY Timing DESC")
    suspend fun getListOfWatchLater(): List<WatchLaterSongModel>

    @Query("DELETE FROM WatchLater_Song_Model WHERE videoId = :mediaId")
    suspend fun deleteSongFromWatchLater(mediaId: String)


    // list for continue song
    @Query("SELECT * FROM History_Song_Model WHERE WatchedPosition != 0 ORDER BY Timing DESC ")
    suspend fun getListOfContinue(): List<HistorySongModel>


    // updating the position
    @Query("UPDATE History_Song_Model SET WatchedPosition = :watchedPosition, Timing = :timing  WHERE videoId = :videoID ")
    suspend fun updatingSongPosTime(watchedPosition: Long,timing: Long, videoID: String)


    //Search History Table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(searchHistory: SearchHistory)

    @Query("SELECT * FROM Search_History ORDER BY Timing DESC")
    suspend fun getListSearchHistory(): List<SearchHistory>

    @Query("DELETE FROM Search_History where queryText NOT IN (SELECT queryText from Search_History ORDER BY timing DESC LIMIT 20)")
    suspend fun deleteSearchHistoryMoreThan20()
}