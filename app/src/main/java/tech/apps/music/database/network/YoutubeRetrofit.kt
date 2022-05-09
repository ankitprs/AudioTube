package tech.apps.music.database.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

const val BASE_URL = "https://www.youtube.com"
const val BASE_URL_SUGGESTION_API = "https://suggestqueries.google.com"

class RetrofitApiClient(context: Context) {

    private var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(getLoggingHttpClient(context))
        .build()

    private var retrofitSuggestion: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_SUGGESTION_API)
        .client(getLoggingHttpClient(context))
        .build()

    val youtubeEndpointService: YoutubeEndpoint by lazy {
        retrofit.create(YoutubeEndpoint::class.java)
    }

    val youtubeEndpointSuggestion: YoutubeEndpoint by lazy {
        retrofitSuggestion.create(YoutubeEndpoint::class.java)
    }

    val apiClient = ApiClient(context)

    private fun getLoggingHttpClient(mContext: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        })

        builder.addInterceptor(
            ChuckerInterceptor.Builder(mContext)
                .collector(ChuckerCollector(mContext))
                .maxContentLength(250000L)
                .redactHeaders(emptySet())
                .alwaysReadResponseBody(false)
                .build()
        )

        return builder.build()
    }
}