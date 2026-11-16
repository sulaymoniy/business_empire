package com.businessempire.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Minimal line chart drawn with Canvas (no chart library).
 * [reference] draws a dashed horizontal line, e.g. the player's average cost.
 */
@Composable
fun LineChart(
    points: List<Double>,
    color: Color,
    modifier: Modifier = Modifier,
    reference: Double? = null,
    referenceColor: Color = Color.Gray,
    description: String = "",
    showFill: Boolean = true,
    strokeWidthDp: Float = 2f,
) {
    Canvas(modifier.semantics { contentDescription = description }) {
        if (points.size < 2) return@Canvas
        var min = points.min()
        var max = points.max()
        if (reference != null && reference > 0) {
            min = minOf(min, reference)
            max = maxOf(max, reference)
        }
        if (max - min < 1e-9) {
            max += 1.0
            min -= 1.0
        }
        val pad = (max - min) * 0.08
        min -= pad
        max += pad
        val stepX = size.width / (points.size - 1)
        fun y(v: Double) = (size.height * (1.0 - (v - min) / (max - min))).toFloat()

        val line = Path()
        points.forEachIndexed { i, v ->
            val x = i * stepX
            if (i == 0) line.moveTo(x, y(v)) else line.lineTo(x, y(v))
        }
        if (showFill) {
            val fill = Path().apply {
                addPath(line)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0.02f))))
        }
        drawPath(
            line,
            color = color,
            style = Stroke(width = strokeWidthDp.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        if (reference != null && reference > 0) {
            val ry = y(reference)
            drawLine(
                referenceColor,
                Offset(0f, ry),
                Offset(size.width, ry),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())),
            )
        }
    }
}
