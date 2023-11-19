package com.hightech.cryptofeed

import app.cash.turbine.test
import com.hightech.cryptofeed.api.BadRequest
import com.hightech.cryptofeed.api.Connectivity
import com.hightech.cryptofeed.api.InternalServerError
import com.hightech.cryptofeed.api.InvalidData
import com.hightech.cryptofeed.api.NotFound
import com.hightech.cryptofeed.api.Unexpected
import com.hightech.cryptofeed.domain.LoadCryptoFeedResult
import com.hightech.cryptofeed.domain.LoadCryptoFeedUseCase
import com.hightech.cryptofeed.presentation.CryptoFeedViewModel
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CryptoFeedViewModelTest {
    private val useCase = spyk<LoadCryptoFeedUseCase>()
    private lateinit var sut: CryptoFeedViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        sut = CryptoFeedViewModel(useCase = useCase)

        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun testInitInitialState() {
        val uiState = sut.uiState.value

        assertFalse(uiState.isLoading)
        assertTrue(uiState.cryptoFeed.isEmpty())
        assert(uiState.failed.isEmpty())
    }

    @Test
    fun testInitDoesNotLoad() {
        verify(exactly = 0) {
            useCase.load()
        }

        confirmVerified(useCase)
    }

    @Test
    fun testLoadRequestsData() = runBlocking {
        every {
            useCase.load()
        } returns flowOf()

        sut.load()

        verify(exactly = 1) {
            useCase.load()
        }

        confirmVerified(useCase)
    }

    @Test
    fun testLoadTwiceRequestsDataTwice() = runBlocking {
        every {
            useCase.load()
        } returns flowOf()

        sut.load()
        sut.load()

        verify(exactly = 2) {
            useCase.load()
        }

        confirmVerified(useCase)
    }

    @Test
    fun testLoadIsLoadingState() = runBlocking {
        every {
            useCase.load()
        } returns flowOf()

        sut.load()

        sut.uiState.take(1).test {
            val receivedResult = awaitItem()
            assertEquals(true, receivedResult.isLoading)
            awaitComplete()
        }

        verify(exactly = 1) {
            useCase.load()
        }

        confirmVerified(useCase)
    }

    @Test
    fun testLoadFailedConnectivityShowsConnectivityError() = runBlocking {
        expect(
            result = LoadCryptoFeedResult.Failure(Connectivity()),
            sut = sut,
            expectedLoadingResult = false,
            expectedFailedResult = "Tidak ada internet"
        )
    }

    @Test
    fun testLoadFailedInvalidDataErrorShowsError() = runBlocking {
        expect(
            result = LoadCryptoFeedResult.Failure(InvalidData()),
            sut = sut,
            expectedLoadingResult = false,
            expectedFailedResult = "Terjadi kesalahan, coba lagi"
        )
    }

    @Test
    fun testLoadBadRequestShowsBadRequestError() = runBlocking {
        expect(
            result = LoadCryptoFeedResult.Failure(BadRequest()),
            sut = sut,
            expectedLoadingResult = false,
            expectedFailedResult = "Permintaan gagal, coba lagi"
        )
    }

    @Test
    fun testLoadNotFoundShowsNotFoundError() = runBlocking {
        expect(
            result = LoadCryptoFeedResult.Failure(NotFound()),
            sut = sut,
            expectedLoadingResult = false,
            expectedFailedResult = "Tidak ditemukan, coba lagi"
        )
    }

    @Test
    fun testLoadInternalServerErrorShowsInternalServerError() = runBlocking {
        expect(
            result = LoadCryptoFeedResult.Failure((InternalServerError())),
            sut = sut,
            expectedLoadingResult = false,
            expectedFailedResult = "Server sedang dalam perbaikan"
        )
    }

    @Test
    fun testLoadUnexpectedErrorShowsError() = runBlocking {
        expect(
            result = LoadCryptoFeedResult.Failure((Unexpected())),
            sut = sut,
            expectedLoadingResult = false,
            expectedFailedResult = "Terjadi kesalahan, coba lagi"
        )
    }

    @Test
    fun testLoadSuccessShowsCryptoFeed() = runBlocking {
        expect(
            result = LoadCryptoFeedResult.Success(cryptoFeed),
            sut = sut,
            expectedLoadingResult = false,
            expectedFailedResult = ""
        )
    }

    private fun expect(
        result: LoadCryptoFeedResult,
        sut: CryptoFeedViewModel,
        expectedLoadingResult: Boolean,
        expectedFailedResult: String
    ) = runBlocking {
        every {
            useCase.load()
        } returns flowOf(result)

        sut.load()

        sut.uiState.take(1).test {
            val receivedResult = awaitItem()
            if (receivedResult.failed.isEmpty()) {
                assertEquals(expectedLoadingResult, receivedResult.isLoading)
                assertEquals(cryptoFeed, receivedResult.cryptoFeed)
                assertEquals(expectedFailedResult, receivedResult.failed)
            } else {
                assertEquals(expectedLoadingResult, receivedResult.isLoading)
                assertEquals(expectedFailedResult, receivedResult.failed)
            }
            awaitComplete()
        }

        verify(exactly = 1) {
            useCase.load()
        }

        confirmVerified(useCase)
    }
}