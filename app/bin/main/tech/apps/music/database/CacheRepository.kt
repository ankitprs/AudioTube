package tech.apps.music.database

import androidx.room.withTransaction
import kotlinx.coroutines.delay
import tech.apps.music.database.network.YoutubeRepository
import tech.apps.music.database.offline.CacheDatabase
import tech.apps.music.util.getCacheTimeDuration
import tech.apps.music.util.networkBoundResource
import javax.inject.Inject

class CacheRepository @Inject constructor(
    private val api: YoutubeRepository,
    private val db: CacheDatabase
) {
    private val cacheDao = db.cacheDao()

    fun getListOfSongWithKeyword(query: String) = networkBoundResource(
        query = {
            println("called = query")
            cacheDao.getListOfQuery(query)
        },
        fetch = {
            println("called = fetch")
            api.searchWithKeywords(query)
        },
        saveFetchResult = {
            println("called = save")
            db.withTransaction {
                delay(0)
                cacheDao.deleteListByQuery(query)
                cacheDao.insertSongList(it)
            }
        },
        shouldFetch = {
            if (it.isNotEmpty()) {
                getCacheTimeDuration(it[0].time)
            } else {
                true
            }
        }
    )

    fun getListOfSongTending() = networkBoundResource(
        query = {
            cacheDao.getListOfQuery("")
        },
        fetch = {
            delay(0)
            api.trendingMusicNow()
        },
        saveFetchResult = {
            db.withTransaction {
                cacheDao.deleteListByQuery("")
                cacheDao.insertSongList(it)
            }
        },
        shouldFetch = {
            if (it.isNotEmpty()) {
                getCacheTimeDuration(it[0].time)
            } else {
                true
            }
        }
    )

    suspend fun getVideoUri(url: String): Pair<String, String>? =
        api.getVideoData(url)

    fun getVideoIdFromUrl(ytUrl: String): String? =
        api.getVideoIdFromUrl(ytUrl)

    suspend fun searchSuggestionWithKeywords(
        keyword: String
    ): ArrayList<String> {
        return api.searchSuggestionWithKeywords(keyword)
    }
}