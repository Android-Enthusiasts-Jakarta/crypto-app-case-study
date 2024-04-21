package com.hightech.cryptofeed.api

data class RemoteRootCryptoFeed(
    val data: List<RemoteCryptoFeed>
)

data class RemoteCryptoFeed(
    val remoteCoinInfo: RemoteCoinInfo,
    val remoteRaw: RemoteRaw
)

data class RemoteCoinInfo(
    val id: String,
    val name: String,
    val fullName: String,
    val imageUrl: String,
)

data class RemoteRaw(
    val remoteUsd: RemoteUsd
)

data class RemoteUsd(
    val price: Double,
    val changePctDay: Float
)