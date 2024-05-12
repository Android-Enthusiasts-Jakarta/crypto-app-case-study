package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

typealias SaveResult = Exception?

class CacheCryptoFeedUseCase constructor(
    private val store: CryptoFeedStore,
    private val currentDate: Date
) {
    fun save(feeds: List<CryptoFeed>): Flow<SaveResult> = flow {
        store.deleteCache().collect { deleteError ->
            if (deleteError != null) {
                emit(deleteError)
            } else {
                store.insert(feeds.toLocal(), currentDate).collect { insertError ->
                    emit(insertError)
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