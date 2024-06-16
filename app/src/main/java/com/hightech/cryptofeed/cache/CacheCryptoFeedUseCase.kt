package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CoinInfo
import com.hightech.cryptofeed.domain.CryptoFeed
import com.hightech.cryptofeed.domain.LoadCryptoFeedResult
import com.hightech.cryptofeed.domain.LoadCryptoFeedUseCase
import com.hightech.cryptofeed.domain.Raw
import com.hightech.cryptofeed.domain.Usd
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import java.util.Date

class CryptoFeedCachePolicy constructor(
    private val currentDate: Date,
    private val calendar: Calendar = Calendar.getInstance()
) {
    private val maxCacheAgeInDays: Int = 1

    fun validate(timestamp: Date): Boolean {
        calendar.apply {
            time = timestamp
            add(Calendar.DAY_OF_YEAR, maxCacheAgeInDays)
        }
        val maxCacheAge = calendar.time
        return currentDate.before(maxCacheAge)
    }
}

typealias SaveResult = Exception?
typealias LoadResult = LoadCryptoFeedResult

class CacheCryptoFeedUseCase constructor(
    private val store: CryptoFeedStore,
    private val currentDate: Date,
    private val cachePolicy: CryptoFeedCachePolicy = CryptoFeedCachePolicy(
        currentDate = currentDate
    )
): LoadCryptoFeedUseCase {
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

    override fun load(): Flow<LoadResult> = flow {
        store.retrieve().collect { result ->
            when(result) {
                is RetrieveCachedCryptoFeedResult.Empty -> {
                    emit(LoadCryptoFeedResult.Success(emptyList()))
                }
                is RetrieveCachedCryptoFeedResult.Found -> {
                    if (cachePolicy.validate(result.timestamp)) {
                        emit(LoadCryptoFeedResult.Success(result.cryptoFeed.toModels()))
                    } else {
                        emit(LoadCryptoFeedResult.Success(emptyList()))
                    }
                }
                is RetrieveCachedCryptoFeedResult.Failure -> {
                    emit(LoadCryptoFeedResult.Failure(result.exception))
                }
            }
        }
    }

    suspend fun validateCache() {
        store.retrieve().collect { result ->
            when(result) {
                is RetrieveCachedCryptoFeedResult.Empty -> {}

                is RetrieveCachedCryptoFeedResult.Found -> {
                    if (!cachePolicy.validate(result.timestamp)) {
                        store.deleteCache().collect { _ -> }
                    }
                }

                is RetrieveCachedCryptoFeedResult.Failure -> {
                    store.deleteCache().collect { _ -> }
                }
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


private fun List<LocalCryptoFeed>.toModels(): List<CryptoFeed> {
    return map {
        CryptoFeed(
            coinInfo = CoinInfo(
                id = it.coinInfo.id,
                name = it.coinInfo.name,
                fullName = it.coinInfo.fullName,
                imageUrl = it.coinInfo.imageUrl
            ),
            raw = Raw(
                usd = Usd(
                    price = it.raw.usd.price,
                    changePctDay = it.raw.usd.changePctDay
                )
            )
        )
    }
}