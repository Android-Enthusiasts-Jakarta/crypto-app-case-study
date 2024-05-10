package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

interface CryptoFeedStore {
    fun deleteCache(): Flow<Exception?>
    fun insert(feeds: List<CryptoFeed>, timestamp: Date): Flow<Exception?>
}

class CacheCryptoFeedUseCase constructor(
    private val store: CryptoFeedStore,
    private val currentDate: Date
) {
    fun save(feeds: List<CryptoFeed>): Flow<Exception?> = flow {
        store.deleteCache().collect { deleteError ->
            if (deleteError == null) {
                store.insert(feeds, currentDate).collect { insertError ->
                    emit(insertError)
                }
            } else {
                emit(deleteError)
            }
        }
    }
}