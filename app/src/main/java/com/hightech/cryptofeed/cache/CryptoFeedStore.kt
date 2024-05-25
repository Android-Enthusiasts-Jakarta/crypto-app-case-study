package com.hightech.cryptofeed.cache

import kotlinx.coroutines.flow.Flow
import java.util.Date

sealed class RetrieveCacheCryptoFeedResult {
    class Empty: RetrieveCacheCryptoFeedResult()
    data class Found(val cryptoFeed: List<LocalCryptoFeed>, val timestamp: Date): RetrieveCacheCryptoFeedResult()
    data class Failure(val exception: Exception): RetrieveCacheCryptoFeedResult()
}

typealias deleteCacheResult = Exception?
typealias insertResult = Exception?
typealias RetrievalResult = RetrieveCacheCryptoFeedResult

interface CryptoFeedStore {
    fun deleteCache(): Flow<deleteCacheResult>
    fun insert(feeds: List<LocalCryptoFeed>, timestamp: Date): Flow<insertResult>
    fun retrieve(): Flow<RetrievalResult>
}