package tech.apps.music.model

data class EpisodesListModel(
    var id: String = "",
    var title: String = "",
    var author: String = "",
    var duration: String = "",
    var thumbnailUrl: String = "",
    var time: Long = 0L,
    var episodePosition: Int = 0,
    var EpisodesModel: ArrayList<EpisodeModel> = ArrayList()
)

data class EpisodeModel(
    var songId: String = "",
    var songUrl: String = "",
    var title: String = "",
    var songThumbnailUrl: String = "",
    var duration: Long = 0L,
    var time: Long = 0L,
    var watchedPosition: Long = 0L,
)

fun List<YTAudioDataModel>.toEpisodes(): List<EpisodeModel> {
    val list: ArrayList<EpisodeModel> = ArrayList()
    forEach {
        list.add(
            EpisodeModel(
                it.mediaId,
                it.ytSongUrl,
                it.title,
                it.thumbnailUrl
            )
        )
    }
    return list
}

fun EpisodesListModel.ytAudioDataModel(): List<YTAudioDataModel> {
    val list: ArrayList<YTAudioDataModel> = ArrayList()
    EpisodesModel.forEach {
        list.add(
            YTAudioDataModel(
                it.songId,
                it.title,
                author,
                it.songUrl,
                it.songThumbnailUrl,
                it.duration
            )
        )
    }
    return list
}

fun EpisodeModel.ytAudioDataModel(): YTAudioDataModel {
    return YTAudioDataModel(
        songId,
        title,
        "",
        songUrl,
        songThumbnailUrl,
        duration
    )
}
