package tech.apps.music.database.offline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.apps.music.model.SongModelForList

@Dao
interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSongList(songModelWithQuery: List<SongModelForList>)

    @Query("SELECT * FROM SongListWithQuery where `query` == :query")
    fun getListOfQuery(query: String): Flow<List<SongModelForList>>

    @Query("DELETE FROM SongListWithQuery where `query` == :query")
    fun deleteListByQuery(query: String)
}