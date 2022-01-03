package tech.apps.music.database.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.util.BasicStorage
import java.util.regex.Matcher
import java.util.regex.Pattern

class YoutubeVideoExtractor {
    suspend fun getYoutubeVideoData(
        videoId: String,
        callback: (data: ArrayList<YTAudioDataModel>) -> Unit
    ) {
        if (!isInternetExist()) {
            return
        }
        withContext(Dispatchers.IO) {
            try {
                YoutubeRetrofitInstance.ytSearchInstant.ytGetVideo(videoId)
                    .enqueue(object : retrofit2.Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (!response.isSuccessful) {
                                callback(ArrayList())
                            }
                            val resp = response.body()?.string()
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            callback(ArrayList())
                            throw t
                        }
                    })
            } catch (error: Exception) {

            }
        }

    }

    private fun getVideoDataFromResponse(res: String) {

    }

    private val patPlayerResponse =
        Pattern.compile("var ytInitialPlayerResponse\\s*=\\s*(\\{.+?\\})\\s*;")
    private val patSigEncUrl = Pattern.compile("url=(.+?)(\\u0026|$)")
    private val patSignature = Pattern.compile("s=(.+?)(\\u0026|$)")

    fun getVideoIDFromURL(ytUrl: String): String? {
        val patYouTubePageLink =
            Pattern.compile("(http|https)://(www\\.|m.|)youtube\\.com/watch\\?v=(.+?)( |\\z|&)")
        val patYouTubeShortLink =
            Pattern.compile("(http|https)://(www\\.|)youtu.be/(.+?)( |\\z|&)")

        val patYouTubeShortsLink =
            Pattern.compile("(http|https)://(www\\.|m.|)youtube\\.com/shorts/(.+?)( |\\z|&)")

        val shortsFeature = Pattern.compile("(.+?)( |\\z|)\\?feature=share")

        var videoID: String? = null

        var mat: Matcher =
            patYouTubePageLink.matcher(ytUrl)

        if (mat.find()) {
            videoID = mat.group(3)
        } else {
            mat = patYouTubeShortLink.matcher(ytUrl)
            videoID = if (mat.find()) {
                mat.group(3)
            } else {
                mat = patYouTubeShortsLink.matcher(ytUrl)
                if (mat.find()) {
                    val id = mat.group(3)
                    mat = shortsFeature.matcher(id)
                    if (mat.find())
                        mat.group(1)
                    else
                        id
                } else
                    null
            }
        }

        return videoID
    }

    private fun isInternetExist(): Boolean =
        BasicStorage.isNetworkConnected.value == true
}