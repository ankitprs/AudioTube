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
//        if(getAllSongs().size > 5){
//            database.getYTVideoDao().deleteLink(getAllSongs()[4])
//            database.getYTVideoDao().insertLink(ytVideoLink)
//        }else{
            database.getYTVideoDao().insertLink(ytVideoLink)
//        }
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

    private fun getAllSongs(): List<YTVideoLink> = database.getYTVideoDao().getAllSongs()

    fun getAllSongsLiveData(): LiveData<List<YTVideoLink>> =
        database.getYTVideoDao().getAllSongsLiveData()

    fun getAllLikedSongs(): LiveData<List<YTVideoLinkLiked>> =
        database.getYTVideoDao().getAllLikedSongs()


    fun getSongModelWithLink(ytUrl: String, callback: (ytModel: YTAudioDataModel?) -> Unit) {

        YTVideoExtractor.getObject(context, ytUrl) { audioModel ->
            if (audioModel != null) {
                callback(audioModel)
                println(audioModel.ytSongUrl)

            } else {
                callback(null)
            }
        }
    }
}