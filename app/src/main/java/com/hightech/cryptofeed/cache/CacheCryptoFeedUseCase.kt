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

class RoomCryptoFeedStore {
    fun deleteCache(): Flow<Exception?> = flow {}

    fun insert(feeds: List<LocalCryptoFeed>, timestamp: Date): Flow<Exception?> = flow {}
}

data class LocalCryptoFeed(
    val coinInfo: LocalCoinInfo,
    val raw: LocalRaw
)

data class LocalCoinInfo(
    val id: String,
    val name: String,
    val fullName: String,
    val imageUrl: String
)

data class LocalRaw(
    val usd: LocalUsd
)

data class LocalUsd(
    val price: Double,
    val changePctDay: Float
)