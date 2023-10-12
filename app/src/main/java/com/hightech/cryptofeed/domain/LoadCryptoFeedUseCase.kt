package com.hightech.cryptofeed.domain

import kotlinx.coroutines.flow.Flow
import java.lang.Exception

sealed class LoadCryptoFeedResult {
    data class Success(val cryptoFeed: List<CryptoFeed>): LoadCryptoFeedResult()
    data class Error(val exception: Exception): LoadCryptoFeedResult()
}

interface LoadCryptoFeedUseCase {
    fun load(): Flow<LoadCryptoFeedResult>
}