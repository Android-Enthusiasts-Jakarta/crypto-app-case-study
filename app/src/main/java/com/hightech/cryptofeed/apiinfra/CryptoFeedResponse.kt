package com.hightech.cryptofeed.apiinfra

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