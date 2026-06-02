package com.myfit.core.ui.component.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.myfit.core.ui.theme.*
import com.myfit.feature.analytics.LinePoint

@Composable
fun LineChart(
    points: List<LinePoint>,
    lineColor: Color = Active,
    modifier: Modifier = Modifier,
    yLabelFormat: (Float) -> String = { "%.1f".format(it) }
) {
    if (points.size < 2) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                "Log more sessions to see trends",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDisabled
            )
        }
        return
    }

    val sorted  = points.sortedBy { it.xMs }
    val minVal  = sorted.minOf { it.value }
    val maxVal  = sorted.maxOf { it.value }
    val range   = (maxVal - minVal).coerceAtLeast(1f)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val w = size.width
            val h = size.height
            val dotR  = 5.dp.toPx()
            val n     = sorted.size

            fun xOf(i: Int) = if (n == 1) w / 2f else i.toFloat() / (n - 1) * w
            fun yOf(v: Float) = h - ((v - minVal) / range * (h - dotR * 2)) - dotR

            // Mid gridline
            drawLine(
                color = Border,
                start = Offset(0f, yOf((minVal + maxVal) / 2f)),
                end   = Offset(w,  yOf((minVal + maxVal) / 2f)),
                strokeWidth = 1.dp.toPx()
            )

            // Line path
            val path = Path()
            sorted.forEachIndexed { i, pt ->
                val x = xOf(i); val y = yOf(pt.value)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path,
                color = lineColor.copy(alpha = 0.7f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Dots — outer filled, inner black
            sorted.forEachIndexed { i, pt ->
                val c = Offset(xOf(i), yOf(pt.value))
                drawCircle(color = lineColor, radius = dotR, center = c)
                drawCircle(color = Black, radius = dotR - 2.dp.toPx(), center = c)
            }
        }

        // X-axis: first, mid, last labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val mid = sorted.getOrNull(sorted.size / 2)
            listOfNotNull(sorted.first(), mid, sorted.last()).forEach { pt ->
                Text(pt.xLabel, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }

        // Y range
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(yLabelFormat(minVal), style = MaterialTheme.typography.labelSmall, color = TextDisabled)
            Text(yLabelFormat(maxVal), style = MaterialTheme.typography.labelSmall, color = TextDisabled)
        }
    }
}
