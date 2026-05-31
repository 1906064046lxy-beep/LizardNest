package com.lizardnest.app.data.repository

import com.lizardnest.app.data.datasource.EnvironmentDataSource
import com.lizardnest.app.data.datasource.VideoStreamDataSource
import com.lizardnest.app.data.model.TemperatureHumidity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据仓库：统一管理温湿度和视频流数据
 */
@Singleton
class NestRepository @Inject constructor(
    private val environmentDataSource: EnvironmentDataSource,
    private val videoStreamDataSource: VideoStreamDataSource
) {
    private val _currentData = MutableStateFlow(TemperatureHumidity(0f, 0f, 0L))
    val currentData: Flow<TemperatureHumidity> = _currentData.asStateFlow()

    private val _historyData = MutableStateFlow<List<TemperatureHumidity>>(emptyList())
    val historyData: Flow<List<TemperatureHumidity>> = _historyData.asStateFlow()

    /**
     * 刷新当前温湿度数据
     */
    suspend fun refreshCurrentData() {
        val data = environmentDataSource.getCurrentData()
        _currentData.value = data
    }

    /**
     * 加载历史温湿度数据
     * @param hours 查询最近多少小时的数据
     */
    suspend fun loadHistoryData(hours: Int = 24) {
        val data = environmentDataSource.getHistoryData(hours)
        _historyData.value = data
    }

    /**
     * 获取视频流地址
     */
    fun getStreamUrl(): String {
        return videoStreamDataSource.getStreamUrl()
    }
}
