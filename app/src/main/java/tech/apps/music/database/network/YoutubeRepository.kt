package tech.apps.music.database.network

import android.annotation.SuppressLint
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import tech.apps.music.model.SongModelForList
import java.util.regex.Matcher
import java.util.regex.Pattern

class YoutubeRepository {

    private val MAX_LIST_SIZE = 20

    suspend fun searchWithKeywords(
        keyword: String,
        context: Context
    ): ArrayList<SongModelForList> {

        val response = RetrofitApiClient(context).apiClient.searchWithKeywords(keyword)

        return if (!response.isSuccessful) {
            ArrayList()
        } else {
            val resp: String = response.body.string()

            run {
                val list = parseHtml(resp)
                list
            }
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun parseHtml(response: String): ArrayList<SongModelForList> {

        val results: ArrayList<SongModelForList> = ArrayList()

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
            val res = SongModelForList()

            if (video.optJSONObject("videoRenderer") != null) {
                val video_data = video.getJSONObject("videoRenderer")
                res.videoId = video_data.getString("videoId")
                res.title = video_data.getJSONObject("title").getJSONArray("runs").getJSONObject(0)
                    .getString("text")
                res.ChannelName =
                    video_data.getJSONObject("longBylineText").getJSONArray("runs").getJSONObject(0)
                        .getString("text")

                if (video_data.optJSONObject("lengthText") != null) {
                    res.durationText =
                        video_data.getJSONObject("lengthText").getString("simpleText")
                } else {
                    res.duration = -1L
                }
                res.time = 1L
                results.add(res)
                if (results.size >= MAX_LIST_SIZE) {
                    return results
                }
            }
        }
        return results
    }

    suspend fun getVideoData(
        url: String,
        context: Context,
    ): Pair<String, String>? {
        val response = RetrofitApiClient(context).apiClient.getVideoData(url)

        return if (!response.isSuccessful) {
            null
        } else {
            val resp: String = response.body.string()

            Pair(
                JSONObject(resp).getString("title"),
                JSONObject(resp).getString("author_name")
            )
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

    suspend fun searchSuggestionWithKeywords(
        keyword: String,
        context: Context
    ): ArrayList<String> {

        val response = RetrofitApiClient(context).apiClient.searchSuggestionWithKeywords(keyword)

        return if (!response.isSuccessful) {
            ArrayList()

        } else {
            val array = (JSONTokener(response.body.string()).nextValue()) as JSONArray

            val list = array.getJSONArray(1)

            if (list != null) {
                val returnList = ArrayList<String>()
                for (i in 0 until list.length()) {
                    returnList.add(list.get(i).toString())
                }
                returnList
            } else {
                ArrayList()
            }
        }

    }
}