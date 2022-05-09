package tech.apps.music.database.network

import android.content.Context
import okhttp3.ResponseBody
import retrofit2.Response


class ApiClient(
    private val context: Context
) {

    suspend fun searchWithKeywords(keywordEncoded: String): SimpleResponse<ResponseBody> {
        return safeApiCall {
            RetrofitApiClient(context).youtubeEndpointService
                .ytSearchResult(keywordEncoded)
        }
    }

    suspend fun getVideoData(url: String): SimpleResponse<ResponseBody> {
        return safeApiCall {
            RetrofitApiClient(context).youtubeEndpointService
                .ytGetVideo(url)
        }
    }

    suspend fun searchSuggestionWithKeywords(keyword: String): SimpleResponse<ResponseBody> {
        return safeApiCall { RetrofitApiClient(context).youtubeEndpointSuggestion
            .searchSuggestion(keyword = keyword)
        }
    }


    private inline fun <T> safeApiCall(apiCall: () -> Response<T>): SimpleResponse<T> {
        return try {
            SimpleResponse.success(apiCall.invoke())
        } catch (e: Exception) {
            SimpleResponse.failure(e)
        }
    }
}