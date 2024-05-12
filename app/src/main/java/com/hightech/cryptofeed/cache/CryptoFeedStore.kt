package com.hightech.cryptofeed.cache

import kotlinx.coroutines.flow.Flow
import java.util.Date

interface CryptoFeedStore {
    fun deleteCache(): Flow<Exception?>
    fun insert(feeds: List<LocalCryptoFeed>, timestamp: Date): Flow<Exception?>
}