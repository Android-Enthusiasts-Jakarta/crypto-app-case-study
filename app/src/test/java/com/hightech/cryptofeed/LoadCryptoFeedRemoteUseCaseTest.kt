package com.hightech.cryptofeed

import com.hightech.cryptofeed.api.Connectivity
import com.hightech.cryptofeed.api.HttpClient
import com.hightech.cryptofeed.api.LoadCryptoFeedRemoteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadCryptoFeedRemoteUseCaseTest {

    @Test
    fun testInitDoesNotRequestData() {
        val (_, client) = makeSut()

        assertTrue(client.getCount == 0)
    }

    @Test
    fun testLoadRequestsData() {
        val (sut, client) = makeSut()

        sut.load()

        assertEquals(1, client.getCount)
    }

    @Test
    fun testLoadTwiceRequestsDataTwice() {
        val (sut, client) = makeSut()

        sut.load()
        sut.load()

        assertEquals(2, client.getCount)
    }

    @Test
    fun testLoadDeliversErrorOnClientError() = runBlocking {
        val (sut, client) = makeSut()

        val capturedError = arrayListOf<Exception>()
        sut.load().collect { error ->
            capturedError.add(error)
        }

        client.error = Exception("Test")

        assertEquals(listOf(Connectivity::class.java), capturedError.map { it::class.java })
    }

    private fun makeSut(): Pair<LoadCryptoFeedRemoteUseCase, HttpClientSpy> {
        val client = HttpClientSpy()
        val sut = LoadCryptoFeedRemoteUseCase(client = client)
        return Pair(sut, client)
    }

    private class HttpClientSpy: HttpClient {
        var getCount = 0
        var error: Exception? = null

        override fun get(): Flow<Exception> = flow {
            if (error != null) {
                emit(error ?: Exception())
            }
            getCount += 1
        }
    }
}