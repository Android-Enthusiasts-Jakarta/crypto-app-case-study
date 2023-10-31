package com.hightech.cryptofeed.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class HttpClientResult {
    data class Failure(val exception: Exception): HttpClientResult()
}

interface HttpClient {
    fun get(): Flow<HttpClientResult>
}

class ConnectivityException : Exception()
class InvalidDataException : Exception()
class BadRequestException : Exception()
class InternalServerErrorException : Exception()

sealed class LoadCryptoFeedResult {
    data class Failure(val exception: Exception): LoadCryptoFeedResult()
}

class LoadCryptoFeedRemoteUseCase constructor(
    private val client: HttpClient
) {
    fun load(): Flow<LoadCryptoFeedResult> = flow {
        client.get().collect { result ->
            when (result) {
                is HttpClientResult.Failure -> {
                    when(result.exception) {
                        is ConnectivityException -> {
                            emit(LoadCryptoFeedResult.Failure(Connectivity()))
                        }
                        is InvalidDataException -> {
                            emit(LoadCryptoFeedResult.Failure(InvalidData()))
                        }
                        is BadRequestException -> {
                            emit(LoadCryptoFeedResult.Failure(BadRequest()))
                        }
                        is InternalServerErrorException -> {
                            emit(LoadCryptoFeedResult.Failure(InternalServerError()))
                        }
                    }
                }
            }
        }
    }
}

class Connectivity : Exception()
class InvalidData : Exception()
class BadRequest : Exception()
class InternalServerError : Exception()