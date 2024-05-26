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
        val retrievalException = anyException()

        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Failure(retrievalException), action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCacheCryptoFeedResult.Failure(retrievalException))
        },
            retrieveExactly = 1
        )
    }

    @Test
    fun testLoadDeliversNoCryptoFeedOnEmptyCache() {
        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Success(emptyList()), action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCacheCryptoFeedResult.Empty())
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
            } returns flowOf(RetrieveCacheCryptoFeedResult.Found(cryptoFeed.second, lessThanOneDayOldTimestamp))
        })
    }

    @Test
    fun testLoadDeliversNoCryptoFeedOnOneDayOldCache() {
        val cryptoFeed = uniqueItems()
        val oneDayOldTimestamp = fixedCurrentDate.adding(days = -1)

        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Success(emptyList()), action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCacheCryptoFeedResult.Found(cryptoFeed.second, oneDayOldTimestamp))
        })
    }

    @Test
    fun testLoadDeliversNoCryptoFeedOnMoreThanOneDayOldCache() {
        val cryptoFeed = uniqueItems()
        val moreThanOneDayOldTimestamp = fixedCurrentDate.adding(days = -1).adding(seconds = -1)

        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Success(emptyList()), action = {
            every {
                store.retrieve()
            } returns flowOf(RetrieveCacheCryptoFeedResult.Found(cryptoFeed.second, moreThanOneDayOldTimestamp))
        })
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

        confirmVerified(store)
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