package com.hightech.cryptofeed.`api infrastructure`

import com.hightech.cryptofeed.api.BadRequestException
import com.hightech.cryptofeed.api.ConnectivityException
import com.hightech.cryptofeed.api.HttpClientResult
import com.hightech.cryptofeed.api.InternalServerErrorException
import com.hightech.cryptofeed.api.InvalidDataException
import com.hightech.cryptofeed.api.NotFoundException
import com.hightech.cryptofeed.api.RemoteCoinInfo
import com.hightech.cryptofeed.api.RemoteCryptoFeed
import com.hightech.cryptofeed.api.RemoteRaw
import com.hightech.cryptofeed.api.RemoteRootCryptoFeed
import com.hightech.cryptofeed.api.RemoteUsd
import com.hightech.cryptofeed.api.UnexpectedException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class CryptoFeedRetrofitHttpClient(
    private val service: CryptoFeedService
) {
    fun get(): Flow<HttpClientResult> = flow {
        try {
            val response = service.get()
            emit(HttpClientResult.Success(toModels(response)))
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> {
                    emit(HttpClientResult.Failure(ConnectivityException()))
                }

                is HttpException -> {
                    when (exception.code()) {
                        400 -> {
                            emit(HttpClientResult.Failure(BadRequestException()))
                        }

                        404 -> {
                            emit(HttpClientResult.Failure(NotFoundException()))
                        }

                        422 -> {
                            emit(HttpClientResult.Failure(InvalidDataException()))
                        }

                        500 -> {
                            emit(HttpClientResult.Failure(InternalServerErrorException()))
                        }
                    }
                }

                else -> {
                    emit(HttpClientResult.Failure(UnexpectedException()))
                }
            }
        }
    }

    private fun toModels(response: RootCryptoFeedResponse): RemoteRootCryptoFeed {
        return RemoteRootCryptoFeed(
            data = response.data.map {
                RemoteCryptoFeed(
                    remoteCoinInfo = RemoteCoinInfo(
                        id = it.coinInfoResponse.id,
                        name = it.coinInfoResponse.name,
                        fullName = it.coinInfoResponse.fullName,
                        imageUrl = it.coinInfoResponse.imageUrl,
                    ),
                    remoteRaw = RemoteRaw(
                        remoteUsd = RemoteUsd(
                            price = it.rawResponse.usdResponse.price,
                            changePctDay = it.rawResponse.usdResponse.changePctDay
                        )
                    )
                )
            }
        )
    }
}