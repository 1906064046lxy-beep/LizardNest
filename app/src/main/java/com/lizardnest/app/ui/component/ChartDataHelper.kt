package com.lizardnest.app.ui.component

import com.lizardnest.app.data.model.TemperatureHumidity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 图表数据归一化工具
 * 负责将原始温湿度数据映射到 Canvas 坐标空间
 */
object ChartDataHelper {

    /**
     * 为温度数据添加上下边距，使折线图不过分贴近边缘
     */
    fun calculateTemperatureRange(data: List<TemperatureHumidity>): Pair<Float, Float> {
        if (data.isEmpty()) return 20f to 35f
        val min = data.minOf { it.temperature }
        val max = data.maxOf { it.temperature }
        val padding = ((max - min) * 0.1f).coerceAtLeast(0.5f)
        return (min - padding) to (max + padding)
    }

    /**
     * 为湿度数据添加上下边距
     */
    fun calculateHumidityRange(data: List<TemperatureHumidity>): Pair<Float, Float> {
        if (data.isEmpty()) return 40f to 80f
        val min = data.minOf { it.humidity }
        val max = data.maxOf { it.humidity }
        val padding = ((max - min) * 0.1f).coerceAtLeast(0.5f)
        return (min - padding) to (max + padding)
    }

    /**
     * 将温度值映射到 Canvas Y 坐标
     * @param value 温度值
     * @param tempRange 温度范围 (min, max)
     * @param chartHeight 图表区域高度（像素）
     */
    fun mapTemperatureToY(
        value: Float,
        tempRange: Pair<Float, Float>,
        chartHeight: Float
    ): Float {
        val ratio = (value - tempRange.first) / (tempRange.second - tempRange.first)
        return chartHeight * (1f - ratio)  // Canvas Y 轴向下为正
    }

    /**
     * 将湿度值映射到 Canvas Y 坐标
     */
    fun mapHumidityToY(
        value: Float,
        humRange: Pair<Float, Float>,
        chartHeight: Float
    ): Float {
        val ratio = (value - humRange.first) / (humRange.second - humRange.first)
        return chartHeight * (1f - ratio)
    }

    /**
     * 将时间戳映射到 Canvas X 坐标
     * @param timestamp 毫秒时间戳
     * @param timeRange 时间范围 (最早, 最晚)
     * @param chartWidth 图表区域宽度（像素）
     */
    fun mapTimeToX(
        timestamp: Long,
        timeRange: Pair<Long, Long>,
        chartWidth: Float
    ): Float {
        val range = timeRange.second - timeRange.first
        if (range == 0L) return 0f
        val ratio = (timestamp - timeRange.first).toFloat() / range.toFloat()
        return ratio * chartWidth
    }

    /**
     * 获取时间范围
     */
    fun getTimeRange(data: List<TemperatureHumidity>): Pair<Long, Long> {
        if (data.isEmpty()) {
            val now = System.currentTimeMillis()
            return now - 24 * 3600_000L to now
        }
        return data.first().timestamp to data.last().timestamp
    }

    /**
     * 格式化时间标签
     * @param timestamp 毫秒时间戳
     * @param showFullDateTime 是否显示完整日期时间，还是仅显示时间
     */
    fun formatTimeLabel(timestamp: Long, showFullDateTime: Boolean = false): String {
        val format = if (showFullDateTime) {
            SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("HH:mm", Locale.getDefault())
        }
        return format.format(Date(timestamp))
    }

    /**
     * 生成 Y 轴刻度标签
     * @param range 数值范围 (min, max)
     * @param tickCount 刻度数量
     * @return 刻度值列表（从小到大）
     */
    fun generateYTicks(range: Pair<Float, Float>, tickCount: Int = 5): List<Float> {
        val step = (range.second - range.first) / (tickCount - 1)
        return (0 until tickCount).map { range.first + step * it }
    }

    /**
     * 生成 X 轴时间刻度标签
     * @param timeRange 时间范围 (开始, 结束)
     * @param tickCount 刻度数量
     * @return (timestamp, label) 列表
     */
    fun generateXTimeTicks(
        timeRange: Pair<Long, Long>,
        tickCount: Int = 6
    ): List<Pair<Long, String>> {
        val step = (timeRange.second - timeRange.first) / (tickCount - 1)
        return (0 until tickCount).map { i ->
            val ts = timeRange.first + step * i
            ts to formatTimeLabel(ts)
        }
    }
}
