package com.hightech.cryptofeed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hightech.cryptofeed.api.BadRequest
import com.hightech.cryptofeed.api.Connectivity
import com.hightech.cryptofeed.api.InternalServerError
import com.hightech.cryptofeed.api.InvalidData
import com.hightech.cryptofeed.api.NotFound
import com.hightech.cryptofeed.domain.CryptoFeed
import com.hightech.cryptofeed.domain.LoadCryptoFeedResult
import com.hightech.cryptofeed.domain.LoadCryptoFeedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class UiState(
    val isLoading: Boolean = false,
    val cryptoFeed: List<CryptoFeed> = emptyList(),
    val failed: String = ""
)

class CryptoFeedViewModel(private val useCase: LoadCryptoFeedUseCase): ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            useCase.load().collect { result ->
                _uiState.update {
                    when(result) {
                        is LoadCryptoFeedResult.Success -> {
                            it.copy(
                                isLoading = false,
                                cryptoFeed = result.cryptoFeed
                            )
                        }
                        is LoadCryptoFeedResult.Failure -> {
                            it.copy(
                                isLoading = false,
                                failed = when(result.exception) {
                                    is Connectivity -> "Tidak ada internet"
                                    is InvalidData -> "Terjadi kesalahan, coba lagi"
                                    is BadRequest -> "Permintaan gagal, coba lagi"
                                    is NotFound -> "Tidak ditemukan, coba lagi"
                                    is InternalServerError -> "Server sedang dalam perbaikan"
                                    else -> { "Terjadi kesalahan, coba lagi" }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}