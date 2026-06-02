package com.myfit.core.ui.component.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.myfit.core.ui.theme.*
import com.myfit.feature.analytics.BarData

@Composable
fun BarChart(
    bars: List<BarData>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 32.dp,
    barColor: (BarData) -> Color = { barValueColor(it.value, it.maxValue) }
) {
    if (bars.isEmpty()) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                "No data yet — log sets to build volume history",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDisabled
            )
        }
        return
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        bars.forEach { bar ->
            BarRow(bar, barHeight, barColor(bar))
        }
    }
}

@Composable
private fun BarRow(bar: BarData, barHeight: Dp, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = bar.label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.width(96.dp)
        )
        Box(modifier = Modifier.weight(1f).height(barHeight)) {
            val fraction = if (bar.maxValue > 0f) (bar.value / bar.maxValue).coerceIn(0f, 1f) else 0f
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(color = Surface2, size = size, cornerRadius = CornerRadius(4.dp.toPx()))
                if (fraction > 0f) {
                    drawRoundRect(
                        color = color,
                        size = Size(size.width * fraction, size.height),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }
            }
            Text(
                text = formatVolume(bar.value),
                style = MaterialTheme.typography.labelSmall,
                color = if (fraction > 0.35f) Black else color,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
            )
        }
    }
}

private fun barValueColor(value: Float, maxValue: Float): Color {
    if (maxValue == 0f) return TextDisabled
    return when (value / maxValue) {
        in 0.75f..2f -> Performance
        in 0.40f..0.75f -> Caution
        else -> Active
    }
}

private fun formatVolume(kg: Float) =
    if (kg >= 1_000f) "${"%.1f".format(kg / 1_000f)}t" else "${kg.toInt()} kg"
