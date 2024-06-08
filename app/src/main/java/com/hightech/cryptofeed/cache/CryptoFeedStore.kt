package com.hightech.cryptofeed.cache

import kotlinx.coroutines.flow.Flow
import java.util.Date

sealed class RetrieveCachedCryptoFeedResult {
    class Empty: RetrieveCachedCryptoFeedResult()
    data class Found(val cryptoFeed: List<LocalCryptoFeed>, val timestamp: Date): RetrieveCachedCryptoFeedResult()
    data class Failure(val exception: Exception): RetrieveCachedCryptoFeedResult()
}

typealias deleteCacheResult = Exception?
typealias insertResult = Exception?
typealias RetrievalResult = RetrieveCachedCryptoFeedResult

interface CryptoFeedStore {
    fun deleteCache(): Flow<deleteCacheResult>
    fun insert(feeds: List<LocalCryptoFeed>, timestamp: Date): Flow<insertResult>
    fun retrieve(): Flow<RetrievalResult>
}