package com.lizardnest.app.ui.component

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.pow

/**
 * 视频重连状态
 */
enum class ReconnectState {
    IDLE,
    RECONNECTING,
    FAILED
}

/**
 * 视频自动重连管理器
 * 监听 ExoPlayer 错误回调，使用指数退避策略自动重连
 * 最多重试 MAX_ATTEMPTS 次，每次间隔 baseDelay * 2^(attempt-1)
 */
class VideoReconnectionManager(
    private val player: ExoPlayer,
    private val streamUrl: String,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _reconnectState = MutableStateFlow(ReconnectState.IDLE)
    val reconnectState: StateFlow<ReconnectState> = _reconnectState.asStateFlow()

    private var reconnectJob: Job? = null
    private var attemptCount = 0
    private var isActive = true

    init {
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.w(TAG, "播放器错误: ${error.message}, errorCode=${error.errorCodeName}")
                if (shouldRetry(error)) {
                    scheduleReconnect()
                } else {
                    _reconnectState.value = ReconnectState.FAILED
                }
            }
        })
    }

    /**
     * 判断错误是否应该重试（网络相关错误重试，格式错误不重试）
     */
    private fun shouldRetry(error: PlaybackException): Boolean {
        return when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
            PlaybackException.ERROR_CODE_TIMEOUT -> true
            else -> false
        }
    }

    /**
     * 使用指数退避策略调度重连
     */
    fun scheduleReconnect() {
        if (!isActive) return
        if (_reconnectState.value == ReconnectState.RECONNECTING) return

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            attemptCount = 0
            _reconnectState.value = ReconnectState.RECONNECTING

            while (attemptCount < MAX_ATTEMPTS && isActive) {
                attemptCount++
                val backoffMs = min(
                    BASE_DELAY_MS * 2.0.pow(attemptCount - 1).toLong(),
                    MAX_DELAY_MS
                )

                Log.d(TAG, "重连尝试 $attemptCount/$MAX_ATTEMPTS，等待 ${backoffMs}ms")
                delay(backoffMs)

                try {
                    player.apply {
                        setMediaItem(MediaItem.fromUri(streamUrl))
                        prepare()
                        playWhenReady = true
                    }
                    _reconnectState.value = ReconnectState.IDLE
                    Log.d(TAG, "重连成功")
                    return@launch
                } catch (e: Exception) {
                    Log.w(TAG, "重连尝试 $attemptCount 失败: ${e.message}")
                }
            }

            if (attemptCount >= MAX_ATTEMPTS) {
                _reconnectState.value = ReconnectState.FAILED
                Log.w(TAG, "重连失败，已达最大重试次数 $MAX_ATTEMPTS")
            }
        }
    }

    /**
     * 当网络恢复时触发重新连接
     */
    fun onNetworkRestored() {
        if (_reconnectState.value == ReconnectState.FAILED) {
            attemptCount = 0
            scheduleReconnect()
        }
    }

    /**
     * 手动重试
     */
    fun retry() {
        attemptCount = 0
        scheduleReconnect()
    }

    /**
     * 释放资源
     */
    fun release() {
        isActive = false
        reconnectJob?.cancel()
    }

    companion object {
        private const val TAG = "VideoReconnect"
        private const val MAX_ATTEMPTS = 5
        private const val BASE_DELAY_MS = 1000L
        private const val MAX_DELAY_MS = 30000L
    }
}
