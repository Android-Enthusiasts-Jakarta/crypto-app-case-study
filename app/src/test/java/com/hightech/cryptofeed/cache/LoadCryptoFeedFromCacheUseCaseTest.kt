package com.hightech.cryptofeed.cache

import io.mockk.confirmVerified
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
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
}