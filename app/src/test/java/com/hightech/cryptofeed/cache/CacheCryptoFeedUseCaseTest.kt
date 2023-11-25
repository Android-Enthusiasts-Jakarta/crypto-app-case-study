package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CoinInfo
import com.hightech.cryptofeed.domain.CryptoFeed
import com.hightech.cryptofeed.domain.Raw
import com.hightech.cryptofeed.domain.Usd
import io.mockk.confirmVerified
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.UUID

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

class CacheCryptoFeedUseCaseTest {
    private val store = spyk<RoomCryptoFeedStore>()
    private lateinit var sut: CacheCryptoFeedUseCase

    @Before
    fun setUp() {
        sut = CacheCryptoFeedUseCase(store)
    }

    @Test
    fun testInitDoesNotDeleteCacheUponCreation() = runBlocking {
        verify(exactly = 0) {
            store.deleteCache()
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveRequestsCacheDeletion() = runBlocking {
        val feeds = listOf(uniqueCryptoFeed(), uniqueCryptoFeed())

        sut.save(feeds)

        verify(exactly = 1) {
            store.deleteCache()
        }

        confirmVerified(store)
    }

    private fun uniqueCryptoFeed(): CryptoFeed {
        return CryptoFeed(
            CoinInfo(
                UUID.randomUUID().toString(),
                "any",
                "any",
                "any-url"
            ),
            Raw(
                Usd(
                    1.0,
                    1F,
                )
            )
        )
    }
 }