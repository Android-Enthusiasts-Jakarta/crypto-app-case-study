package com.hightech.cryptofeed.apiinfra

interface CryptoFeedService {
    suspend fun get(): RootCryptoFeedResponse
}