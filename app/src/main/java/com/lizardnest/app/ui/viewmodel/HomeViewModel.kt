package com.lizardnest.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lizardnest.app.data.network.NetworkMonitor
import com.lizardnest.app.data.repository.NestRepository
import com.lizardnest.app.ui.model.ConnectionStatus
import com.lizardnest.app.ui.model.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NestRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var currentDataJob: Job? = null
    private var historyDataJob: Job? = null
    private var networkJob: Job? = null

    init {
        networkMonitor.startMonitoring()
        loadInitialData()
        startPolling()
        observeNetwork()
    }

    /**
     * 监听网络状态变化，自动更新连接状态
     */
    private fun observeNetwork() {
        networkJob = viewModelScope.launch {
            networkMonitor.isConnected.collect { connected ->
                if (connected && _uiState.value.connectionStatus != ConnectionStatus.CONNECTED) {
                    _uiState.update { it.copy(connectionStatus = ConnectionStatus.RECONNECTING) }
                    // 网络恢复，自动重试加载数据
                    retry()
                } else if (!connected) {
                    _uiState.update { it.copy(connectionStatus = ConnectionStatus.DISCONNECTED) }
                }
            }
        }
    }

    /**
     * 首次加载：并发加载当前数据和历史数据
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "正在连接设备…") }

            // 设置视频流地址
            _uiState.update { it.copy(streamUrl = repository.getStreamUrl()) }

            try {
                coroutineScope {
                    // 并发加载当前数据和历史数据
                    launch { loadCurrentData() }
                    launch { loadHistoryData() }
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "数据加载失败: ${e.message}"
                    )
                }
            }
        }

        // 订阅数据变化
        observeDataFlows()
    }

    /**
     * 加载当前温湿度数据
     */
    private suspend fun loadCurrentData() {
        repository.refreshCurrentData()
    }

    /**
     * 加载 24 小时历史数据
     */
    private suspend fun loadHistoryData() {
        repository.loadHistoryData(24)
    }

    /**
     * 订阅 Repository 的数据流，自动更新 UI
     */
    private fun observeDataFlows() {
        currentDataJob = viewModelScope.launch {
            repository.currentData.collect { data ->
                _uiState.update {
                    it.copy(
                        currentTemperature = data.temperature,
                        currentHumidity = data.humidity
                    )
                }
            }
        }

        historyDataJob = viewModelScope.launch {
            repository.historyData.collect { data ->
                _uiState.update { it.copy(historyData = data) }
            }
        }
    }

    /**
     * 定时轮询当前温湿度数据（每 8 秒刷新一次）
     */
    private fun startPolling() {
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(8_000)
                try {
                    repository.refreshCurrentData()
                    _uiState.update { it.copy(errorMessage = null) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMessage = "数据刷新失败: ${e.message}") }
                }
            }
        }
    }

    /**
     * 重试加载数据（用户点击重试按钮时调用）
     */
    fun retry() {
        _uiState.update { it.copy(isRetrying = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                coroutineScope {
                    launch { loadCurrentData() }
                    launch { loadHistoryData() }
                }
                _uiState.update { it.copy(isRetrying = false, isLoading = false, errorMessage = null, connectionStatus = ConnectionStatus.CONNECTED) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isRetrying = false, errorMessage = "重试失败: ${e.message}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        currentDataJob?.cancel()
        historyDataJob?.cancel()
        networkJob?.cancel()
    }
}
