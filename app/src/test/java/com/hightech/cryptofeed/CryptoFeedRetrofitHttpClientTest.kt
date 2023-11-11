package com.hightech.cryptofeed

import app.cash.turbine.test
import com.hightech.cryptofeed.api.BadRequestException
import com.hightech.cryptofeed.api.ConnectivityException
import com.hightech.cryptofeed.api.CryptoFeedRetrofitHttpClient
import com.hightech.cryptofeed.api.CryptoFeedService
import com.hightech.cryptofeed.api.HttpClientResult
import com.hightech.cryptofeed.api.InternalServerErrorException
import com.hightech.cryptofeed.api.InvalidDataException
import com.hightech.cryptofeed.api.NotFoundException
import com.hightech.cryptofeed.api.RemoteCoinInfo
import com.hightech.cryptofeed.api.RemoteCryptoFeedItem
import com.hightech.cryptofeed.api.RemoteDisplay
import com.hightech.cryptofeed.api.RemoteRootCryptoFeed
import com.hightech.cryptofeed.api.RemoteUsd
import com.hightech.cryptofeed.api.UnexpectedException
import com.hightech.cryptofeed.domain.LoadCryptoFeedResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
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

    @Test
    fun testGetFailsOn422HttpResponse() {
        expect(
            withStatusCode = 422,
            sut = sut,
            expectedResult = InvalidDataException()
        )
    }

    @Test
    fun testGetFailsOn500HttpResponse() {
        expect(
            withStatusCode = 500,
            sut = sut,
            expectedResult = InternalServerErrorException()
        )
    }

    @Test
    fun testGetFailsOnUnexpectedException() {
        expect(
            sut = sut,
            expectedResult = UnexpectedException()
        )
    }

    @Test
    fun testGetSuccessOn200HttpResponseWithResponse() {
        val cryptoFeedResponse = listOf(
            RemoteCryptoFeedItem(
                RemoteCoinInfo(
                    "1",
                    "BTC",
                    "Bitcoin",
                    "imageUrl",
                ),
                RemoteDisplay(
                    RemoteUsd(
                        1.0,
                        1F,
                    ),
                ),
            ),
            RemoteCryptoFeedItem(
                RemoteCoinInfo(
                    "2",
                    "BTC 2",
                    "Bitcoin 2",
                    "imageUrl"
                ),
                RemoteDisplay(
                    RemoteUsd(
                        2.0,
                        2F,
                    ),
                ),
            ),
        )

        expect(
            sut = sut,
            receivedResult = RemoteRootCryptoFeed(cryptoFeedResponse),
            expectedResult = HttpClientResult.Success(
                RemoteRootCryptoFeed(
                    cryptoFeedResponse
                )
            )
        )
    }

    private fun expect(
        withStatusCode: Int? = null,
        sut: CryptoFeedRetrofitHttpClient,
        receivedResult: Any? = null,
        expectedResult: Any
    ) = runBlocking {
        when {
            withStatusCode != null -> {
                val response = Response.error<RemoteRootCryptoFeed>(
                    withStatusCode,
                    ResponseBody.create(null, "")
                )
                coEvery {
                    service.get()
                } throws HttpException(response)
            }

            expectedResult is ConnectivityException -> {
                coEvery {
                    service.get()
                } throws IOException()
            }

            expectedResult is HttpClientResult.Success -> {
                coEvery {
                    service.get()
                } returns receivedResult as RemoteRootCryptoFeed
            }

            else -> {
                coEvery {
                    service.get()
                } throws Exception()
            }
        }

        sut.get().test {
            when (val receivedResult = awaitItem()) {
                is HttpClientResult.Success -> {
                    assertEquals(
                        expectedResult,
                        receivedResult
                    )
                }
                is HttpClientResult.Failure -> {
                    assertEquals(
                        expectedResult::class.java,
                        receivedResult.exception::class.java
                    )
                }
            }
            awaitComplete()
        }

        coVerify(exactly = 1) {
            service.get()
        }

        confirmVerified(service)
    }
}