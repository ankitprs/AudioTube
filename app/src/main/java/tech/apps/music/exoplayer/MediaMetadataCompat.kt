package tech.apps.music.exoplayer

import android.media.MediaMetadata.*
import android.support.v4.media.MediaMetadataCompat
import tech.apps.music.model.YTAudioDataModel

fun MediaMetadataCompat.toSong():YTAudioDataModel?{
    return description?.let {
        YTAudioDataModel(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}

fun YTAudioDataModel.toMetaData():MediaMetadataCompat{
    return let {model->
        MediaMetadataCompat.Builder()
            .putString(METADATA_KEY_ARTIST,model.author)
            .putString(METADATA_KEY_MEDIA_ID,model.mediaId)
            .putString(METADATA_KEY_TITLE,model.title)
            .putString(METADATA_KEY_DISPLAY_TITLE,model.title)
            .putString(METADATA_KEY_DISPLAY_ICON_URI, model.thumbnailUrl)
            .putString(METADATA_KEY_DISPLAY_SUBTITLE, model.author)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,model.ytSongUrl)
            .putString(METADATA_KEY_ALBUM_ART_URI,model.thumbnailUrl)
            .putString(METADATA_KEY_DISPLAY_DESCRIPTION,model.author)
            .build()
    }
}
