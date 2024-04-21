package com.hightech.cryptofeed.cache

interface CryptoFeedStore {
    fun deleteCache()
}

class CryptoFeedCacheUseCase constructor(private val store: CryptoFeedStore) {
    fun save() {
        store.deleteCache()
    }
}