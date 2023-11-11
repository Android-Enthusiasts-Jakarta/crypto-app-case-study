package com.hightech.cryptofeed.api

interface CryptoFeedService {
    suspend fun get(): RemoteRootCryptoFeed
}