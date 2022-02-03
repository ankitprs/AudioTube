package tech.apps.music.database.network

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL_YOUTUBE_SEARCH = "https://www.googleapis.com"
const val API_KEY = "AIzaSyAa1_rnr-wBbiF_gm2RTDzmZ6rk0JGjoyM"
const val MAX_LIST_SIZE = "25"

interface RetrofitSearch {
    @GET("/youtube/v3/search")
    fun ytSearchResult(
        @Query("part") part: String = "snippet",
        @Query("q") keyword: String,
        @Query("eventType") eventType: String = "completed",
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: String = MAX_LIST_SIZE,
        @Query("key") key: String = API_KEY
    ): Call<ResponseBody>
}

object YoutubeRetrofitInstance {

    val ytSearchInstant: RetrofitSearch

    init {

        val okHttpClient = OkHttpClient().newBuilder()
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_YOUTUBE_SEARCH)
            .client(okHttpClient)
            .build()

        ytSearchInstant = retrofit.create(RetrofitSearch::class.java)
    }
}