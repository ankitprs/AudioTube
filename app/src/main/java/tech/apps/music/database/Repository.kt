package tech.apps.music.database

import android.content.Context
import androidx.lifecycle.LiveData
import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.database.offline.OfflineDatabase
import tech.apps.music.database.offline.WatchLaterSongModel
import tech.apps.music.model.YTAudioDataModel
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
    private suspend fun deleteRecentlyAdded20More(){
        database.getYTVideoDao().deleteFromHistory20More()
    }
    suspend fun deleteAllHistory() {
        database.getYTVideoDao().deleteAllSongsFromHistory()
    }
    fun getAllSongsLiveData(): LiveData<List<HistorySongModel>> =
        database.getYTVideoDao().getListOfHistory()

    fun getLast5RecentList(): LiveData<List<HistorySongModel>> = database.getYTVideoDao().getLast5RecentList()


    // watch Later Table Repo
    suspend fun insertSongIntoWatchLater(watchLaterSongModel: WatchLaterSongModel){
        database.getYTVideoDao().insertSongIntoWatchLater(watchLaterSongModel)
    }
    suspend fun deleteAllSongsFromWatchLater(){
        database.getYTVideoDao().deleteAllSongsFromWatchLater()
    }
    suspend fun deleteSongFromWatchLater(mediaId: String){
        database.getYTVideoDao().deleteSongFromWatchLater(mediaId)
    }
    fun getListOfWatchLater() : LiveData<List<WatchLaterSongModel>> =
        database.getYTVideoDao().getListOfWatchLater()



    // list for continue song Repo
    fun getListOfContinue(): LiveData<List<HistorySongModel>> =
        database.getYTVideoDao().getListOfContinue()



    //updating watch time
    suspend fun updatingSongPosTime(watchedPosition: Long,timing: Long, videoID: String){
        database.getYTVideoDao().updatingSongPosTime(watchedPosition,timing,videoID)
    }





    // networkCall
    fun getSongModelWithLink(ytUrl: String, callback: (ytModel: YTAudioDataModel?) -> Unit) {
        callback(null)
    }

    //Search History
    suspend fun insertSearchQuery(queryText: String){
//        database.getYTVideoDao().insertSearchQuery(SearchHistory(
//            queryText,
//            System.currentTimeMillis()
//        ))
//        database.getYTVideoDao().deleteSearchHistoryMoreThan20()
    }
//    suspend fun getListSearchHistory(): List<SearchHistory> = database.getYTVideoDao().getListSearchHistory()
}