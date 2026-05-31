package com.lizardnest.app.data.model

/**
 * 温湿度数据模型
 * @param temperature 温度（摄氏度）
 * @param humidity 湿度（百分比）
 * @param timestamp 数据时间戳（毫秒）
 */
data class TemperatureHumidity(
    val temperature: Float,
    val humidity: Float,
    val timestamp: Long
)
