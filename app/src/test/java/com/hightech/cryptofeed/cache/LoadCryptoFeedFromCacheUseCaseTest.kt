package com.hightech.cryptofeed.cache

import app.cash.turbine.test
import com.hightech.cryptofeed.domain.LoadCryptoFeedResult
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.Date

class LoadCryptoFeedFromCacheUseCaseTest {
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
    fun testLoadRequestsCacheRetrieval() = runBlocking {
        every {
            store.retrieve()
        } returns flowOf()

        sut.load().test {
            awaitComplete()
        }
        verify(exactly = 1) {
            store.retrieve()
        }

        confirmVerified(store)
    }

    @Test
    fun testLoadFailsOnRetrievalError() {
        val retrievalError = anyException()

        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Failure(retrievalError), action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCachedCryptoFeedResult.Failure(retrievalError))

            every {
                store.deleteCache()
            } returns flowOf()
        },
            retrieveExactly = 1
        )
    }

    @Test
    fun testLoadDeliversNoCryptoFeedOnEmptyCache() {
        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Success(emptyList()), action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCachedCryptoFeedResult.Empty())
        },
            retrieveExactly = 1
        )
    }

    @Test
    fun testLoadDeliversCachedCryptoFeedOnLessThanOneDayOldCache() {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val lessThanOneDayOldTimestamp = fixedCurrentDate.adding(days = -1).adding(seconds = 1)

        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Success(cryptoFeed.first), action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, lessThanOneDayOldTimestamp))
        })
    }

    @Test
    fun testLoadDeliversNoCryptoFeedOnOneDayOldCache() {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val oneDayOldTimestamp = fixedCurrentDate.adding(days = -1)

        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Success(emptyList()), action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, oneDayOldTimestamp))

            every {
                store.deleteCache()
            } returns flowOf()
        })
    }

    @Test
    fun testLoadDeliversNoCryptoFeedOnMoreThanOneDayOldCache() {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val moreThanOneDayOldTimestamp = fixedCurrentDate.adding(days = -1).adding(seconds = -1)

        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Success(emptyList()), action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, moreThanOneDayOldTimestamp))

            every {
                store.deleteCache()
            } returns flowOf()
        })
    }

    @Test
    fun testLoadHasNoSideEffectsOnRetrievalError() = runBlocking {
        val retrievalError= anyException()

        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Failure(retrievalError))

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
    fun testLoadHasNoSideEffectsOnEmptyCache() = runBlocking {
        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Empty())

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
    fun testLoadHasNoSideEffectsOnLessThanOneDayOldCache() = runBlocking {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
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

    @Test
    fun testLoadHasNoSideEffectsOnOneDayOldCache() = runBlocking {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val oneDayOldTimestamp = fixedCurrentDate.adding(days = -1)

        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, oneDayOldTimestamp))

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
    fun testLoadHasNoSideEffectsOnMoreThanOneDayOldCache() = runBlocking {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val oneDayOldTimestamp = fixedCurrentDate.adding(days = -1).adding(seconds = -1)

        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, oneDayOldTimestamp))

        sut.load().test {
            skipItems(1)
            awaitComplete()
        }

        verify(exactly = 1) {
            store.retrieve()
        }

        confirmVerified(store)
    }

    private fun expect(
        sut: CacheCryptoFeedUseCase,
        expectedResult: LoadResult,
        action: () -> Unit,
        retrieveExactly: Int = -1
    ) = runBlocking {
        action()

        sut.load().test {
            when(val receivedResult = awaitItem()) {
                is LoadCryptoFeedResult.Success -> {
                    assertEquals(expectedResult, receivedResult)
                }
                is LoadCryptoFeedResult.Failure -> {
                    assertEquals(expectedResult, receivedResult)
                }
                else -> {
                    fail("Expected result $expectedResult, got $receivedResult instead")
                }
            }
            awaitComplete()
        }

        verify(exactly = retrieveExactly) {
            store.retrieve()
        }
    }
}