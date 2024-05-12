package com.hightech.cryptofeed

import com.hightech.cryptofeed.domain.CoinInfo
import com.hightech.cryptofeed.domain.CryptoFeed
import com.hightech.cryptofeed.domain.Raw
import com.hightech.cryptofeed.domain.Usd

val cryptoFeed = listOf(
    CryptoFeed(
        CoinInfo(
            "1",
            "BTC",
            "Bitcoin",
            "imageUrl"
        ),
        Raw(
            Usd(
                1.0,
                1F,
            ),
        ),
    ),
    CryptoFeed(
        CoinInfo(
            "2",
            "BTC 2",
            "Bitcoin 2",
            "imageUrl"
        ),
        Raw(
            Usd(
                2.0,
                2F,
            ),
        ),
    ),
)