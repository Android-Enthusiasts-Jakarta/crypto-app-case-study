package com.hightech.cryptofeed

import app.cash.turbine.test
import com.hightech.cryptofeed.cache.CryptoFeedCacheUseCase
import com.hightech.cryptofeed.cache.CryptoFeedStore
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class CryptoFeedCacheUseCaseTest {
    private val store = spyk<CryptoFeedStore>()
    private lateinit var sut: CryptoFeedCacheUseCase

    @Before
    fun setUp() {
        sut = CryptoFeedCacheUseCase(store = store)
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

        sut.save().test {
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

        sut.save().test {
            awaitComplete()
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        verify(exactly = 0) {
            store.insert()
        }

        confirmVerified(store)
    }
}