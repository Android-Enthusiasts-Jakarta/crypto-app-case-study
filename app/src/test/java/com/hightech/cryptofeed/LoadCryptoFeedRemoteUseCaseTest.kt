package com.hightech.cryptofeed

import app.cash.turbine.test
import com.hightech.cryptofeed.api.BadRequest
import com.hightech.cryptofeed.api.BadRequestException
import com.hightech.cryptofeed.api.Connectivity
import com.hightech.cryptofeed.api.ConnectivityException
import com.hightech.cryptofeed.api.HttpClient
import com.hightech.cryptofeed.api.HttpClientResult
import com.hightech.cryptofeed.api.InternalServerError
import com.hightech.cryptofeed.api.InternalServerErrorException
import com.hightech.cryptofeed.api.InvalidData
import com.hightech.cryptofeed.api.InvalidDataException
import com.hightech.cryptofeed.api.LoadCryptoFeedRemoteUseCase
import com.hightech.cryptofeed.api.LoadCryptoFeedResult
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LoadCryptoFeedRemoteUseCaseTest {
    private val client = spyk<HttpClient>()
    private lateinit var sut: LoadCryptoFeedRemoteUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        sut = LoadCryptoFeedRemoteUseCase(client)
    }

    @Test
    fun testInitDoesNotRequestData() {
        verify(exactly = 0) {
            client.get()
        }

        confirmVerified(client)
    }

    @Test
    fun testLoadRequestsData() = runBlocking {
        every {
            client.get()
        } returns flowOf()

        sut.load().test {
            awaitComplete()
        }

        verify(exactly = 1) {
            client.get()
        }

        confirmVerified(client)
    }

    @Test
    fun testLoadTwiceRequestsDataTwice() = runBlocking {
        every {
            client.get()
        } returns flowOf()

        sut.load().test {
            awaitComplete()
        }

        sut.load().test {
            awaitComplete()
        }

        verify(exactly = 2) {
            client.get()
        }

        confirmVerified(client)
    }


    @Test
    fun testLoadDeliversConnectivityErrorOnClientError() = runBlocking {
        expect(
            client = client,
            sut = sut,
            receivedHttpClientResult = HttpClientResult.Failure(ConnectivityException()),
            expectedResult = Connectivity(),
            exactly = 1,
            confirmVerified = client
        )
    }

    @Test
    fun testLoadDeliversInvalidDataError() {
        expect(
            client = client,
            sut = sut,
            receivedHttpClientResult = HttpClientResult.Failure(InvalidDataException()),
            expectedResult = InvalidData(),
            exactly = 1,
            confirmVerified = client
        )
    }

    @Test
    fun testLoadDeliversBadRequestError() {
        expect(
            client = client,
            sut = sut,
            receivedHttpClientResult = HttpClientResult.Failure(BadRequestException()),
            expectedResult = BadRequest(),
            exactly = 1,
            confirmVerified = client
        )
    }

    @Test
    fun testLoadDeliversInternalServerError() {
        expect(
            client = client,
            sut = sut,
            receivedHttpClientResult = HttpClientResult.Failure(InternalServerErrorException()),
            expectedResult = InternalServerError(),
            exactly = 1,
            confirmVerified = client
        )
    }

    private fun expect(
        client: HttpClient,
        sut: LoadCryptoFeedRemoteUseCase,
        receivedHttpClientResult: HttpClientResult,
        expectedResult: Any,
        exactly: Int = -1,
        confirmVerified: HttpClient
    ) = runBlocking {
        every {
            client.get()
        } returns flowOf(receivedHttpClientResult)

        sut.load().test {
            when (val receivedResult = awaitItem()) {
                is LoadCryptoFeedResult.Failure -> {
                    assertEquals(
                        expectedResult::class.java,
                        receivedResult.exception::class.java
                    )
                }
            }
            awaitComplete()
        }

        verify(exactly = exactly) {
            client.get()
        }

        confirmVerified(confirmVerified)
    }
}