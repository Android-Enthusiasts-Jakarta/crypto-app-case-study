import kotlinx.coroutines.flow.Flow
import java.lang.Exception

sealed class CryptoFeedResult {
    data class Success(val cryptoFeed: List<CryptoFeed>): CryptoFeedResult()
    data class Error(val exception: Exception): CryptoFeedResult()
}

interface CryptoFeedUseCase {
    fun load(): Flow<CryptoFeedResult>
}