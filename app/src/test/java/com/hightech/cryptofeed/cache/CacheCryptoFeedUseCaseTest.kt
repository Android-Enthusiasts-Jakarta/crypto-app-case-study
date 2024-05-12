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
        every {
            store.deleteCache()
        } returns flowOf()

        sut.save(uniqueItems().first).test {
            awaitComplete()
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveDoesNotRequestCacheInsertionOnDeletionError() = runBlocking {
        val deletionError = anyException()

        every {
            store.deleteCache()
        } returns flowOf(deletionError)

        sut.save(uniqueItems().first).test {
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
        val captureTimestamp = slot<Date>()
        val captureFeed = slot<List<LocalCryptoFeed>>()

        val items = uniqueItems()

        every {
            store.deleteCache()
        } returns flowOf(null)

        every {
            store.insert(capture(captureFeed), capture(captureTimestamp))
        } returns flowOf()

        sut.save(items.first).test {
            assertEquals(items.second, captureFeed.captured)
            assertEquals(timestamp, captureTimestamp.captured)
            awaitComplete()
        }

        verifyOrder {
            store.deleteCache()
            store.insert(any(), any())
        }

        verify(exactly = 1) {
            store.deleteCache()
            store.insert(any(), any())
        }

        confirmVerified(store)
    }

    @Test
    fun testSaveFailsOnDeletionError() {
        val deletionError = anyException()

        expect(
            sut = sut, expectedError = deletionError, action = {
                every {
                    store.deleteCache()
                } returns flowOf(deletionError)
            },
            deleteExactly = 1,
            insertExactly = 0
        )
    }

    @Test
    fun testSaveFailsOnInsertionError() {
        val deletionError = anyException()

        expect(
            sut = sut, expectedError = deletionError, action = {
                every {
                    store.deleteCache()
                } returns flowOf(null)

                every {
                    store.insert(any(), any())
                } returns flowOf(deletionError)
            },
            ordering = {
                verifyOrder {
                    store.deleteCache()
                    store.insert(any(), any())
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

        sut.save(uniqueItems().first).test {
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
                UUID.randomUUID().toString(), "any", "any", "any-url"
            ), Raw(
                Usd(
                    1.0,
                    1F,
                )
            )
        )
    }

    private fun uniqueItems(): Pair<List<CryptoFeed>, List<LocalCryptoFeed>> {
        val cryptoFeeds = listOf(uniqueCryptoFeed(), uniqueCryptoFeed())
        val localCryptoFeeds = cryptoFeeds.map {
            LocalCryptoFeed(
                coinInfo = LocalCoinInfo(
                    id = it.coinInfo.id,
                    name = it.coinInfo.name,
                    fullName = it.coinInfo.fullName,
                    imageUrl = it.coinInfo.imageUrl
                ),
                raw = LocalRaw(
                    usd = LocalUsd(
                        price = it.raw.usd.price,
                        changePctDay = it.raw.usd.changePctDay
                    )
                )
            )
        }
        return Pair(cryptoFeeds, localCryptoFeeds)
    }

    private fun anyException(): Exception {
        return Exception()
    }
}