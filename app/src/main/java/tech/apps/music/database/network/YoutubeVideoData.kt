package tech.apps.music.database.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tech.apps.music.NoConnectivityException
import java.util.regex.Matcher
import java.util.regex.Pattern


class YoutubeVideoData {

    suspend fun getVideoData(
        url: String,
        context: Context,
        callback: (Pair<String, String>?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                RetrofitApiClient().getRetrofitClient(context)
                    ?.create(RetrofitSearch::class.java)
                    ?.ytGetVideo(url)
                    ?.enqueue(object : Callback<ResponseBody> {
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
                            if(t == NoConnectivityException()) {
                                Log.i("YoutubeVideoData","Network issue")
                            }
                            throw t
                        }

                    })

            } catch (error: Exception) {
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
            videoID = if (mat.find()) {
                mat.group(3)
            } else {
                ytUrl
            }
        }
        return videoID
    }
}