package com.hightech.cryptofeed.cache

import app.cash.turbine.test
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
    private val store = spyk<RoomCryptoFeedStore>()
    private lateinit var sut: CacheCryptoFeedUseCase

    private val feed = listOf(uniqueCryptoFeed(), uniqueCryptoFeed())

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
        val captureTimestamp = slot<Date>()
        val captureFeed = slot<List<CryptoFeed>>()

        every {
            store.deleteCache()
        } returns flowOf(null)

        every {
            store.insert(capture(captureFeed), capture(captureTimestamp))
        } returns flowOf()

        sut.save(feed).test {
            assertEquals(feed, captureFeed.captured)
            assertEquals(timestamp, captureTimestamp.captured)
            awaitComplete()
        }

        verifyOrder {
            store.deleteCache()
            store.insert(feed, timestamp)
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        verify(exactly = 1) {
            store.insert(feed, timestamp)
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveFailsOnDeletionError() {
        expect(
            sut = sut, expectedError = Exception(), action = {
                every {
                    store.deleteCache()
                } returns flowOf(Exception())
            },
            deleteExactly = 1,
            insertExactly = 0
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
                    store.insert(feed, timestamp)
                } returns flowOf(Exception())
            },
            ordering = {
                verifyOrder {
                    store.deleteCache()
                    store.insert(feed, timestamp)
                }
            },
            deleteExactly = 1,
            insertExactly = 1
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
                    store.insert(feed, timestamp)
                } returns flowOf(null)
            },
            ordering = {
                verifyOrder {
                    store.deleteCache()
                    store.insert(feed, timestamp)
                }
            },
            deleteExactly = 1,
            insertExactly = 1
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

        sut.save(feed).test {
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
            store.insert(feed, timestamp)
        }

        confirmVerified(store)
    }

    private fun uniqueCryptoFeed(): CryptoFeed {
        return CryptoFeed(
            CoinInfo(
                UUID.randomUUID().toString(), "any", "any", "any-url"
            ), Raw(
                Usd(
                    1.0,
                    1F,
                )
            )
        )
    }
}