package com.hightech.cryptofeed.apiinfra

val cryptoFeedResponse = listOf(
    CryptoFeedResponse(
        CoinInfoResponse(
            "1",
            "BTC",
            "Bitcoin",
            "imageUrl",
        ),
        RawResponse(
            UsdResponse(
                1.0,
                1F,
            ),
        ),
    ),
    CryptoFeedResponse(
        CoinInfoResponse(
            "2",
            "BTC 2",
            "Bitcoin 2",
            "imageUrl"
        ),
        RawResponse(
            UsdResponse(
                2.0,
                2F,
            ),
        ),
    ),
)