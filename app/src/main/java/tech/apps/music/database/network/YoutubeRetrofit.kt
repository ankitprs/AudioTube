package tech.apps.music.database.network

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://www.youtube.com"

interface RetrofitSearch {
    @GET("/results")
    fun ytSearchResult(@Query("search_query") keyword: String): Call<ResponseBody>

    @GET("/oembed")
    fun ytGetVideo(@Query("url") youtubeUrl: String) : Call<ResponseBody>
}

object YoutubeRetrofitInstance {

    val ytSearchInstant: RetrofitSearch

    init {

        val okHttpClient = OkHttpClient().newBuilder()
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()

        ytSearchInstant = retrofit.create(RetrofitSearch::class.java)
    }
}