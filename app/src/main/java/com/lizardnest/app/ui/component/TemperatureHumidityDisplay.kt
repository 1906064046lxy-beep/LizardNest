package com.lizardnest.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 温湿度实时数值显示组件
 * 横向排列温度和湿度两张卡片，大字体展示实时数值
 */
@Composable
fun TemperatureHumidityDisplay(
    temperature: Float,
    humidity: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 温度卡片
        DataCard(
            value = String.format("%.1f", temperature),
            unit = "℃",
            label = "温度",
            icon = Icons.Filled.Thermostat,
            iconTint = Color(0xFFE53935),   // 红色
            valueColor = Color(0xFFE53935),
            modifier = Modifier.weight(1f)
        )

        // 湿度卡片
        DataCard(
            value = String.format("%.1f", humidity),
            unit = "%",
            label = "湿度",
            icon = Icons.Filled.WaterDrop,
            iconTint = Color(0xFF1E88E5),   // 蓝色
            valueColor = Color(0xFF1E88E5),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DataCard(
    value: String,
    unit: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图标
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f))
                    .padding(6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 数值 + 单位
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
                Text(
                    text = unit,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = valueColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 标签
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
