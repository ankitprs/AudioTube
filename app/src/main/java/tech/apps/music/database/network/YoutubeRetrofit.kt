package tech.apps.music.database.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import tech.apps.music.NetworkConnectionInterceptor

const val BASE_URL = "https://www.youtube.com"

interface RetrofitSearch {
    @GET("/results")
    fun ytSearchResult(@Query("search_query") keyword: String): Call<ResponseBody>

    @GET("/oembed")
    fun ytGetVideo(@Query("url") youtubeUrl: String) : Call<ResponseBody>
}
class RetrofitApiClient {
    private var retrofit: Retrofit? = null

    fun getRetrofitClient(mContext: Context): Retrofit? {
        if (retrofit == null) {
            val oktHttpClient = OkHttpClient.Builder()
                .addInterceptor(NetworkConnectionInterceptor(mContext))
//            oktHttpClient.addInterceptor(logging)
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(oktHttpClient.build())
                .build()
        }
        return retrofit
    }
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