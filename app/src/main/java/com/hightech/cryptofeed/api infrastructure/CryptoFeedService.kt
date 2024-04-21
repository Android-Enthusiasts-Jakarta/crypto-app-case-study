package com.hightech.cryptofeed.`api infrastructure`

interface CryptoFeedService {
    suspend fun get(): RootCryptoFeedResponse
}