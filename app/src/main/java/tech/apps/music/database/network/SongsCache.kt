package tech.apps.music.database.network

import tech.apps.music.model.YTAudioDataModel

object SongsCache {
    val SongsMap=mutableMapOf<String,SongsCacheModel>()
}
data class SongsCacheModel(var time:Long=0L,
                           var ytAudioDataModel: YTAudioDataModel)