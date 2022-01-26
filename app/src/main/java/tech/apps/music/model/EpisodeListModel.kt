package tech.apps.music.model

import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.Keep

@Keep
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

@Keep
data class EpisodeModel(
    var songId: String = "",
    var songUrl: String = "",
    var title: String = "",
    var songThumbnailUrl: String = "",
    var duration: Long = 0L,
    var time: Long = 0L,
    var watchedPosition: Long = 0L,
)

fun List<MediaMetadataCompat>.toEpisodes(): List<EpisodeModel> {
    val list: ArrayList<EpisodeModel> = ArrayList()
    forEach {
        val description = it.description
        list.add(
            EpisodeModel(
                description.mediaId.toString(),
                description.mediaUri.toString(),
                description.title.toString(),
                description.iconUri.toString()
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
