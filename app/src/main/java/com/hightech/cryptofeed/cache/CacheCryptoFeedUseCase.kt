package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CryptoFeed

class CacheCryptoFeedUseCase constructor(
    private val store: RoomCryptoFeedStore
) {
    fun save(feeds: List<CryptoFeed>) {
        store.deleteCache()
    }
}

class RoomCryptoFeedStore {
    fun deleteCache() {

    }
}