package tech.apps.music.database.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseArray
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.util.VideoData

object YTVideoExtractor {

    fun getObject(
        context: Context,
        ytLink: String,
        callback: (ytAudioDataModel: YTAudioDataModel?) -> Unit
    ) {

        val cacheSong = SongsMap[ytLink]
        val currentTiming = System.currentTimeMillis()
        if (cacheSong != null && currentTiming - cacheSong.time < 18000000L) {
            callback(cacheSong.ytAudioDataModel)
            return
        }
        val obj =
            @SuppressLint("StaticFieldLeak")
            object : YouTubeExtractor(context) {
                override fun onExtractionComplete(
                    ytFiles: SparseArray<YtFile>?,
                    vMeta: VideoMeta?
                ) {

                    if (ytFiles == null) {
                        callback(null)
                        return
                    }
                    val iTag = 251
                    val audioLink: String = ytFiles[iTag].url ?: ytFiles[iTag - 1].url

                    if (vMeta != null) {
                        val song = YTAudioDataModel(
                            vMeta.videoId,
                            vMeta.title,
                            vMeta.author,
                            audioLink,
                            VideoData.getThumbnailFromId(vMeta.videoId),
                            vMeta.videoLength
                        )
                        callback(song)
                        val songCache = SongsCacheModel(System.currentTimeMillis(), song)
                        if (cacheSong == null) {
                            SongsMap[ytLink] = songCache
                        } else {
                            songCache.ytAudioDataModel = songCache.ytAudioDataModel
                            songCache.time = songCache.time
                        }
                    }
                }
            }
        obj.extract(ytLink)
    }
    fun clearCache(){
        SongsMap.clear()
    }
}