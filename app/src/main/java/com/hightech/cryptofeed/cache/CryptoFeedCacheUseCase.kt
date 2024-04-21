package com.hightech.cryptofeed.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface CryptoFeedStore {
    fun deleteCache(): Flow<Exception>
    fun insert(): Flow<Exception>
}

class CryptoFeedCacheUseCase constructor(private val store: CryptoFeedStore) {
    fun save(): Flow<Exception> = flow {
        store.deleteCache().collect { error ->

        }
    }
}