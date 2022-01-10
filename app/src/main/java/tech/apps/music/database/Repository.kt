package tech.apps.music.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tech.apps.music.database.network.FirestoreMetaDataList
import tech.apps.music.database.network.SongsMap
import tech.apps.music.database.network.YTVideoExtractor
import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.database.offline.OfflineDatabase
import tech.apps.music.database.offline.WatchLaterSongModel
import tech.apps.music.model.EpisodesListModel
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



    val songsData: MutableLiveData<List<YTAudioDataModel>> = MutableLiveData<List<YTAudioDataModel>>()



    // networkCall
    fun getSongModelWithLink(ytUrl: String, callback: (ytModel: YTAudioDataModel?) -> Unit) {

        YTVideoExtractor.getObject(context, ytUrl) { audioModel ->
            if (audioModel != null) {
                callback(audioModel)
            } else {
                callback(null)
            }
        }
    }


    // youtube video cashing
    fun getSongFromCache(ytLink: String): YTAudioDataModel?{
        val cacheSong = SongsMap[ytLink]
        val currentTiming = System.currentTimeMillis()
        return if (cacheSong != null && currentTiming - cacheSong.time < 18000000L) {
            (cacheSong.ytAudioDataModel)

        }else{
            null
        }
    }



    // get List Of AudioBook From Firestore
    suspend fun getListOfAudioBooks(callback: (list: List<EpisodesListModel>? ) -> Unit ){
        FirestoreMetaDataList().getEpisodesListFromFirestore(){
            callback(it)
        }
    }

}