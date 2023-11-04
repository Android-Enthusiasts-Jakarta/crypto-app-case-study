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
        expect(
            sut = sut,
            expectedResult = ConnectivityException()
        )
    }

    @Test
    fun testGetFailsOn400HttpResponse() {
        expect(
            withStatusCode = 400,
            sut = sut,
            expectedResult = BadRequestException()
        )
    }

    @Test
    fun testGetFailsOn404HttpResponse() {
        expect(
            withStatusCode = 404,
            sut = sut,
            expectedResult = NotFoundException()
        )
    }

    private fun expect(
        withStatusCode: Int? = null,
        sut: CryptoFeedRetrofitHttpClient,
        expectedResult: Any
    ) = runBlocking {
        when {
            withStatusCode != null -> {
                val response = Response.error<RemoteRootCryptoFeed>(withStatusCode, ResponseBody.create(null, ""))
                coEvery {
                    service.get()
                } throws HttpException(response)
            }

            expectedResult is ConnectivityException -> {
                coEvery {
                    service.get()
                } throws IOException()
            }
        }

        sut.get().test {
            val receivedValue = awaitItem() as HttpClientResult.Failure
            assertEquals(expectedResult::class.java, receivedValue.exception::class.java)
            awaitComplete()
        }

        coVerify(exactly = 1) {
            service.get()
        }
    }
}