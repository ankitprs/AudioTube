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
                    val audioLinkNullable: String? = ytFiles[iTag].url ?: ytFiles[iTag - 1].url

                    if ( audioLinkNullable == null){
                        callback(null)
                        return
                    }

                    val audioLink: String = audioLinkNullable

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
                    }
                }
            }
        obj.extract(ytLink)
    }
}