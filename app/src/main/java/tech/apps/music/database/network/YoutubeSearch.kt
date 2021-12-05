package tech.apps.music.database.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import tech.apps.music.util.BasicStorage
import java.io.IOException
import java.net.URLEncoder

const val BASE_URL = "https://www.youtube.com"
const val MAX_LIST_SIZE = 20

interface RetrofitCreateRoom {
    @GET("/results")
    fun ytSearchResult(@Query("search_query") keyword: String): Call<ResponseBody>
}

object YtSearchInstance {

    val ytSearchInstant: RetrofitCreateRoom

    init {

        val okHttpClient = OkHttpClient().newBuilder()
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()

        ytSearchInstant = retrofit.create(RetrofitCreateRoom::class.java)
    }
}

class YoutubeSearch {

    suspend fun searchWithKeywords(
        keyword: String,
        callback: (data: ArrayList<VideoObject>) -> Unit
    ) {
        if (!isInternetExist()) {
            return
        }
        val keywordEncoded = URLEncoder.encode(keyword.trim(), "UTF-8")

        withContext(Dispatchers.IO) {
            try {

                YtSearchInstance.ytSearchInstant.ytSearchResult(keywordEncoded)
                    .enqueue(object : retrofit2.Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {

                            if (!response.isSuccessful) {
                                callback(ArrayList())
                            }
                            val resp = response.body()?.string()

                            if (resp != null) {
                                val list = parseHtml(resp)
                                callback(list)
                            } else {
                                callback(ArrayList())
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            callback(ArrayList())
                            throw t
                        }

                    })

            } catch (error: IOException) {
                print(error)
                callback(ArrayList())
            } catch (err: HttpException) {
                print(err)
                callback(ArrayList())
            }
        }
    }

    private fun parseHtml(response: String): ArrayList<VideoObject> {

        val results: ArrayList<VideoObject> = ArrayList()

        val start = response.indexOf("ytInitialData") + ("ytInitialData").length + 3
        val end = response.indexOf("};", start) + 1

        val jsonStr = response.substring(start, end)

        val data = JSONObject(jsonStr)

        val videos = data.getJSONObject("contents")
            .getJSONObject("twoColumnSearchResultsRenderer")
            .getJSONObject("primaryContents")
            .getJSONObject("sectionListRenderer")
            .getJSONArray("contents")
            .getJSONObject(0)
            .getJSONObject("itemSectionRenderer")
            .getJSONArray("contents")

        val size = videos.length()

        for (i in 0 until (size - 1)) {
            val video = videos.getJSONObject(i)
            val res = VideoObject()

            if (video.optJSONObject("videoRenderer") != null) {
                val video_data = video.getJSONObject("videoRenderer")
                res.videoId = video_data.getString("videoId")
                res.thumbnails = video_data.getJSONObject("thumbnail").getJSONArray("thumbnails")
                    .getJSONObject(0).getString("url")
                res.title = video_data.getJSONObject("title").getJSONArray("runs").getJSONObject(0)
                    .getString("text")
                res.channelName =
                    video_data.getJSONObject("longBylineText").getJSONArray("runs").getJSONObject(0)
                        .getString("text")

                if (video_data.optJSONObject("lengthText") != null) {
                    res.duration = video_data.getJSONObject("lengthText").getString("simpleText")
                    results.add(res)
                }

                if (results.size >= MAX_LIST_SIZE) {
                    return results
                }
            }
        }
        return results
    }

    private fun isInternetExist(): Boolean =
        BasicStorage.isNetworkConnected.value == true

}

data class VideoObject(
    var videoId: String = "",
    var thumbnails: String = "",
    var title: String = "",
    var channelName: String = "",
    var duration: String = "",
    var viewsCount: String = ""
)