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
    fun testLoadDeliversCachedCryptoFeedOnNonExpiredCache() {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val nonExpiredTimestamp = fixedCurrentDate.minusCryptoFeedCacheMaxAge().adding(seconds = 1)

        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Success(cryptoFeed.first), action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, nonExpiredTimestamp))
        })
    }

    @Test
    fun testLoadDeliversNoCryptoFeedOnCacheExpiration() {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val expirationTimestamp = fixedCurrentDate.minusCryptoFeedCacheMaxAge()

        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Success(emptyList()), action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, expirationTimestamp))

            every {
                store.deleteCache()
            } returns flowOf()
        })
    }

    @Test
    fun testLoadDeliversNoCryptoFeedOnOnExpiredCache() {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val expiredCacheTimestamp = fixedCurrentDate.minusCryptoFeedCacheMaxAge().adding(seconds = -1)

        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Success(emptyList()), action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, expiredCacheTimestamp))

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
    fun testLoadHasNoSideEffectsOnNonExpiredCache() = runBlocking {
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
    fun testLoadHasNoSideEffectsOnCacheExpiration() = runBlocking {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val expirationTimestamp = fixedCurrentDate.minusCryptoFeedCacheMaxAge()

        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, expirationTimestamp))

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
    fun testLoadHasNoSideEffectsOnExpiredCache() = runBlocking {
        val cryptoFeed = uniqueItems()
        val fixedCurrentDate = Date()
        val expiredCacheTimestamp = fixedCurrentDate.minusCryptoFeedCacheMaxAge().adding(seconds = -1)

        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, expiredCacheTimestamp))

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