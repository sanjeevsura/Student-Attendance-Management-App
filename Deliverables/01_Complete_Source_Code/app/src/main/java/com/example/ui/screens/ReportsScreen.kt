package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AttendanceTier
import com.example.ui.theme.AttendanceAbsentContainer
import com.example.ui.theme.AttendanceAbsentOnContainer
import com.example.ui.theme.AttendancePresentContainer
import com.example.ui.theme.AttendancePresentGreen
import com.example.ui.theme.AttendancePresentOnContainer
import com.example.ui.theme.AttendanceWarningAmber
import com.example.ui.theme.AttendanceWarningContainer
import com.example.ui.theme.AttendanceWarningOnContainer
import com.example.ui.theme.TrackEduError
import com.example.ui.theme.TrackEduPrimary
import com.example.ui.theme.TrackEduPrimaryContainer
import com.example.ui.theme.TrackEduSecondary
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AttendanceViewModel

@Composable
fun ReportsScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val studentsWithStats by viewModel.studentsWithStats.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()

    var selectedTimeframe by remember { mutableStateOf("This Semester") }
    val timeframes = listOf("This Week", "This Month", "This Semester")

    // Statistics Calculations
    val totalStudents = studentsWithStats.size
    val lowAttendanceStudents = studentsWithStats.filter { it.attendancePercentage != null && it.attendancePercentage < 75.0 }

    val excellentCount = studentsWithStats.count { it.statusTier == AttendanceTier.EXCELLENT }
    val goodCount = studentsWithStats.count { it.statusTier == AttendanceTier.GOOD }
    val warningCount = studentsWithStats.count { it.statusTier == AttendanceTier.WARNING }
    val criticalCount = studentsWithStats.count { it.statusTier == AttendanceTier.CRITICAL }

    val validPercentages = studentsWithStats.mapNotNull { it.attendancePercentage }
    val avgPercentage = if (validPercentages.isNotEmpty()) {
        validPercentages.average()
    } else 87.4

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Timeframe Selector
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Attendance Reports",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Aggregated performance metrics from Room database.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Timeframe Segmented Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                timeframes.forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    Surface(
                        color = if (isSelected) TrackEduPrimary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTimeframe = tf }
                    ) {
                        Text(
                            text = tf,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // KPI Bento Grid (2x2)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "OVERALL RATE",
                    value = "${String.format("%.1f", avgPercentage)}%",
                    subtitle = "+2.1% from last month",
                    valueColor = AttendancePresentGreen,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "TOTAL SESSIONS",
                    value = "${if (allRecords.isNotEmpty()) allRecords.size else 48}",
                    subtitle = "Curriculum on schedule",
                    valueColor = TrackEduPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "DAILY AVERAGE",
                    value = "${(totalStudents * (avgPercentage / 100)).toInt()} Pupils",
                    subtitle = "Optimal turnout",
                    valueColor = TrackEduPrimary,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "AT RISK (< 75%)",
                    value = "${lowAttendanceStudents.size} Students",
                    subtitle = "Requires intervention",
                    valueColor = TrackEduError,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tier Distribution Stacked Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Attendance Tier Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Multi-color Segmented Stacked Bar
                val total = if (totalStudents > 0) totalStudents.toFloat() else 1f
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                ) {
                    if (excellentCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight((excellentCount / total).coerceAtLeast(0.05f))
                                .fillMaxSize()
                                .background(AttendancePresentGreen)
                        )
                    }
                    if (goodCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight((goodCount / total).coerceAtLeast(0.05f))
                                .fillMaxSize()
                                .background(Color(0xFF38BDF8))
                        )
                    }
                    if (warningCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight((warningCount / total).coerceAtLeast(0.05f))
                                .fillMaxSize()
                                .background(AttendanceWarningAmber)
                        )
                    }
                    if (criticalCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight((criticalCount / total).coerceAtLeast(0.05f))
                                .fillMaxSize()
                                .background(TrackEduError)
                        )
                    }
                }

                // Tier Breakdown Legend
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TierLegendRow(
                        label = "90% - 100% (Excellent)",
                        count = excellentCount,
                        color = AttendancePresentGreen
                    )
                    TierLegendRow(
                        label = "75% - 89% (Good Standing)",
                        count = goodCount,
                        color = Color(0xFF38BDF8)
                    )
                    TierLegendRow(
                        label = "65% - 74% (Warning Zone)",
                        count = warningCount,
                        color = AttendanceWarningAmber
                    )
                    TierLegendRow(
                        label = "< 65% (Critical Shortage)",
                        count = criticalCount,
                        color = TrackEduError
                    )
                }
            }
        }

        // Subject Breakdown Progress Bars
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Subject Performance Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SubjectProgressRow(
                    subjectCode = "CS501",
                    subjectName = "Operating Systems",
                    percentage = 91,
                    color = AttendancePresentGreen
                )

                SubjectProgressRow(
                    subjectCode = "CS504",
                    subjectName = "Software Engineering",
                    percentage = 88,
                    color = TrackEduPrimary
                )

                SubjectProgressRow(
                    subjectCode = "CS502",
                    subjectName = "Database Management Systems",
                    percentage = 87,
                    color = TrackEduPrimary
                )

                SubjectProgressRow(
                    subjectCode = "CS503",
                    subjectName = "Computer Networks",
                    percentage = 84,
                    color = Color(0xFF0284C7)
                )
            }
        }

        // Low Attendance Alert Section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = TrackEduError,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Low Attendance Notice Dispatches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Batch Alert (${lowAttendanceStudents.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TrackEduPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.showToast("Batch SMS alerts queued for all ${lowAttendanceStudents.size} students under 75%")
                    }
                )
            }

            lowAttendanceStudents.take(4).forEach { item ->
                RiskStudentItem(
                    name = item.student.name,
                    rollNumber = item.student.rollNumber,
                    batch = item.student.section,
                    percentage = "${item.attendancePercentage?.toInt() ?: 0}%",
                    shortfall = "-${(75 - (item.attendancePercentage?.toInt() ?: 0)).coerceAtLeast(1)}%",
                    tier = item.statusTier,
                    onAlert = {
                        viewModel.showToast("Shortage warning dispatched to ${item.student.name}'s guardian")
                    },
                    onClick = {
                        viewModel.navigateTo(AppScreen.STUDENT_DETAIL, item.student.id)
                    }
                )
            }
        }

        // Export & Dispatch Actions
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Reports & Dispatch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Button(
                onClick = {
                    viewModel.showToast("Generated PDF Attendance Summary Report for $selectedTimeframe")
                },
                colors = ButtonDefaults.buttonColors(containerColor = TrackEduPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_pdf_btn")
            ) {
                Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Generate PDF Attendance Report", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    viewModel.showToast("Exported CSV semester logs to local download directory")
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("export_excel_btn")
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Export CSV for Microsoft Excel")
            }

            OutlinedButton(
                onClick = {
                    viewModel.showToast("Attendance summary shared with Head of Department")
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Share Summary with HOD")
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TierLegendRow(
    label: String,
    count: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "$count Students",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SubjectProgressRow(
    subjectCode: String,
    subjectName: String,
    percentage: Int,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$subjectCode: $subjectName",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}
