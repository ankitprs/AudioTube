package tech.apps.music.model

data class SongModelForList(
    var videoId: String = "",
    var title: String = "",
    var ChannelName: String = "",
    var duration: Long = 0L,
    var time: Long = 0L,
    var watchedPosition: Long = 0,
    var durationText: String = ""
)
