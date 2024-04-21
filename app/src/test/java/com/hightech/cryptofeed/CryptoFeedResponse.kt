package com.hightech.cryptofeed

import com.hightech.cryptofeed.apiinfra.CoinInfoResponse
import com.hightech.cryptofeed.apiinfra.CryptoFeedResponse
import com.hightech.cryptofeed.apiinfra.RawResponse
import com.hightech.cryptofeed.api.RemoteCoinInfo
import com.hightech.cryptofeed.api.RemoteCryptoFeed
import com.hightech.cryptofeed.api.RemoteRaw
import com.hightech.cryptofeed.api.RemoteUsd
import com.hightech.cryptofeed.apiinfra.UsdResponse

val remoteCryptoFeed = listOf(
    RemoteCryptoFeed(
        RemoteCoinInfo(
            "1",
            "BTC",
            "Bitcoin",
            "imageUrl",
        ),
        RemoteRaw(
            RemoteUsd(
                1.0,
                1F,
            ),
        ),
    ),
    RemoteCryptoFeed(
        RemoteCoinInfo(
            "2",
            "BTC 2",
            "Bitcoin 2",
            "imageUrl"
        ),
        RemoteRaw(
            RemoteUsd(
                2.0,
                2F,
            ),
        ),
    ),
)

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