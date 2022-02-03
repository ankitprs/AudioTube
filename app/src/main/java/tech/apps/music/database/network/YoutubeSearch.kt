package tech.apps.music.database.network

import android.annotation.SuppressLint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import tech.apps.music.model.SongModelForList
import tech.apps.music.util.BasicStorage
import java.io.IOException
import java.net.URLEncoder


class YoutubeSearch {

    suspend fun searchWithKeywords(
        keyword: String,
        callback: (data: ArrayList<SongModelForList>) -> Unit
    ) {
        if (!isInternetExist()) {
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val keywordEncoded = URLEncoder.encode(keyword.trim(), "UTF-8")

                YoutubeRetrofitInstance.ytSearchInstant.ytSearchResult(keyword = keywordEncoded)
                    .enqueue(object : retrofit2.Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {

                            if (response.isSuccessful) {
                                val responseString: String? = response.body()?.string()
                                val responseItemJsonArray: JSONArray? =
                                    JSONObject(responseString ?: "{}").optJSONArray("items")

                                if (responseItemJsonArray != null) {
                                    val list = parseHtml(responseItemJsonArray)
                                    callback(list)
                                } else {
                                    callback(ArrayList())
                                }

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

    @SuppressLint("SimpleDateFormat")
    private fun parseHtml(responseArray: JSONArray): ArrayList<SongModelForList> {

        val results: ArrayList<SongModelForList> = ArrayList()

        for (i in 0 until responseArray.length()) {
            val videoData = responseArray.getJSONObject(i)
            val snippets = videoData.getJSONObject("snippet")


            val res = SongModelForList()
            res.videoId = videoData.getJSONObject("id").getString("videoId")
            res.title = snippets.getString("title")
            res.ChannelName = snippets.getString("channelTitle")

//          res.durationText = snippets.getString("duration")
            res.time = 1L
            results.add(res)

        }
        return results
    }

    private fun isInternetExist(): Boolean =
        BasicStorage.isNetworkConnected.value == true

}