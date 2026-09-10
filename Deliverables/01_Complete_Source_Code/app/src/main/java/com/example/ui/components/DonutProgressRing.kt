package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AttendancePresentGreen
import com.example.ui.theme.AttendanceWarningAmber
import com.example.ui.theme.TrackEduError
import com.example.ui.theme.TrackEduPrimary

@Composable
fun DonutProgressRing(
    percentage: Double,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    strokeWidth: Dp = 8.dp,
    showLabel: Boolean = true,
    labelText: String = "TURN-OUT",
    trackColor: Color = Color(0xFFE2E7FF),
    progressColor: Color? = null
) {
    val activeColor = progressColor ?: when {
        percentage >= 80.0 -> AttendancePresentGreen
        percentage >= 75.0 -> AttendanceWarningAmber
        else -> TrackEduError
    }

    val sweepAngle = ((percentage.coerceIn(0.0, 100.0) / 100.0) * 360f).toFloat()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            // Background track circle
            drawCircle(
                color = trackColor,
                style = Stroke(width = strokePx)
            )

            // Progress arc
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        if (showLabel) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${percentage.toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
                if (labelText.isNotEmpty()) {
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
