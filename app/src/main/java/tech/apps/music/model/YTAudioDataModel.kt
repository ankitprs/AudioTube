package tech.apps.music.model

data class YTAudioDataModel(
    var mediaId: String = "",
    var title: String = "",
    var author: String = "",
    var thumbnailUrl: String = "",
    var duration: Long = 0L
)