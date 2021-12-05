package tech.apps.music.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tech.apps.music.database.network.YTVideoExtractor
import tech.apps.music.database.offline.YTVideoDatabase
import tech.apps.music.database.offline.YTVideoLink
import tech.apps.music.database.offline.YTVideoLinkLiked
import tech.apps.music.model.YTAudioDataModel
import javax.inject.Inject

class Repository
@Inject constructor(
    val context: Context
) {
    val database: YTVideoDatabase = YTVideoDatabase.getDatabase(context)

    suspend fun insertLink(ytVideoLink: YTVideoLink) {
        database.getYTVideoDao().insertLink(ytVideoLink)
    }
    suspend fun deleteRecentlyAdded5More(time5More: Long){
        database.getYTVideoDao().deleteRecentlyAdded5More(time5More)
    }

    suspend fun insertLinkToLiked(ytVideoLinkLiked: YTVideoLinkLiked) {
        database.getYTVideoDao().insertLinkToLiked(ytVideoLinkLiked)
    }

    suspend fun deleteLiked(ytVideoLink: YTVideoLinkLiked) {
        database.getYTVideoDao().deleteLiked(ytVideoLink)
    }

    suspend fun deleteAllHistory() {
        database.getYTVideoDao().deleteAllHistory()
    }

    val songsData: MutableLiveData<YTAudioDataModel> = MutableLiveData<YTAudioDataModel>()

    fun getAllSongsLiveData(): LiveData<List<YTVideoLink>> =
        database.getYTVideoDao().getAllSongsLiveData()

    fun getAllLikedSongs(): LiveData<List<YTVideoLinkLiked>> =
        database.getYTVideoDao().getAllLikedSongs()

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