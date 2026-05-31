package com.lizardnest.app.data.datasource.mock

import com.lizardnest.app.data.datasource.EnvironmentDataSource
import com.lizardnest.app.data.model.TemperatureHumidity
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Mock 温湿度数据源
 * 生成随机但合理范围内的温湿度数据，模拟真实传感器行为
 */
class MockEnvironmentDataSource : EnvironmentDataSource {

    private var lastTemperature: Float = 28.0f
    private var lastHumidity: Float = 65.0f

    override suspend fun getCurrentData(): TemperatureHumidity {
        // 模拟网络延迟
        delay(200)

        // 在上一值基础上平滑变化，避免数值跳跃太大
        lastTemperature = smoothChange(lastTemperature, 22f, 33f, 0.5f)
        lastHumidity = smoothChange(lastHumidity, 45f, 75f, 1.5f)

        return TemperatureHumidity(
            temperature = roundToOneDecimal(lastTemperature),
            humidity = roundToOneDecimal(lastHumidity),
            timestamp = System.currentTimeMillis()
        )
    }

    override suspend fun getHistoryData(hours: Int): List<TemperatureHumidity> {
        delay(300)

        val now = System.currentTimeMillis()
        val intervalMs = hours * 3600_000L / MAX_DATA_POINTS
        val dataPoints = mutableListOf<TemperatureHumidity>()

        var temp = 28.0f
        var hum = 65.0f

        for (i in MAX_DATA_POINTS downTo 1) {
            temp = smoothChange(temp, 22f, 33f, 0.3f)
            hum = smoothChange(hum, 45f, 75f, 0.8f)

            dataPoints.add(
                TemperatureHumidity(
                    temperature = roundToOneDecimal(temp),
                    humidity = roundToOneDecimal(hum),
                    timestamp = now - i * intervalMs
                )
            )
        }

        return dataPoints
    }

    /**
     * 平滑变化：在当前值基础上添加小幅随机变化，并限制在合理范围内
     */
    private fun smoothChange(current: Float, min: Float, max: Float, maxStep: Float): Float {
        val change = Random.nextFloat() * maxStep * 2 - maxStep
        return (current + change).coerceIn(min, max)
    }

    /**
     * 保留一位小数
     */
    private fun roundToOneDecimal(value: Float): Float {
        return (value * 10).toInt() / 10f
    }

    companion object {
        private const val MAX_DATA_POINTS = 1440 // 24h * 60min
    }
}
