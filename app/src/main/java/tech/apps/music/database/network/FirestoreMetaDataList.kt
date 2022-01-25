package tech.apps.music.database.network

import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.apps.music.model.EpisodesListModel

class FirestoreMetaDataList {

    private val db = Firebase.firestore
    private val query = db.collection("Audiotube_Content")
        .document("AudioBook")

    suspend fun getEpisodesListFromFirestore(callback: (list: List<EpisodesListModel>?) -> Unit) =
        withContext(Dispatchers.IO) {

            val source = Source.DEFAULT

            query.get(source).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    try {
                        val list: ListOfEpisodes? = task.result.toObject<ListOfEpisodes>()

                        if (list != null) {
                            callback(list.audiobooksample)
                        } else {
                            callback(null)
                        }
                    } catch (err: Exception) {
                        callback(null)
                    }

                } else {
                    callback(null)
                }
            }

        }

    data class ListOfEpisodes(var audiobooksample: ArrayList<EpisodesListModel> = ArrayList())
}