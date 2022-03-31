package tech.apps.music.database.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONTokener
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import tech.apps.music.util.BasicStorage
import java.io.IOException
import java.net.URLEncoder

const val BASE_URL_SUGGESTION_API =
    "https://suggestqueries.google.com"

interface RetrofitSuggestion {
    @GET("/complete/search")
    fun searchSuggestion(@Query("client") firefox: String= "firefox",@Query("ds") yt :String = "yt",@Query("q") keyword: String): Call<ResponseBody>
}

object SearchSuggestionApi {

    val searchInstant: RetrofitSuggestion

    init {

        val okHttpClient = OkHttpClient().newBuilder()
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_SUGGESTION_API)
            .client(okHttpClient)
            .build()

        searchInstant = retrofit.create(RetrofitSuggestion::class.java)
    }
}

class SearchSuggestion {

    suspend fun searchWithKeywords(
        keyword: String,
        callback: (data: ArrayList<String>) -> Unit
    ) {
        if (!isInternetExist()) {
            callback(ArrayList())
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val keywordEncoded = URLEncoder.encode(keyword.trim(), "UTF-8")

                SearchSuggestionApi.searchInstant.searchSuggestion(keyword = keywordEncoded)
                    .enqueue(object : retrofit2.Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (!response.isSuccessful) {
                                callback(ArrayList())
                                return
                            }
                            val array = (JSONTokener(response.body()?.string()).nextValue()) as JSONArray

                            val list = array.getJSONArray(1)
                            if (list != null) {
                                val returnList = ArrayList<String>()
                                for ( i in 0 until list.length()){
                                    returnList.add(list.get(i).toString())
                                }
                                    callback(returnList)
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

    private fun isInternetExist(): Boolean =
        BasicStorage.isNetworkConnected.value == true
}