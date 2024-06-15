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

    private val fixedCurrentDate = Date()

    @Before
    fun setUp() {
        sut = CacheCryptoFeedUseCase(store = store, fixedCurrentDate)
    }

    @Test
    fun testInitDoesNotLoadCacheUponCreation() = runBlocking {
        verify(exactly = 0) {
            store.retrieve()
        }

        confirmVerified(store)
    }

    @Test
    fun testValidateCacheDeletesCacheOnRetrievalError() = runBlocking {
        val retrievalError= anyException()

        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Failure(retrievalError))

        every {
            store.deleteCache()
        } returns flowOf()

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
    fun testValidateCacheDoesNotDeletesOnLessThanOneDayOldCache() = runBlocking {
        val cryptoFeed = uniqueItems()
        val lessThanOneDayOldTimestamp = fixedCurrentDate.adding(days = -1).adding(seconds = 1)

        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, lessThanOneDayOldTimestamp))

        sut.load().test {
            skipItems(1)
            awaitComplete()
        }

        verify(exactly = 1) {
            store.retrieve()
        }

        confirmVerified(store)
    }
}