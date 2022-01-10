package tech.apps.music.model

import tech.apps.music.util.VideoData

data class SongModelForList(
    var videoId: String = "",
    var title: String = "",
    var ChannelName: String = "",
    var duration: Long = 0L,
    var time: Long = 0L,
    var watchedPosition: Long = 0,
    var durationText: String = ""
)

fun  List<SongModelForList>.toYtAudioDataModel(): List<YTAudioDataModel> {
    val list = ArrayList<YTAudioDataModel> ()
    forEach {
        list.add(YTAudioDataModel(
            it.videoId,
            it.title,
            it.ChannelName,
            "",
            VideoData.getThumbnailFromId(it.videoId),
            it.duration
        ))
    }
    return list
}
