package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.util.Date

class CacheCryptoFeedUseCase constructor(
    private val store: RoomCryptoFeedStore,
    private val currentDate: Date
) {
    fun save(feeds: List<CryptoFeed>): Flow<Exception> = flow {
        store.deleteCache().collect { result ->
            when(result) {
                is DeleteResult.Success -> {
                    store.insert(feeds, currentDate).collect { error ->
                        emit(error)
                    }
                }
                is DeleteResult.Failure -> {
                    emit(DeletionError())
                }
            }
        }
    }
}

class DeletionError : Exception()

sealed interface DeleteResult {
    class Success: DeleteResult
    class Failure: DeleteResult
}

class RoomCryptoFeedStore {
    fun deleteCache(): Flow<DeleteResult> = flow {}

    fun insert(feeds: List<CryptoFeed>, timestamp: Date): Flow<Exception> = flow {}
}