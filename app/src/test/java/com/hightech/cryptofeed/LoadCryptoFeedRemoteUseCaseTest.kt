package com.hightech.cryptofeed

import org.junit.Assert.assertTrue
import org.junit.Test

class LoadCryptoFeedRemoteUseCase {

}

class HttpClient {
    var getCount = 0
}

class LoadCryptoFeedRemoteUseCaseTest {
    @Test
    fun testInitDoesNotLoad() {
        val client = HttpClient()
        LoadCryptoFeedRemoteUseCase()

        assertTrue(client.getCount == 0)
    }
}