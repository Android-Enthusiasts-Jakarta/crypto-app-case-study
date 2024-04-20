package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

typealias SaveResult = Exception?

class CacheCryptoFeedUseCase constructor(
    private val store: RoomCryptoFeedStore,
    private val currentDate: Date
) {
    fun save(feeds: List<CryptoFeed>): Flow<SaveResult> = flow {
        store.deleteCache().collect { deleteError ->
            if (deleteError != null) {
                emit(deleteError)
            } else {
                store.insert(feeds, currentDate).collect { insertError ->
                    emit(insertError)
                }
            }
        }
    }
}

class RoomCryptoFeedStore {
    fun deleteCache(): Flow<Exception?> = flow {}

    fun insert(feeds: List<CryptoFeed>, timestamp: Date): Flow<Exception?> = flow {}
}