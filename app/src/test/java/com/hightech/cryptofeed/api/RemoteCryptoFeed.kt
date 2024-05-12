package com.hightech.cryptofeed.api

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