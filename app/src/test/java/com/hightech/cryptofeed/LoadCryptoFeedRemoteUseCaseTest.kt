package com.hightech.cryptofeed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadCryptoFeedRemoteUseCase {
    fun load() {
        HttpClient.instance.getCount = 1
    }
}

class HttpClient private constructor() {
    companion object {
        var instance = HttpClient()
    }
    var getCount = 0
}

class LoadCryptoFeedRemoteUseCaseTest {

    @Test
    fun testInitDoesNotLoad() {
        val client = HttpClient.instance
        LoadCryptoFeedRemoteUseCase()

        assertTrue(client.getCount == 0)
    }

    @Test
    fun testLoadRequestData() {
        val client = HttpClient.instance
        val sut = LoadCryptoFeedRemoteUseCase()

        sut.load()

        assertEquals(1, client.getCount)
    }
}