package tech.apps.music.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tech.apps.music.database.network.YTVideoExtractor
import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.database.offline.OfflineDatabase
import tech.apps.music.database.offline.WatchLaterSongModel
import tech.apps.music.model.YTAudioDataModel
import javax.inject.Inject

class Repository
@Inject constructor(
    val context: Context
) {
    val database: OfflineDatabase = OfflineDatabase.getDatabase(context)

    // History Table Repository
    suspend fun insertSongInHistory(songModel: HistorySongModel) {
        database.getYTVideoDao().insertSongIntoHistory(songModel)
    }
    suspend fun deleteRecentlyAdded5More(time20More: Long){
        database.getYTVideoDao().deleteFromHistory20More(time20More)
    }
    suspend fun deleteAllHistory() {
        database.getYTVideoDao().deleteAllSongsFromHistory()
    }
    fun getAllSongsLiveData(): LiveData<List<HistorySongModel>> =
        database.getYTVideoDao().getListOfHistory()


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


    fun addingSongFromLink(ytUrl: String, callback: (ytModel: YTAudioDataModel?) -> Unit){
        try{
            YTVideoExtractor.getObject(context, ytUrl) { audioModel ->
                if (audioModel != null) {
                    callback(audioModel)
                }
                callback(null)
            }
        }catch (err: Exception){
            callback(null)
        }
    }


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
}