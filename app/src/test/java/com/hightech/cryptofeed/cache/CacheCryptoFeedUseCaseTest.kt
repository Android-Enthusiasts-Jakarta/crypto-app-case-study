package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.domain.CoinInfo
import com.hightech.cryptofeed.domain.CryptoFeed
import com.hightech.cryptofeed.domain.Raw
import com.hightech.cryptofeed.domain.Usd
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.UUID

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

    @Test
    fun testSaveDoesNotRequestsCacheInsertionOnDeletionError() = runBlocking {
        val feeds = listOf(uniqueCryptoFeed(), uniqueCryptoFeed())

        every {
            store.deleteCache()
        } returns flowOf(Exception())

        sut.save(feeds)

        verify(exactly = 0) {
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