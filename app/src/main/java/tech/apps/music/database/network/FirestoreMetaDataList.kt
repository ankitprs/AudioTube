package tech.apps.music.database.network

import android.util.Log
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.apps.music.model.EpisodeModel
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
                        Log.d(
                            "FirestoreMetaDataList",
                            "Cached get failed: " + task.result.data.toString()
                        )
                        if (list != null) {
                            callback(list.audiobooksample)
                            Log.d("FirestoreMetaDataList", "Cached get failed: " + list.toString())
                        } else {
                            callback(getEpisodesList())
                        }
                    } catch (err: Exception) {
                        Log.d(TAG, "Cached get failed: ", err)
                        callback(getEpisodesList())
                    }


                } else {
                    callback(getEpisodesList())
                    Log.d(TAG, "Cached get failed: ", task.exception)
                }
            }

        }


    private fun getEpisodesList(): List<EpisodesListModel> {

        val lilyPic =
            "https://raw.githubusercontent.com/ap20u10584/testingDatabase/main/00015b6a-090a-40cd-bc19-f6330fec7421_1024.jpg"
        val lilyAudio =
            "https://github.com/ap20u10584/testingDatabase/blob/main/Lily-by-Alan-Walker-K-391-Emelie-Hollow-Electro-Music.mp3?raw=true"

        val fadedPic =
            "https://raw.githubusercontent.com/ap20u10584/testingDatabase/main/8e2758477b11d7c44d8defe9bf08ffb6.jpg"
        val fadedAudio =
            "https://github.com/ap20u10584/testingDatabase/blob/main/Faded_320(PaglaSongs).mp3?raw=true"

        val alonePic =
            "https://raw.githubusercontent.com/ap20u10584/testingDatabase/main/9fa13acf7fd692b9310adda64cd0be3f.jpg"
        val aloneAudio =
            "https://github.com/ap20u10584/testingDatabase/blob/main/Alan_Walker_-_Alone_Olagist.co_.mp3?raw=true"


        val onMyWayPic =
            "https://raw.githubusercontent.com/ap20u10584/testingDatabase/main/On_My_Way.webp"
        val onMyWayAudio =
            "https://github.com/ap20u10584/testingDatabase/blob/main/Alan_Walker_Ft_Sabrina_Carpenter_Farruko_-_On_My_Way.mp3?raw=true"

        val list: ArrayList<EpisodeModel> = ArrayList()

        val listOfEpisodes: ArrayList<EpisodesListModel> = ArrayList()
        val arrOfPic = arrayOf(lilyPic, fadedPic, alonePic, onMyWayPic)

        val songUrlArray = arrayOf(lilyAudio, fadedAudio, aloneAudio, onMyWayAudio)


        var temp = 11
        var position = 0

        for (i in arrOfPic) {
            for (ar in songUrlArray) {
                temp++
                val episodeModel = EpisodeModel(
                    "${temp}oghk123",
                    ar,
                    "Alan Walker",
                    arrOfPic[position],
                    5425L,
                    System.currentTimeMillis()
                )
                position++
                list.add(episodeModel)
            }
            position = 0
            listOfEpisodes.add(
                EpisodesListModel(
                    temp.toString(),
                    "Alan Walker",
                    "Alan Walker",
                    "2:00:00",
                    i,
                    System.currentTimeMillis(),
                    0,
                    list
                )
            )
        }
        return listOfEpisodes

    }

    data class ListOfEpisodes(var audiobooksample: ArrayList<EpisodesListModel> = ArrayList())
}