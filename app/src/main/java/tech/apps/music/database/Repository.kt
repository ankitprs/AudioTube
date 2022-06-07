package tech.apps.music.database

import android.content.Context
import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.database.offline.OfflineDatabase
import tech.apps.music.database.offline.SearchHistory
import tech.apps.music.database.offline.WatchLaterSongModel
import javax.inject.Inject

class Repository
@Inject constructor(
    val context: Context,
    var database: OfflineDatabase
) {

    // History Table Repository
    suspend fun insertSongInHistory(songModel: HistorySongModel) {
        database.getYTVideoDao().insertSongIntoHistory(songModel)
        deleteRecentlyAdded20More()
    }

    private suspend fun deleteRecentlyAdded20More() {
        database.getYTVideoDao().deleteFromHistory20More()
    }

    suspend fun deleteAllHistory() {
        database.getYTVideoDao().deleteAllSongsFromHistory()
    }

    suspend fun getAllSongsLiveData(): List<HistorySongModel> =
        database.getYTVideoDao().getListOfHistory()

    suspend fun getLast5RecentList(): List<HistorySongModel> =
        database.getYTVideoDao().getLast5RecentList()


    // watch Later Table Repo
    suspend fun insertSongIntoWatchLater(watchLaterSongModel: WatchLaterSongModel) {
        database.getYTVideoDao().insertSongIntoWatchLater(watchLaterSongModel)
    }

    suspend fun deleteAllSongsFromWatchLater() {
        database.getYTVideoDao().deleteAllSongsFromWatchLater()
    }

    suspend fun deleteSongFromWatchLater(mediaId: String) {
        database.getYTVideoDao().deleteSongFromWatchLater(mediaId)
    }

    suspend fun getListOfWatchLater(): List<WatchLaterSongModel> =
        database.getYTVideoDao().getListOfWatchLater()

    // list for continue song Repo
    suspend fun getListOfContinue(): List<HistorySongModel> =
        database.getYTVideoDao().getListOfContinue()

    //updating watch time
    suspend fun updatingSongPosTime(watchedPosition: Long, timing: Long, videoID: String) {
        database.getYTVideoDao().updatingSongPosTime(watchedPosition, timing, videoID)
    }

    //Search History
    suspend fun insertSearchQuery(queryText: String) {
        database.getYTVideoDao().insertSearchQuery(
            SearchHistory(
                queryText,
                System.currentTimeMillis()
            )
        )
        database.getYTVideoDao().deleteSearchHistoryMoreThan20()
    }

    suspend fun getListSearchHistory(): List<SearchHistory> =
        database.getYTVideoDao().getListSearchHistory()

    suspend fun deleteSearchByQuery(searchQuery: String){
        database.getYTVideoDao().deleteSearchByQuery(searchQuery)
    }

    suspend fun deleteAllSearchHistory(){
        database.getYTVideoDao().deleteAllSearchHistory()
    }
}