package com.hightech.cryptofeed.cache

import app.cash.turbine.test
import com.hightech.cryptofeed.domain.LoadCryptoFeedResult
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date

class LoadCryptoFeedFromCacheUseCaseTest {
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
    fun testLoadDeletesCacheOnRetrievalError() = runBlocking {
        val retrievalError= anyException()

        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Failure(retrievalError))

        every {
            store.deleteCache()
        } returns flowOf()

        sut.load().test {
            skipItems(1)
            awaitComplete()
        }

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
    fun testLoadDoesNotDeletesCacheOnEmptyCache() = runBlocking {
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
    fun testLoadDoesNotDeleteCacheOnLessThanOneDayOldCache() = runBlocking {
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

    @Test
    fun testLoadDeleteCacheOnOneDayOldCache() = runBlocking {
        val cryptoFeed = uniqueItems()
        val oneDayOldTimestamp = fixedCurrentDate.adding(days = -1)

        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, oneDayOldTimestamp))

        every {
            store.deleteCache()
        } returns flowOf()

        sut.load().test {
            skipItems(1)
            awaitComplete()
        }

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
    fun testLoadDeleteCacheOnMoreThanOneDayOldCache() = runBlocking {
        val cryptoFeed = uniqueItems()
        val oneDayOldTimestamp = fixedCurrentDate.adding(days = -1).adding(seconds = -1)

        every {
            store.retrieve()
        } returns flowOf(RetrieveCachedCryptoFeedResult.Found(cryptoFeed.second, oneDayOldTimestamp))

        every {
            store.deleteCache()
        } returns flowOf()

        sut.load().test {
            skipItems(1)
            awaitComplete()
        }

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

fun Date.adding(days: Int): Date = Calendar.getInstance().apply {
    time = this@adding
    add(Calendar.DAY_OF_YEAR, days)
}.time

fun Date.adding(seconds: Long): Date {
    val time = this.time + seconds * 1000
    return Date(time)
}