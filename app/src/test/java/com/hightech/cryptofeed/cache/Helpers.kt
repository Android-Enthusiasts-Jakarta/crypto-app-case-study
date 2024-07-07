package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CoinInfo
import com.hightech.cryptofeed.domain.CryptoFeed
import com.hightech.cryptofeed.domain.Raw
import com.hightech.cryptofeed.domain.Usd
import java.util.Calendar
import java.util.Date
import java.util.UUID

fun uniqueCryptoFeed(): CryptoFeed {
    return CryptoFeed(
        CoinInfo(
            UUID.randomUUID().toString(),
            "any",
            "any",
            "any-url"
        ),
        Raw(
            Usd(
                1.0,
                1F,
            )
        )
    )
}

fun uniqueItems(): Pair<List<CryptoFeed>, List<LocalCryptoFeed>> {
    val cryptoFeeds = listOf(uniqueCryptoFeed(), uniqueCryptoFeed())
    val localCryptoFeed = cryptoFeeds.map {
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
    return Pair(cryptoFeeds, localCryptoFeed)
}

fun anyException(): Exception {
    return Exception()
}

fun Date.minusCryptoFeedCacheMaxAge(): Date {
    return adding(days = -cryptoFeedCacheMaxAgeInDays)
}

private var cryptoFeedCacheMaxAgeInDays = 1

fun Date.adding(days: Int): Date = Calendar.getInstance().apply {
    time = this@adding
    add(Calendar.DAY_OF_YEAR, days)
}.time

fun Date.adding(seconds: Long): Date {
    val time = this.time + seconds * 1000
    return Date(time)
}