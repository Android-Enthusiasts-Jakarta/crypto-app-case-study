package com.hightech.cryptofeed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadCryptoFeedRemoteUseCase constructor(
    private val client: HttpClient
) {
    fun load() {
        client.get()
    }
}

interface HttpClient {
    fun get()
}

class HttpClientSpy: HttpClient {
    var getCount = 0

    override fun get() {
        getCount += 1
    }
}

class LoadCryptoFeedRemoteUseCaseTest {

    @Test
    fun testInitDoesNotLoad() {
        val client = HttpClientSpy()
        LoadCryptoFeedRemoteUseCase(client = client)

        assertTrue(client.getCount == 0)
    }

    @Test
    fun testLoadRequestData() {
        val client = HttpClientSpy()
        val sut = LoadCryptoFeedRemoteUseCase(client = client)

        sut.load()

        assertEquals(1, client.getCount)
    }
}