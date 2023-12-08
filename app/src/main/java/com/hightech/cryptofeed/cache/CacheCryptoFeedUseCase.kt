package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class CacheCryptoFeedUseCase constructor(
    private val store: RoomCryptoFeedStore
) {
    fun save(feeds: List<CryptoFeed>): Flow<Exception> = flow {
        store.deleteCache().collect { result ->
            when(result) {
                is DeleteResult.Success -> {
                    emitAll(store.insert(feeds))
                }
                is DeleteResult.Failure -> {
                    if (result.exception is DeletionErrorException) {
                        emit(DeletionError())
                    }
                }
            }
        }
    }
}

class DeletionError : Exception()

sealed interface DeleteResult {
    class Success: DeleteResult
    data class Failure(val exception: Exception): DeleteResult
}

class RoomCryptoFeedStore {
    fun deleteCache(): Flow<DeleteResult> = flow {}

    fun insert(feeds: List<CryptoFeed>): Flow<Exception> = flow {
        println("test")
    }
}

class DeletionErrorException : Exception()