package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

class CacheCryptoFeedUseCase constructor(
    private val store: RoomCryptoFeedStore,
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

class RoomCryptoFeedStore {
    fun deleteCache(): Flow<Exception?> = flow {}

    fun insert(feeds: List<CryptoFeed>, timestamp: Date): Flow<Exception?> = flow {}
}