package com.hightech.cryptofeed

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

interface CryptoFeedStore {
    fun deleteCache()
}

class CryptoFeedCacheUseCase constructor(private val store: CryptoFeedStore) {}

class CryptoFeedCacheUseCaseTest {
    private val store = mockk<CryptoFeedStore>()
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

}