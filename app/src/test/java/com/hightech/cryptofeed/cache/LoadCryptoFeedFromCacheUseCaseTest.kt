package com.hightech.cryptofeed.cache

import app.cash.turbine.test
import com.hightech.cryptofeed.domain.CryptoFeed
import com.hightech.cryptofeed.domain.LoadCryptoFeedResult
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert
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

        every {
            store.retrieve()
        } returns flowOf(retrievalException)

        sut.load().test {
            when(val result = awaitItem()) {
                is LoadCryptoFeedResult.Failure -> {
                    assertEquals(retrievalException, result.exception)
                }
                else -> {
                    fail("Expected failure, got $result instead")
                }
            }

            awaitComplete()
        }
        verify(exactly = 1) {
            store.retrieve()
        }

        confirmVerified(store)
    }

    @Test
    fun testLoadDeliversNoCryptoFeedOnEmptyCache() = runBlocking {
        every {
            store.retrieve()
        } returns flowOf(null)

        sut.load().test {
            when(val result = awaitItem()) {
                is LoadCryptoFeedResult.Success -> {
                    assertEquals(listOf<CryptoFeed>(), result.cryptoFeed)
                }
                else -> {
                    fail("Expected success, got $result instead")
                }
            }
            awaitComplete()
        }
        verify(exactly = 1) {
            store.retrieve()
        }

        confirmVerified(store)
    }
}