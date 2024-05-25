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

    private val timestamp = Date()

    @Before
    fun setUp() {
        sut = CacheCryptoFeedUseCase(store = store, timestamp)
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
    fun testLoadFailsOnRetrievalError() = runBlocking {
        val retrievalException = anyException()

        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Failure(retrievalException), action = {
            every {
                store.retrieve()
            } returns flowOf(retrievalException)
        },
            retrieveExactly = 1
        )
    }

    @Test
    fun testLoadDeliversNoCryptoFeedOnEmptyCache() = runBlocking {
        expect(sut = sut, expectedResult = LoadCryptoFeedResult.Success(emptyList()), action = {
            every {
                store.retrieve()
            } returns flowOf(null)
        },
            retrieveExactly = 1
        )
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