package tech.apps.music.database.network

import androidx.annotation.Keep
import tech.apps.music.model.EpisodesListModel

class FirestoreMetaDataList {

//    private val db = Firebase.firestore
//    private val query = db.collection("Audiotube_Content")
//        .document("AudioBook")
//
//    suspend fun getEpisodesListFromFirestore(callback: (list: List<EpisodesListModel>?) -> Unit) =
//        withContext(Dispatchers.IO) {
//
//            val source = Source.CACHE
//
//            query.get(source).addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//
//                    try {
//                        val list: ListOfEpisodes? = task.result.toObject<ListOfEpisodes>()
//
//                        if (list != null) {
//                            callback(list.audiobooksample)
//                        } else {
//                            callback(null)
//                        }
//                    } catch (err: Exception) {
//                        callback(null)
//                    }
//
//                } else {
//                    callback(null)
//                }
//            }
//
//        }

    @Keep
    data class ListOfEpisodes(var audiobooksample: ArrayList<EpisodesListModel> = ArrayList())
}