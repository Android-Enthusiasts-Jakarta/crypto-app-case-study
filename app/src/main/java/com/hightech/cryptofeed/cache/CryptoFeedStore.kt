package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow
import java.util.Date

sealed class RetrieveCacheCryptoFeedResult {
    class Empty: RetrieveCacheCryptoFeedResult()
    data class Found(val cryptoFeed: List<CryptoFeed>, val timestamp: Date): RetrieveCacheCryptoFeedResult()
    data class Failure(val exception: Exception): RetrieveCacheCryptoFeedResult()
}

typealias RetrievalResult = RetrieveCacheCryptoFeedResult

interface CryptoFeedStore {
    fun deleteCache(): Flow<Exception?>
    fun insert(feeds: List<LocalCryptoFeed>, timestamp: Date): Flow<Exception?>
    fun retrieve(): Flow<RetrievalResult>
}