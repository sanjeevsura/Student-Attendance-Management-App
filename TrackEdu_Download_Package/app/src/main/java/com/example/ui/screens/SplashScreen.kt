package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TrackEduPrimary
import com.example.ui.theme.TrackEduSecondary
import com.example.viewmodel.AttendanceViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val logoScale = remember { Animatable(0.5f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val progressAlpha = remember { Animatable(0f) }

    // Pulse animation for subtle academic progress dots
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotPulse"
    )

    LaunchedEffect(Unit) {
        // Step 1 & 2: Logo fades and scales in
        logoAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        logoScale.animateTo(1.05f, tween(350, easing = FastOutSlowInEasing))
        logoScale.animateTo(1.0f, tween(200, easing = FastOutSlowInEasing))

        // Step 3: Title appears
        titleAlpha.animateTo(1f, tween(300))

        // Step 4: Subtitle appears
        subtitleAlpha.animateTo(1f, tween(300))

        // Step 5: Tagline appears
        taglineAlpha.animateTo(1f, tween(300))

        // Step 6: Progress indicator fades in
        progressAlpha.animateTo(1f, tween(250))

        // Target duration: ~1.8 - 2.0 seconds total
        delay(400)

        // Step 7: Check local session and transition to correct destination
        viewModel.checkStartupSession()
    }

    val isDark by viewModel.isDarkTheme.collectAsState()

    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF00174F),
                Color(0xFF00236F),
                Color(0xFF0B1B3D)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF8FAFC),
                Color(0xFFEFF6FF)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated Brand Logo with Stitch-styled gradient & elevation shadow
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "TrackEdu Logo",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Title
            Text(
                text = "TrackEdu",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else Color(0xFF1E3A8A),
                letterSpacing = 1.sp,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
                text = "Student Attendance Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFF93C5FD) else Color(0xFF0D9488),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tagline
            Text(
                text = "Smarter attendance. Better academic tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Animated Loading / Progress Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(progressAlpha.value)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(dotScale)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB))
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8))
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(dotScale)
                        .clip(CircleShape)
                        .background(Color(0xFF14B8A6))
                )
            }
        }

        // Bottom version watermark
        Text(
            text = "TrackEdu v2.4 • Offline Suite",
            style = MaterialTheme.typography.labelSmall,
            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .alpha(taglineAlpha.value)
        )
    }
}
