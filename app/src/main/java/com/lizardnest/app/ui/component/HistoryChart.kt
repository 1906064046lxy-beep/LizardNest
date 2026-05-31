package com.lizardnest.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lizardnest.app.data.model.TemperatureHumidity

// 图表颜色定义
private val TEMPERATURE_COLOR = Color(0xFFE53935)
private val HUMIDITY_COLOR = Color(0xFF1E88E5)
private val GRID_COLOR = Color(0xFFE0E0E0)
private val AXIS_COLOR = Color(0xFFBDBDBD)
private val TEXT_COLOR = Color(0xFF757575)
private val CROSSHAIR_COLOR = Color(0xFF424242)

/**
 * 24 小时温湿度历史曲线图
 * 使用 Compose Canvas 原生绘制双 Y 轴折线图
 * 支持横向滑动和触摸指示线
 */
@Composable
fun HistoryChart(
    data: List<TemperatureHumidity>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "暂无历史数据",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // 触摸指示线位置（-1 表示不显示）
    var touchX by remember { mutableFloatStateOf(-1f) }
    var touchDataPoint by remember { mutableStateOf<TemperatureHumidity?>(null) }

    // 图表偏移量（用于横向滑动）
    var scrollOffset by remember { mutableFloatStateOf(0f) }

    // 数据预处理
    val tempRange = remember(data) { ChartDataHelper.calculateTemperatureRange(data) }
    val humRange = remember(data) { ChartDataHelper.calculateHumidityRange(data) }
    val timeRange = remember(data) { ChartDataHelper.getTimeRange(data) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 标题行
            Text(
                text = "24 小时温湿度曲线",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 图表 Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .pointerInput(data) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                touchX = -1f
                                touchDataPoint = null
                            }
                        ) { _, dragAmount ->
                            touchX = (touchX + dragAmount).coerceIn(0f, size.width)
                            // 查找最接近的数据点
                            val ratio = touchX / size.width
                            val idx = (ratio * (data.size - 1)).toInt()
                                .coerceIn(0, data.size - 1)
                            touchDataPoint = data[idx]
                        }
                    }
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            touchX = offset.x.coerceIn(0f, size.width)
                            val ratio = touchX / size.width
                            val idx = (ratio * (data.size - 1)).toInt()
                                .coerceIn(0, data.size - 1)
                            touchDataPoint = data[idx]
                        }
                    }
            ) {
                val paddingLeft = 52f
                val paddingRight = 52f
                val paddingTop = 16f
                val paddingBottom = 36f

                val chartWidth = size.width - paddingLeft - paddingRight
                val chartHeight = size.height - paddingTop - paddingBottom

                if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

                // === 绘制网格 ===
                drawGridLines(
                    paddingLeft, paddingRight, paddingTop, paddingBottom,
                    chartWidth, chartHeight, tempRange, humRange
                )

                // === 绘制温度折线 ===
                drawDataLine(
                    data = data,
                    mapper = { d -> ChartDataHelper.mapTimeToX(d.timestamp, timeRange, chartWidth) to
                                   ChartDataHelper.mapTemperatureToY(d.temperature, tempRange, chartHeight) },
                    offsetX = paddingLeft,
                    offsetY = paddingTop,
                    color = TEMPERATURE_COLOR
                )

                // === 绘制湿度折线 ===
                drawDataLine(
                    data = data,
                    mapper = { d -> ChartDataHelper.mapTimeToX(d.timestamp, timeRange, chartWidth) to
                                   ChartDataHelper.mapHumidityToY(d.humidity, humRange, chartHeight) },
                    offsetX = paddingLeft,
                    offsetY = paddingTop,
                    color = HUMIDITY_COLOR
                )

                // === 绘制 Y 轴标签 ===
                drawYAxisLabels(
                    paddingLeft, paddingTop,
                    chartHeight, tempRange, humRange
                )

                // === 绘制 X 轴时间标签 ===
                drawXAxisLabels(
                    data, timeRange, chartWidth,
                    paddingLeft, paddingTop, paddingBottom, chartHeight
                )

                // === 绘制触摸指示线 ===
                if (touchX >= 0 && touchDataPoint != null) {
                    drawCrosshair(
                        touchX, paddingTop, chartHeight + paddingTop,
                        touchDataPoint!!, tempRange, humRange,
                        timeRange, chartHeight
                    )
                }

                // === 绘制图例 ===
                drawLegend(paddingLeft)
            }
        }
    }
}

/**
 * 绘制网格线
 */
private fun DrawScope.drawGridLines(
    paddingLeft: Float, paddingRight: Float,
    paddingTop: Float, paddingBottom: Float,
    chartWidth: Float, chartHeight: Float,
    tempRange: Pair<Float, Float>,
    humRange: Pair<Float, Float>
) {
    val ticks = ChartDataHelper.generateYTicks(tempRange)

    ticks.forEach { tick ->
        val y = ChartDataHelper.mapTemperatureToY(tick, tempRange, chartHeight) + paddingTop
        drawLine(
            color = GRID_COLOR,
            start = Offset(paddingLeft, y),
            end = Offset(paddingLeft + chartWidth, y),
            strokeWidth = 1f
        )
    }

    // X 轴和 Y 轴线
    drawLine(AXIS_COLOR, Offset(paddingLeft, paddingTop), Offset(paddingLeft, paddingTop + chartHeight), 1.5f)
    drawLine(AXIS_COLOR, Offset(paddingLeft, paddingTop + chartHeight), Offset(paddingLeft + chartWidth, paddingTop + chartHeight), 1.5f)
    drawLine(AXIS_COLOR, Offset(paddingLeft + chartWidth, paddingTop), Offset(paddingLeft + chartWidth, paddingTop + chartHeight), 1.5f)
}

