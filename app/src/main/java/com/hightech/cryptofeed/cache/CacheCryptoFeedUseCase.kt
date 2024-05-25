package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CryptoFeed
import com.hightech.cryptofeed.domain.LoadCryptoFeedResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

typealias SaveResult = Exception?
typealias LoadResult = LoadCryptoFeedResult

class CacheCryptoFeedUseCase constructor(
    private val store: CryptoFeedStore,
    private val currentDate: Date
) {
    fun save(feed: List<CryptoFeed>): Flow<SaveResult> = flow {
        store.deleteCache().collect { deleteError ->
            if (deleteError != null) {
                emit(deleteError)
            } else {
                store.insert(feed.toLocal(), currentDate).collect { insertError ->
                    emit(insertError)
                }
            }
        }
    }

    fun load(): Flow<LoadResult> = flow {
        store.retrieve().collect { result ->
            when(result) {
                is RetrieveCacheCryptoFeedResult.Empty -> {
                    emit(LoadCryptoFeedResult.Success(emptyList()))
                }
                is RetrieveCacheCryptoFeedResult.Found -> {
                    emit(LoadCryptoFeedResult.Success(result.cryptoFeed))
                }
                is RetrieveCacheCryptoFeedResult.Failure -> {
                    emit(LoadCryptoFeedResult.Failure(result.exception))
                }
            }
        }
    }

    private fun List<CryptoFeed>.toLocal(): List<LocalCryptoFeed> {
        return map {
            LocalCryptoFeed(
                coinInfo = LocalCoinInfo(
                    id = it.coinInfo.id,
                    name = it.coinInfo.name,
                    fullName = it.coinInfo.fullName,
                    imageUrl = it.coinInfo.imageUrl
                ),
                raw = LocalRaw(
                    usd = LocalUsd(
                        price = it.raw.usd.price,
                        changePctDay = it.raw.usd.changePctDay
                    )
                )
            )
        }
    }
}