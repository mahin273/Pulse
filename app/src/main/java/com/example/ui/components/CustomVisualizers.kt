package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Live Gauge with Spring Bounce Animation and Neon Glow Effect
 */
@Composable
fun LiveGauge(
    label: String,
    value: Float, // 0 to 100
    color: Color,
    unit: String = "%",
    secondaryLabel: String? = null,
    showCardBackground: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Spring bounce for smooth gauge needle transitions
    val animatedSweep by animateFloatAsState(
        targetValue = (value.coerceIn(0f, 100f) / 100f) * 240f,
        animationSpec = spring(
            dampingRatio = 0.55f, // bouncy feel
            stiffness = Spring.StiffnessLow
        ),
        label = "gauge_sweep"
    )

    // Smooth counter for numerical ticker (200ms tween as requested)
    val animatedValueByCounter by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing),
        label = "counter_value"
    )

    val boxModifier = if (showCardBackground) {
        modifier
            .background(Surface, RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    } else {
        modifier.padding(8.dp)
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = (size.width - strokeWidth) / 2

                    // Base track arc (240 deg starting from 150 deg)
                    drawArc(
                        color = BorderColor,
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(center.x - radius, center.y - radius)
                    )

                    // Active glow layer (Simulated drop shadow glow)
                    drawArc(
                        color = color.copy(alpha = 0.25f),
                        startAngle = 150f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth + 4.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(center.x - radius, center.y - radius)
                    )

                    // Main active arc
                    drawArc(
                        color = color,
                        startAngle = 150f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(center.x - radius, center.y - radius)
                    )
                }

                // Value Display Inside Gauge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.0f", animatedValueByCounter) + unit,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = label.uppercase(),
                        color = TextSecondary,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (secondaryLabel != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = secondaryLabel,
                    color = color.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Real-time Rolling 60s Line Chart with custom linear brushes and indicator dots.
 */
@Composable
fun RollingChart(
    title: String,
    history: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    yMin: Float = 0f,
    yMax: Float = 100f,
    unit: String = "%"
) {
    val displayedHistory = remember(history) {
        if (history.isEmpty()) List(60) { 0f } else history
    }

    Box(
        modifier = modifier
            .background(Surface, RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title.uppercase(),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = String.format("%.1f", displayedHistory.lastOrNull() ?: 0f) + unit,
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
            ) {
                val width = size.width
                val height = size.height
                val pointsCount = displayedHistory.size
                val stepX = width / (pointsCount - 1).coerceAtLeast(1)

                val range = (yMax - yMin).coerceAtLeast(1f)

                // 1. Draw horizontal grid divisions
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = height * i / gridLines
                    drawLine(
                        color = BorderColor.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (pointsCount > 1) {
                    val path = Path()
                    val fillPath = Path()

                    for (i in 0 until pointsCount) {
                        val valPct = (displayedHistory[i] - yMin) / range
                        val x = i * stepX
                        // Canvas coordinate system Y is downwards orienting
                        val y = height - (valPct * height).coerceIn(0f, height)

                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }

                        if (i == pointsCount - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }
                    }

                    // 2. Draw standard grid baseline volumetric shading
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.20f), Color.Transparent),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // 3. Draw main line
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 4. Draw glowing end tracking beacon
                    val lastValPct = (displayedHistory.last() - yMin) / range
                    val endX = width
                    val endY = height - (lastValPct * height).coerceIn(0f, height)

                    drawCircle(
                        color = color,
                        radius = 4.dp.toPx(),
                        center = Offset(endX, endY)
                    )
                    drawCircle(
                        color = color.copy(alpha = 0.35f),
                        radius = 8.dp.toPx(),
                        center = Offset(endX, endY)
                    )
                }
            }
        }
    }
}
