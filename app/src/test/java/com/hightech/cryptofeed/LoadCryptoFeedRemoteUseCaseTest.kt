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
import com.hightech.cryptofeed.api.NotFound
import com.hightech.cryptofeed.api.NotFoundException
import com.hightech.cryptofeed.api.RemoteCoinInfo
import com.hightech.cryptofeed.api.RemoteCryptoFeedItem
import com.hightech.cryptofeed.api.RemoteDisplay
import com.hightech.cryptofeed.api.RemoteRootCryptoFeed
import com.hightech.cryptofeed.api.RemoteUsd
import com.hightech.cryptofeed.api.Unexpected
import com.hightech.cryptofeed.api.UnexpectedException
import com.hightech.cryptofeed.domain.CoinInfo
import com.hightech.cryptofeed.domain.CryptoFeed
import com.hightech.cryptofeed.domain.LoadCryptoFeedResult
import com.hightech.cryptofeed.domain.Raw
import com.hightech.cryptofeed.domain.Usd
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
    fun testLoadDeliversConnectivityErrorOnClientError() {
        expect(
            sut = sut,
            receivedHttpClientResult = HttpClientResult.Failure(ConnectivityException()),
            expectedResult = Connectivity(),
            exactly = 1,
        )
    }

    @Test
    fun testLoadDeliversInvalidDataError() {
        expect(
            sut = sut,
            receivedHttpClientResult = HttpClientResult.Failure(InvalidDataException()),
            expectedResult = InvalidData(),
            exactly = 1,
        )
    }

    @Test
    fun testLoadDeliversBadRequestError() {
        expect(
            sut = sut,
            receivedHttpClientResult = HttpClientResult.Failure(BadRequestException()),
            expectedResult = BadRequest(),
            exactly = 1,
        )
    }

    @Test
    fun testLoadDeliversNotFoundErrorOnClientError() {
        expect(
            sut = sut,
            receivedHttpClientResult = HttpClientResult.Failure(NotFoundException()),
            expectedResult = NotFound(),
            exactly = 1
        )
    }

    @Test
    fun testLoadDeliversInternalServerError() {
        expect(
            sut = sut,
            receivedHttpClientResult = HttpClientResult.Failure(InternalServerErrorException()),
            expectedResult = InternalServerError(),
            exactly = 1
        )
    }

    @Test
    fun testLoadDeliversUnexpectedError() {
        expect(
            sut = sut,
            receivedHttpClientResult = HttpClientResult.Failure((UnexpectedException())),
            expectedResult = Unexpected(),
            exactly = 1
        )
    }

    @Test
    fun testLoadDeliversItemsOn200HttpResponseWithResponse() {
        val cryptoFeedItemsResponse = listOf(
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

        val cryptoFeedItems = listOf(
            CryptoFeed(
                CoinInfo(
                    "1",
                    "BTC",
                    "Bitcoin",
                    "imageUrl"
                ),
                Raw(
                    Usd(
                        1.0,
                        1F,
                    ),
                ),
            ),
            CryptoFeed(
                CoinInfo(
                    "2",
                    "BTC 2",
                    "Bitcoin 2",
                    "imageUrl"
                ),
                Raw(
                    Usd(
                        2.0,
                        2F,
                    ),
                ),
            ),
        )

        expect(
            sut = sut,
            receivedHttpClientResult = HttpClientResult.Success(RemoteRootCryptoFeed(
                cryptoFeedItemsResponse
            )),
            expectedResult = LoadCryptoFeedResult.Success(cryptoFeedItems),
            exactly = 1
        )
    }

    @Test
    fun testLoadDeliversNoItemsOn200HttpResponseWithEmptyResponse() {
        val cryptoFeedItemsResponse = emptyList<RemoteCryptoFeedItem>()
        val cryptoFeedItems = emptyList<CryptoFeed>()

        expect(
            sut = sut,
            receivedHttpClientResult = HttpClientResult.Success(RemoteRootCryptoFeed(
                cryptoFeedItemsResponse
            )),
            expectedResult = LoadCryptoFeedResult.Success(cryptoFeedItems),
            exactly = 1
        )
    }

    private fun expect(
        sut: LoadCryptoFeedRemoteUseCase,
        receivedHttpClientResult: HttpClientResult,
        expectedResult: Any,
        exactly: Int = -1,
    ) = runBlocking {
        every {
            client.get()
        } returns flowOf(receivedHttpClientResult)

        sut.load().test {
            when (val receivedResult = awaitItem()) {
                is LoadCryptoFeedResult.Success -> {
                    assertEquals(
                        expectedResult,
                        receivedResult
                    )
                }
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

        confirmVerified(client)
    }
}