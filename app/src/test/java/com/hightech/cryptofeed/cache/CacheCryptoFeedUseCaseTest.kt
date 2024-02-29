package com.hightech.cryptofeed.cache

import app.cash.turbine.test
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.UUID

class CacheCryptoFeedUseCaseTest {
    private val store = spyk<RoomCryptoFeedStore>()
    private lateinit var sut: CacheCryptoFeedUseCase

    private val timestamp = Date()

    @Before
    fun setUp() {
        sut = CacheCryptoFeedUseCase(store, timestamp)
    }

    @Test
    fun testInitDoesNotDeleteCacheUponCreation() {
        verify(exactly = 0) {
            store.deleteCache()
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveRequestsCacheDeletion() = runBlocking {
        val feeds = listOf(uniqueCryptoFeed(), uniqueCryptoFeed())

        every {
            store.deleteCache()
        } returns flowOf()

        sut.save(feeds).test {
            awaitComplete()
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveDoesNotRequestCacheInsertionOnDeletionError() = runBlocking {
        val feed = listOf(uniqueCryptoFeed(), uniqueCryptoFeed())

        every {
            store.deleteCache()
        } returns flowOf(Exception())

        sut.save(feed).test {
            skipItems(1)
            awaitComplete()
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        verify(exactly = 0) {
            store.insert(feed, timestamp)
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveRequestsNewCacheInsertionWithTimestampOnSuccessfulDeletion() = runBlocking {
        val feeds = listOf(uniqueCryptoFeed(), uniqueCryptoFeed())

        every {
            store.deleteCache()
        } returns flowOf(null)

        sut.save(feeds).test {
            awaitComplete()
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        verify(exactly = 1) {
            store.insert(feeds, timestamp)
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveFailsOnDeletionError() = runBlocking {
        val feeds = listOf(uniqueCryptoFeed(), uniqueCryptoFeed())

        every {
            store.deleteCache()
        } returns flowOf(Exception())

        sut.save(feeds).test {
            assertNotNull(awaitItem()!!::class.java)
            awaitComplete()
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        verify(exactly = 0) {
            store.insert(feeds, timestamp)
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveFailsOnInsertionError() = runBlocking {
        val feeds = listOf(uniqueCryptoFeed(), uniqueCryptoFeed())

        every {
            store.deleteCache()
        } returns flowOf(null)

        every {
            store.insert(feeds, timestamp)
        } returns flowOf(Exception())

        sut.save(feeds).test {
            assertEquals(Exception::class.java, awaitItem()!!::class.java)
            awaitComplete()
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        verify(exactly = 1) {
            store.insert(feeds, timestamp)
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveSucceedsOnSuccessfulCacheInsertion() = runBlocking {
        val feeds = listOf(uniqueCryptoFeed(), uniqueCryptoFeed())

        every {
            store.deleteCache()
        } returns flowOf(null)

        every {
            store.insert(feeds, timestamp)
        } returns flowOf(null)

        sut.save(feeds).test {
            assertNull(awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        verify(exactly = 1) {
            store.insert(feeds, timestamp)
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