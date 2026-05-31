package com.lizardnest.app.ui.model

/**
 * 首页 UI 状态
 */
data class HomeUiState(
    val currentTemperature: Float = 0f,
    val currentHumidity: Float = 0f,
    val streamUrl: String = "",
    val historyData: List<com.lizardnest.app.data.model.TemperatureHumidity> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTED,
    val isLoading: Boolean = true,
    val loadingMessage: String = "正在连接设备…",
    val errorMessage: String? = null,
    val isRetrying: Boolean = false
)

/**
 * 网络连接状态
 */
enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    RECONNECTING
}
