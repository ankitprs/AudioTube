package tech.apps.music.model

import android.support.v4.media.MediaMetadataCompat

data class YTAudioDataModel(
    var mediaId: String = "",
    var title: String = "",
    var author: String = "",
    var thumbnailUrl: String = "",
    var duration: Long = 0L
)

fun MediaMetadataCompat?.toYTAudioModel(): YTAudioDataModel? {
    this?.description?.let {
        return YTAudioDataModel(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.iconUri.toString(),
            0L
        )
    }
    return null
}