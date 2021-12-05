package tech.apps.music.database.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URLEncoder


interface RetrofitVideoData {
    @GET("/oembed")
    fun ytVideoData(@Query("url") keyword: String): Call<ResponseBody>
}

object OembedVideoData {

    val ytVideoDataInstant: RetrofitVideoData

    init {

        val okHttpClient = OkHttpClient().newBuilder()
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()

        ytVideoDataInstant = retrofit.create(RetrofitVideoData::class.java)
    }
}

class VideoDataFromLink{

    suspend fun getVideoDataWithLink(
        link: String,
        callback: (data: VideoObject?) -> Unit
    ) {
        val keywordEncoded = URLEncoder.encode(link.trim(), "UTF-8")

        withContext(Dispatchers.IO) {
            try {

                OembedVideoData.ytVideoDataInstant.ytVideoData(keywordEncoded)
                    .enqueue(object : retrofit2.Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {

                            if (!response.isSuccessful) {
                                callback(null)
                            }
                            val resp = response.body()?.string()

                            if (resp != null) {
                                callback(parseJson(resp))
                            } else {
                                callback(null)
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            callback(null)
                            throw t
                        }

                    })

            } catch (error: Exception) {
                print(error)
                callback(null)
            } catch (err: JSONException){
                print(err)
                callback(null)
            }
        }
    }

    private fun parseJson(response: String): VideoObject {

        val results = VideoObject()

        val data = JSONObject(response)

        results.thumbnails = data.getString("thumbnail_url")
        results.channelName = data.getString("author_name")
        results.title = data.getString("title")

        return results
    }

}