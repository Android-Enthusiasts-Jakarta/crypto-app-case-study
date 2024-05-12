package com.hightech.cryptofeed

import app.cash.turbine.test
import com.hightech.cryptofeed.cache.CacheCryptoFeedUseCase
import com.hightech.cryptofeed.cache.CryptoFeedStore
import com.hightech.cryptofeed.cache.SaveResult
import com.hightech.cryptofeed.domain.CoinInfo
import com.hightech.cryptofeed.domain.CryptoFeed
import com.hightech.cryptofeed.domain.Raw
import com.hightech.cryptofeed.domain.Usd
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.UUID

class CacheCryptoFeedUseCaseTest {
    private val store = spyk<CryptoFeedStore>()
    private lateinit var sut: CacheCryptoFeedUseCase

    private val timestamp = Date()

    @Before
    fun setUp() {
        sut = CacheCryptoFeedUseCase(store = store, timestamp)
    }

    @Test
    fun testInitDoesNotDeletionCacheUponCreation() = runBlocking {
        verify(exactly = 0) {
            store.deleteCache()
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveRequestsCacheDeletion() = runBlocking {
        every {
            store.deleteCache()
        } returns flowOf()

        sut.save(listOf(uniqueCryptoFeed())).test {
            awaitComplete()
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveDoestNotRequestsCacheInsertionOnDeletionError() = runBlocking {
        every {
            store.deleteCache()
        } returns flowOf(Exception())

        sut.save(listOf(uniqueCryptoFeed())).test {
            skipItems(1)
            awaitComplete()
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        verify(exactly = 0) {
            store.insert(any(), any())
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveRequestsNewCacheInsertionWithTimestampOnSuccessfulDeletion() = runBlocking {
        val captureFeed = slot<List<CryptoFeed>>()
        val captureTimeStamp = slot<Date>()

        val feeds = listOf(uniqueCryptoFeed(), uniqueCryptoFeed())

        every {
            store.deleteCache()
        } returns flowOf(null)

        every {
            store.insert(capture(captureFeed), capture(captureTimeStamp))
        } returns flowOf()

        sut.save(feeds).test {
            assertEquals(feeds, captureFeed.captured)
            assertEquals(timestamp, captureTimeStamp.captured)
            awaitComplete()
        }

        verifyOrder {
            store.deleteCache()
            store.insert(any(), any())
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        verify(exactly = 1) {
            store.insert(any(), any())
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveFailsOnDeletionError() {
        expect(
            sut = sut, expectedError = Exception(), action = {
                every {
                    store.deleteCache()
                } returns flowOf(SaveResult())
            },
            deleteExactly = 1,
            insertExactly = 0,
        )
    }

    @Test
    fun testSaveFailsOnInsertionError() {
        expect(
            sut = sut, expectedError = Exception(), action = {
                every {
                    store.deleteCache()
                } returns flowOf(null)

                every {
                    store.insert(any(), any())
                } returns flowOf(SaveResult())
            },
            ordering = {
                verifyOrder {
                    store.deleteCache()
                    store.insert(any(), any())
                }
            },
            deleteExactly = 1,
            insertExactly = 1,
        )
    }

    @Test
    fun testSaveSucceedsOnSuccessfulCacheInsertion() {
        expect(
            sut = sut, expectedError = null, action = {
                every {
                    store.deleteCache()
                } returns flowOf(null)

                every {
                    store.insert(any(), any())
                } returns flowOf(null)
            },
            ordering = {
                verifyOrder {
                    store.deleteCache()
                    store.insert(any(), any())
                }
            },
            deleteExactly = 1,
            insertExactly = 1,
        )
    }

    private fun expect(
        sut: CacheCryptoFeedUseCase,
        expectedError: Exception?,
        action: () -> Unit,
        ordering: () -> Unit = {},
        deleteExactly: Int = -1,
        insertExactly: Int = -1,
    ) = runBlocking {
        action()

        sut.save(listOf(uniqueCryptoFeed())).test {
            if (expectedError != null) {
                assertEquals(expectedError::class.java, awaitItem()!!::class.java)
            } else {
                assertEquals(expectedError, awaitItem())
            }
            awaitComplete()
        }

        ordering()

        verify(exactly = deleteExactly) {
            store.deleteCache()
        }

        verify(exactly = insertExactly) {
            store.insert(any(), any())
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