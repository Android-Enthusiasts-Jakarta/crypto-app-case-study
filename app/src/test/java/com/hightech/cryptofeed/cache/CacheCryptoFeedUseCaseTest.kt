package com.hightech.cryptofeed.cache

import com.hightech.cryptofeed.api.LoadCryptoFeedRemoteUseCase
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class CacheCryptoFeedUseCase constructor(
    private val store: RoomCryptoFeedStore
) {

}

class RoomCryptoFeedStore {
    fun deleteCache() {

    }
}

class CacheCryptoFeedUseCaseTest {
    private val store = spyk<RoomCryptoFeedStore>()
    private lateinit var sut: CacheCryptoFeedUseCase

    @Before
    fun setUp() {
        sut = CacheCryptoFeedUseCase(store)
    }

    @Test
    fun testInitDoesNotDeleteCacheUponCreation() = runBlocking {
        verify(exactly = 0) {
            store.deleteCache()
        }

        confirmVerified(store)
    }
}