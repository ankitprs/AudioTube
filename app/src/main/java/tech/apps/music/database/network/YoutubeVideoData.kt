package tech.apps.music.database.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import tech.apps.music.util.BasicStorage
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern


class YoutubeVideoData {

    suspend fun getVideoData(
        url: String,
        callback: (Pair<String, String>?) -> Unit
    ) {
//        if (!isInternetExist()) {
//            Log.i("YoutubeVideoData","Return From Network")
//            callback(null)
//            return
//        }

        withContext(Dispatchers.IO) {
            try {

                YoutubeRetrofitInstance.ytSearchInstant.ytGetVideo(url)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {

                            if (!response.isSuccessful) {
                                callback(null)
                                Log.i("YoutubeVideoData","Return From isSuccessful")

                                return
                            }
                            val resp: String? = response.body()?.string()

                            if (resp != null) {
                                callback(
                                    Pair(
                                        JSONObject(resp).getString("title"),
                                        JSONObject(resp).getString("author_name")
                                    )
                                )

                            }

                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            callback(null)
                            Log.i("YoutubeVideoData","Return From onFailure")
                            throw t
                        }

                    })

            } catch (error: IOException) {
                callback(null)
                Log.i("YoutubeVideoData","Return From Exceptions")

            } catch (err: HttpException) {
                callback(null)
                Log.i("YoutubeVideoData","Return From Exceptions")
            }
        }
    }

    fun getVideoIdFromUrl(ytUrl: String): String? {
        val patYouTubePageLink =
            Pattern.compile("(http|https)://(www\\.|m.|)youtube\\.com/watch\\?v=(.+?)( |\\z|&)")
        val patYouTubeShortLink =
            Pattern.compile("(http|https)://(www\\.|)youtu.be/(.+?)( |\\z|&)")
        var videoID: String? = null
        var mat: Matcher = patYouTubePageLink.matcher(ytUrl)
        if (mat.find()) {
            videoID = mat.group(3)
        } else {
            mat = patYouTubeShortLink.matcher(ytUrl)
            if (mat.find()) {
                videoID = mat.group(3)
            } else {
                videoID = ytUrl
            }
        }
        return videoID
    }

    private fun isInternetExist(): Boolean =
        BasicStorage.isNetworkConnected.value == true

}