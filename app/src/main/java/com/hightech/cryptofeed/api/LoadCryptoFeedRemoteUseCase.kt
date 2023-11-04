package com.hightech.cryptofeed.api

import com.hightech.cryptofeed.domain.CoinInfo
import com.hightech.cryptofeed.domain.CryptoFeed
import com.hightech.cryptofeed.domain.Raw
import com.hightech.cryptofeed.domain.Usd
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ConnectivityException : Exception()
class InvalidDataException : Exception()
class BadRequestException : Exception()
class InternalServerErrorException : Exception()

sealed class LoadCryptoFeedResult {
    data class Success(val cryptoFeedItems: List<CryptoFeed>) : LoadCryptoFeedResult()
    data class Failure(val exception: Exception) : LoadCryptoFeedResult()
}

class LoadCryptoFeedRemoteUseCase constructor(
    private val client: HttpClient
) {
    fun load(): Flow<LoadCryptoFeedResult> = flow {
        client.get().collect { result ->
            when (result) {
                is HttpClientResult.Success -> {
                    emit(LoadCryptoFeedResult.Success(result.root.data.toModels()))
                }
                is HttpClientResult.Failure -> {
                    when (result.exception) {
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

private fun List<RemoteCryptoFeedItem>.toModels(): List<CryptoFeed> {
    return map {
        CryptoFeed(
            CoinInfo(
                it.remoteCoinInfo.id,
                it.remoteCoinInfo.name,
                it.remoteCoinInfo.fullName,
                it.remoteCoinInfo.imageUrl
            ),
            Raw(
                Usd(
                    it.remoteRaw.usd.price,
                    it.remoteRaw.usd.changePctDay,
                ),
            ),
        )
    }
}

class Connectivity : Exception()
class InvalidData : Exception()
class BadRequest : Exception()
class InternalServerError : Exception()