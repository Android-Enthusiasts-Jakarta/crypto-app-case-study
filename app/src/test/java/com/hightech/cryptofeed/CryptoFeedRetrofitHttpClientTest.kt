package com.hightech.cryptofeed

import app.cash.turbine.test
import com.hightech.cryptofeed.api.BadRequestException
import com.hightech.cryptofeed.api.ConnectivityException
import com.hightech.cryptofeed.api.CryptoFeedRetrofitHttpClient
import com.hightech.cryptofeed.api.CryptoFeedService
import com.hightech.cryptofeed.api.HttpClientResult
import com.hightech.cryptofeed.api.NotFoundException
import com.hightech.cryptofeed.api.RemoteRootCryptoFeed
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException


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

        coVerify(exactly = 1) {
            service.get()
        }
    }

    @Test
    fun testGetFailsOn400HttpResponse() = runBlocking {
        val response = Response.error<RemoteRootCryptoFeed>(400, ResponseBody.create(null, ""))

        coEvery {
            service.get()
        } throws HttpException(response)

        sut.get().test {
            val receivedValue = awaitItem() as HttpClientResult.Failure
            assertEquals(BadRequestException()::class.java, receivedValue.exception::class.java)
            awaitComplete()
        }

        coVerify(exactly = 1) {
            service.get()
        }
    }

    @Test
    fun testGetFailsOn404HttpResponse() = runBlocking {
        val response = Response.error<RemoteRootCryptoFeed>(404, ResponseBody.create(null, ""))

        coEvery {
            service.get()
        } throws HttpException(response)

        sut.get().test {
            val receivedValue = awaitItem() as HttpClientResult.Failure
            assertEquals(NotFoundException()::class.java, receivedValue.exception::class.java)
            awaitComplete()
        }

        coVerify(exactly = 1) {
            service.get()
        }
    }
}