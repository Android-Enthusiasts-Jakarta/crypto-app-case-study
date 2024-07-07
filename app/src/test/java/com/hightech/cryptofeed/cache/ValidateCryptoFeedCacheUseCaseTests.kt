package com.hightech.cryptofeed.cache

import app.cash.turbine.test
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.Date

class ValidateCryptoFeedCacheUseCaseTests {
    private val store = spyk<CryptoFeedStore>()
    private lateinit var sut: CacheCryptoFeedUseCase

    @Before
    fun setUp() {
        sut = CacheCryptoFeedUseCase(store = store, { Date() })
    }

    @Test
    fun testInitDoesNotLoadCacheUponCreation() = runBlocking {
        verify(exactly = 0) {
            store.retrieve()
        }

        confirmVerified(store)
    }

    @Test
    fun testValidateCacheDeletesCacheOnRetrievalError() {
        val retrievalError= anyException()

        expect(sut = sut, action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCachedCryptoFeedResult.Failure(retrievalError))

            every {
                store.deleteCache()
            } returns flowOf()
        })
    }

    @Test
    fun testValidateCacheDoesNotDeletesCacheOnEmptyCache() = runBlocking {
        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Empty())

        sut.validateCache()

        verify(exactly = 1) {
            store.retrieve()
        }

        confirmVerified(store)
    }

    @Test
    fun testValidateCacheDoesNotDeletesCacheOnNonExpiredCache() = runBlocking {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val nonExpiredCacheTimestamp = fixedCurrentDate.minusCryptoFeedCacheMaxAge().adding(seconds = 1)

        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, nonExpiredCacheTimestamp))

        sut.load().test {
            skipItems(1)
            awaitComplete()
        }

        verify(exactly = 1) {
            store.retrieve()
        }

        confirmVerified(store)
    }

    @Test
    fun testValidateCacheDeletesCacheOnCacheExpiration() = runBlocking {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val expirationTimestamp = fixedCurrentDate.minusCryptoFeedCacheMaxAge()

        expect(sut = sut, action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, expirationTimestamp))

            every {
                store.deleteCache()
            } returns flowOf()
        })
    }

    @Test
    fun testValidateCacheDeletesCacheOnExpiredCache() = runBlocking {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val expiredCacheTimestamp = fixedCurrentDate.minusCryptoFeedCacheMaxAge().adding(seconds = -1)

        expect(sut = sut, action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, expiredCacheTimestamp))

            every {
                store.deleteCache()
            } returns flowOf()
        })
    }

    private fun expect(
        sut: CacheCryptoFeedUseCase,
        action: () -> Unit
    ) = runBlocking {
        action()

        sut.validateCache()

        verifySequence {
            store.retrieve()
            store.deleteCache()
        }

        verify(exactly = 1) {
            store.retrieve()
            store.deleteCache()
        }

        confirmVerified(store)
    }
}