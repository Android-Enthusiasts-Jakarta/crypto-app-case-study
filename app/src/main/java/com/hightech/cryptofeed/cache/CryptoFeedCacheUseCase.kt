package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface CryptoFeedStore {
    fun deleteCache(): Flow<Exception?>
    fun insert(feeds: List<CryptoFeed>): Flow<Exception>
}

class CryptoFeedCacheUseCase constructor(private val store: CryptoFeedStore) {
    fun save(feeds: List<CryptoFeed>): Flow<Exception> = flow {
        store.deleteCache().collect { error ->
            if (error == null) {
                store.insert(feeds)
            }
        }
    }
}