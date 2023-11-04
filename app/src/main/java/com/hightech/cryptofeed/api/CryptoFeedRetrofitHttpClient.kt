package com.hightech.cryptofeed.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CryptoFeedRetrofitHttpClient(
    private val service: CryptoFeedService
) {
    fun get(): Flow<HttpClientResult> = flow {
        try {
            service.get()
        } catch (exception: Exception) {
            emit(HttpClientResult.Failure(ConnectivityException()))
        }
    }

}