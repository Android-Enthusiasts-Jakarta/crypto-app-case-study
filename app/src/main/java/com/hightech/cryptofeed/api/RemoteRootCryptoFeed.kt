package com.hightech.cryptofeed.api

import com.squareup.moshi.Json

data class RootCryptoFeedResponse(
    @Json(name = "Data")
    val data: List<CryptoFeedResponse>
)

data class CryptoFeedResponse(
    @Json(name = "CoinInfo")
    val coinInfoResponse: CoinInfoResponse,
    @Json(name = "RAW")
    val rawResponse: RawResponse
)

data class CoinInfoResponse(
    @Json(name = "Id")
    val id: String,
    @Json(name = "Name")
    val name: String,
    @Json(name = "FullName")
    val fullName: String,
    @Json(name = "ImageUrl")
    val imageUrl: String,
)

data class RawResponse(
    @Json(name = "USD")
    val usdResponse: UsdResponse
)

data class UsdResponse(
    @Json(name = "PRICE")
    val price: Double,
    @Json(name = "CHANGEPCTDAY")
    val changePctDay: Float
)

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