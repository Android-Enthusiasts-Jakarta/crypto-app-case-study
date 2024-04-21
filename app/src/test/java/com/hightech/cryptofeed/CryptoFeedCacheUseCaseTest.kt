package com.hightech.cryptofeed

import io.mockk.confirmVerified
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

interface CryptoFeedStore {
    fun deleteCache()
}

class CryptoFeedCacheUseCase constructor(private val store: CryptoFeedStore) {
    fun save() {
        store.deleteCache()
    }
}

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
        sut.save()

        verify(exactly = 1) {
            store.deleteCache()
        }

        confirmVerified(store)
    }
}