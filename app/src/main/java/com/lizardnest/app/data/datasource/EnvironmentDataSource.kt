package com.lizardnest.app.data.datasource

import com.lizardnest.app.data.model.TemperatureHumidity
import kotlinx.coroutines.flow.Flow

/**
 * 温湿度数据源接口
 * MVP 阶段使用 Mock 实现，后续切换为真实硬件通信
 */
interface EnvironmentDataSource {

    /**
     * 获取当前温湿度数据
     */
    suspend fun getCurrentData(): TemperatureHumidity

    /**
     * 获取历史温湿度数据
     * @param hours 查询最近多少小时的数据
     * @return 按时间升序排列的历史数据列表
     */
    suspend fun getHistoryData(hours: Int = 24): List<TemperatureHumidity>
}
