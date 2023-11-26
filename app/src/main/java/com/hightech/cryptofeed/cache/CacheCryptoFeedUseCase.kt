package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CacheCryptoFeedUseCase constructor(
    private val store: RoomCryptoFeedStore
) {
    fun save(feeds: List<CryptoFeed>): Flow<Exception> = flow {
        store.deleteCache().collect { error ->

        }
    }
}

class RoomCryptoFeedStore {
    fun deleteCache(): Flow<Exception> = flow {
    }
}