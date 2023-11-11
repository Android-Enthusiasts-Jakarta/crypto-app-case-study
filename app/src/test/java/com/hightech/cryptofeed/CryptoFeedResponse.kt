package com.hightech.cryptofeed

import com.hightech.cryptofeed.api.RemoteCoinInfo
import com.hightech.cryptofeed.api.RemoteCryptoFeedItem
import com.hightech.cryptofeed.api.RemoteDisplay
import com.hightech.cryptofeed.api.RemoteUsd

val cryptoFeedResponse = listOf(
    RemoteCryptoFeedItem(
        RemoteCoinInfo(
            "1",
            "BTC",
            "Bitcoin",
            "imageUrl",
        ),
        RemoteDisplay(
            RemoteUsd(
                1.0,
                1F,
            ),
        ),
    ),
    RemoteCryptoFeedItem(
        RemoteCoinInfo(
            "2",
            "BTC 2",
            "Bitcoin 2",
            "imageUrl"
        ),
        RemoteDisplay(
            RemoteUsd(
                2.0,
                2F,
            ),
        ),
    ),
)