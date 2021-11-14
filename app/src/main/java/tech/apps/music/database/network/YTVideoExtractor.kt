package tech.apps.music.database.network

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.google.firebase.analytics.FirebaseAnalytics
import tech.apps.music.model.YTAudioDataModel

object YTVideoExtractor {

    fun getObject(
        context: Context,
        ytLink: String,
        callback: (ytAudioDataModel: YTAudioDataModel?) -> Unit
    ) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

        val cacheSong = SongsCache.SongsMap[ytLink]
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
                            vMeta.hqImageUrl,
                            vMeta.videoLength
                        )
                        val bundle = Bundle()
                        bundle.putString("YTVideoLink", ytLink)
                        firebaseAnalytics.logEvent("MusicRequest", bundle)
                        callback(song)
                        val songCache = SongsCacheModel(System.currentTimeMillis(), song)
                        if (cacheSong == null) {
                            SongsCache.SongsMap[ytLink] = songCache
                        } else {
                            songCache.ytAudioDataModel = songCache.ytAudioDataModel
                            songCache.time = songCache.time
                        }
                    }
                }
            }
        obj.extract(ytLink)
    }
}