package tech.apps.music.model

import java.io.Serializable

data class EpisodesListModel(
    var id: String = "",
    var title: String = "",
    var author: String = "",
    var duration: String = "",
    var thumbnailUrl: String = "",
    var time: Long = 0L,
    var episodePosition: Int = 0,
    var EpisodesModel: ArrayList<EpisodeModel> = ArrayList()
) : Serializable

data class EpisodeModel(
    var songId: String = "",
    var songUrl: String = "",
    var title: String = "",
    var duration: Long = 0L,
    var time: Long = 0L,
    var watchedPosition: Long = 0L,
)