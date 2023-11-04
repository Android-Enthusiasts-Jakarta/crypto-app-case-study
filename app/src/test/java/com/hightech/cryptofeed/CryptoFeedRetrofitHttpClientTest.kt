package com.hightech.cryptofeed

import app.cash.turbine.test
import com.hightech.cryptofeed.api.ConnectivityException
import com.hightech.cryptofeed.api.HttpClientResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

interface CryptoFeedService {
    suspend fun get()
}

class CryptoFeedRetrofitHttpClient(
    private val service: CryptoFeedService
) {
    fun get(): Flow<HttpClientResult> = flow {
        try {
            service.get()
        } catch (exception: Exception) {
            emit(HttpClientResult.Failure(ConnectivityException()))
        }
    }

}

class CryptoFeedRetrofitHttpClientTest {
    private val service = mockk<CryptoFeedService>()
    private lateinit var sut: CryptoFeedRetrofitHttpClient

    @Before
    fun setUp() {
        sut = CryptoFeedRetrofitHttpClient(service = service)
    }

    @Test
    fun testGetFailsOnConnectivityError() = runBlocking {
        coEvery {
            service.get()
        } throws IOException()

        sut.get().test {
            val receivedValue = awaitItem() as HttpClientResult.Failure
            assertEquals(ConnectivityException()::class.java, receivedValue.exception::class.java)
            awaitComplete()
        }

        coVerify {
            service.get()
        }
    }
}