/**
 * 绘制折线
 */
private fun DrawScope.drawDataLine(
    data: List<TemperatureHumidity>,
    mapper: (TemperatureHumidity) -> Pair<Float, Float>,
    offsetX: Float,
    offsetY: Float,
    color: Color
) {
    if (data.isEmpty()) return

    val path = Path()
    var isFirst = true

    data.forEach { point ->
        val (x, y) = mapper(point)
        val drawX = x + offsetX
        val drawY = y + offsetY

        if (isFirst) {
            path.moveTo(drawX, drawY)
            isFirst = false
        } else {
            path.lineTo(drawX, drawY)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 2.5f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * 绘制 Y 轴标签（左：温度，右：湿度）
 */
private fun DrawScope.drawYAxisLabels(
    paddingLeft: Float, paddingTop: Float,
    chartHeight: Float,
    tempRange: Pair<Float, Float>,
    humRange: Pair<Float, Float>
) {
    val tempTicks = ChartDataHelper.generateYTicks(tempRange, 5)
    val humTicks = ChartDataHelper.generateYTicks(humRange, 5)

    val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#757575")
        textSize = 28f
        isAntiAlias = true
    }

    // 左 Y 轴（温度）
    tempTicks.forEach { tick ->
        val y = ChartDataHelper.mapTemperatureToY(tick, tempRange, chartHeight) + paddingTop
        drawContext.canvas.nativeCanvas.drawText(
            "${String.format("%.0f", tick)}℃",
            4f, y + 8f, textPaint
        )
    }

    // 右 Y 轴（湿度）
    textPaint.textAlign = android.graphics.Paint.Align.RIGHT
    humTicks.forEach { tick ->
        val y = ChartDataHelper.mapHumidityToY(tick, humRange, chartHeight) + paddingTop
        drawContext.canvas.nativeCanvas.drawText(
            "${String.format("%.0f", tick)}%",
            size.width - 4f, y + 8f, textPaint
        )
    }
}

/**
 * 绘制 X 轴时间标签
 */
private fun DrawScope.drawXAxisLabels(
    data: List<TemperatureHumidity>,
    timeRange: Pair<Long, Long>,
    chartWidth: Float,
    paddingLeft: Float,
    paddingTop: Float,
    paddingBottom: Float,
    chartHeight: Float
) {
    val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#757575")
        textSize = 26f
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val ticks = ChartDataHelper.generateXTimeTicks(timeRange)

    ticks.forEach { (timestamp, label) ->
        val x = ChartDataHelper.mapTimeToX(timestamp, timeRange, chartWidth) + paddingLeft
        val y = paddingTop + chartHeight + paddingBottom - 4f
        drawContext.canvas.nativeCanvas.drawText(label, x, y, textPaint)
    }
}

/**
 * 绘制触摸十字指示线
 */
private fun DrawScope.drawCrosshair(
    touchX: Float,
    topY: Float,
    bottomY: Float,
    dataPoint: TemperatureHumidity,
    tempRange: Pair<Float, Float>,
    humRange: Pair<Float, Float>,
    timeRange: Pair<Long, Long>,
    chartHeight: Float
) {
    // 竖线
    drawLine(
        color = CROSSHAIR_COLOR,
        start = Offset(touchX, topY),
        end = Offset(touchX, bottomY),
        strokeWidth = 1f
    )

    // 温度点
    val tempY = ChartDataHelper.mapTemperatureToY(dataPoint.temperature, tempRange, chartHeight) + topY
    drawCircle(TEMPERATURE_COLOR, 6f, Offset(touchX, tempY))
    drawCircle(Color.White, 3f, Offset(touchX, tempY))

    // 湿度点
    val humY = ChartDataHelper.mapHumidityToY(dataPoint.humidity, humRange, chartHeight) + topY
    drawCircle(HUMIDITY_COLOR, 6f, Offset(touchX, humY))
    drawCircle(Color.White, 3f, Offset(touchX, humY))

    // 数据提示文字
    val tipPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 30f
        isAntiAlias = true
        isFakeBoldText = true
    }

    val bgPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#424242")
        isAntiAlias = true
    }

    val timeStr = ChartDataHelper.formatTimeLabel(dataPoint.timestamp)
    val tipText = "${timeStr}  温度:${String.format("%.1f", dataPoint.temperature)}℃  湿度:${String.format("%.1f", dataPoint.humidity)}%"
    val textWidth = tipPaint.measureText(tipText)

    val tipX = if (touchX + textWidth / 2 + 8 > size.width) size.width - textWidth / 2 - 8
                else if (touchX - textWidth / 2 - 8 < 0) textWidth / 2 + 8
                else touchX

    drawContext.canvas.nativeCanvas.apply {
        drawRoundRect(
            tipX - textWidth / 2 - 10, topY + 2f,
            tipX + textWidth / 2 + 10, topY + 34f,
            6f, 6f, bgPaint
        )
        drawText(tipText, tipX - textWidth / 2, topY + 24f, tipPaint)
    }
}

/**
 * 绘制图例
 */
private fun DrawScope.drawLegend(startX: Float) {
    // 温度图例
    drawCircle(TEMPERATURE_COLOR, 5f, Offset(startX + 5f, 6f))
    val legendPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#757575")
        textSize = 26f
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText("温度(℃)", startX + 14f, 14f, legendPaint)

    // 湿度图例
    drawCircle(HUMIDITY_COLOR, 5f, Offset(startX + 90f, 6f))
    drawContext.canvas.nativeCanvas.drawText("湿度(%)", startX + 99f, 14f, legendPaint)
}
