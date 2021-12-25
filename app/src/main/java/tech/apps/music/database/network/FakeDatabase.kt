package tech.apps.music.database.network

import tech.apps.music.model.EpisodeModel
import tech.apps.music.model.EpisodesListModel

object FakeDatabase {

    fun getEpisodesList(): List<EpisodesListModel> {

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

        val episodeModel = EpisodeModel(
            "r241",
            "https://github.com/ap20u10584/testingDatabase/blob/main/Alan_Walker_-_Alone_Olagist.co_.mp3?raw=true",
            "Alan Walker",
            5425L,
            System.currentTimeMillis()
        )
        var temp = 11
        for (i in arrOfPic) {
            for (ar in songUrlArray) {
                episodeModel.songId += temp
                temp++
                episodeModel.songUrl = ar
                list.add(episodeModel)
            }
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
}