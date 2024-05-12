package com.hightech.cryptofeed.cache

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