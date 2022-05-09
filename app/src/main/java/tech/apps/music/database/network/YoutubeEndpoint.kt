package tech.apps.music.database.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeEndpoint {
    @GET("/results")
    suspend fun ytSearchResult(@Query("search_query") keyword: String): Response<ResponseBody>

    @GET("/oembed")
    suspend fun ytGetVideo(@Query("url") youtubeUrl: String): Response<ResponseBody>

    @GET("/complete/search")
    suspend fun searchSuggestion(
        @Query("client") firefox: String = "firefox",
        @Query("ds") yt: String = "yt",
        @Query("q") keyword: String
    ): Response<ResponseBody>
}