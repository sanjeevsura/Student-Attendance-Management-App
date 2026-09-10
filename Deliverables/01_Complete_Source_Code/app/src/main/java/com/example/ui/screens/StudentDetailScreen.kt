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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.AttendanceType
import com.example.data.model.UserRole
import com.example.data.repository.AttendanceTier
import com.example.ui.components.DonutProgressRing
import com.example.ui.theme.AttendanceAbsentContainer
import com.example.ui.theme.AttendanceAbsentOnContainer
import com.example.ui.theme.AttendancePresentContainer
import com.example.ui.theme.AttendancePresentOnContainer
import com.example.ui.theme.AttendanceWarningContainer
import com.example.ui.theme.AttendanceWarningOnContainer
import com.example.ui.theme.TrackEduError
import com.example.ui.theme.TrackEduPrimary
import com.example.ui.theme.TrackEduPrimaryContainer
import com.example.ui.theme.TrackEduSecondary
import com.example.ui.theme.TrackEduSecondaryContainer
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AttendanceViewModel

@Composable
fun StudentDetailScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val selectedId by viewModel.selectedStudentId.collectAsState()
    val studentsWithStats by viewModel.studentsWithStats.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val studentStat = studentsWithStats.find { it.student.id == selectedId }

    if (studentStat == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Student record not found")
        }
        return
    }

    val student = studentStat.student
    val studentRecords = allRecords.filter { it.studentId == student.id }
        .sortedByDescending { it.date }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val hasRecords = studentStat.totalSessions > 0
    val percentage = studentStat.attendancePercentage
    val isCritical = studentStat.statusTier == AttendanceTier.CRITICAL
    val isWarning = studentStat.statusTier == AttendanceTier.WARNING

    val tierBg = when {
        !hasRecords -> MaterialTheme.colorScheme.surfaceContainerHigh
        isCritical -> AttendanceAbsentContainer
        isWarning -> AttendanceWarningContainer
        else -> AttendancePresentContainer
    }
    val tierText = when {
        !hasRecords -> MaterialTheme.colorScheme.onSurfaceVariant
        isCritical -> AttendanceAbsentOnContainer
        isWarning -> AttendanceWarningOnContainer
        else -> AttendancePresentOnContainer
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Student Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.EDIT_STUDENT, student.id) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = TrackEduPrimary
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Record",
                            tint = TrackEduError
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCritical) AttendanceAbsentContainer else TrackEduPrimaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.getInitials(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isCritical) AttendanceAbsentOnContainer else Color.White
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Roll Number: ${student.rollNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TrackEduPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${student.department} • ${student.year} (${student.section})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Communication Center: Call Student & Call Parent Buttons
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Direct Communication & Alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Call Student
                            Button(
                                onClick = { viewModel.dialPhone(student.phone, student.name) },
                                colors = ButtonDefaults.buttonColors(containerColor = TrackEduPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Call Student",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            // Call Parent
                            Button(
                                onClick = { viewModel.dialPhone(student.parentPhone, student.parentName) },
                                colors = ButtonDefaults.buttonColors(containerColor = TrackEduSecondary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContactPhone,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Call Parent",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        // SMS Notice to Parent
                        OutlinedButton(
                            onClick = { viewModel.sendSms(student.parentPhone, "${student.name}'s Parent") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = TrackEduPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Dispatch SMS Attendance Notice to Parent (${student.parentPhone})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TrackEduPrimary,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }

            // Attendance Standing & Performance Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Attendance Standing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                color = tierBg,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (hasRecords) studentStat.statusTier.label else "No Records",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = tierText,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DonutProgressRing(
                                percentage = percentage ?: 0.0,
                                size = 96.dp,
                                strokeWidth = 8.5.dp,
                                labelText = if (hasRecords) "RECORDED" else "PENDING"
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatRow(
                                    label = "Total Lectures Held",
                                    value = "${studentStat.totalSessions}",
                                    badgeColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                StatRow(
                                    label = "Classes Attended",
                                    value = "${studentStat.attendedSessions}",
                                    badgeColor = AttendancePresentContainer,
                                    valueColor = AttendancePresentOnContainer
                                )
                                StatRow(
                                    label = "Special Cases (OD/Med)",
                                    value = "${studentStat.specialSessions}",
                                    badgeColor = TrackEduSecondaryContainer,
                                    valueColor = TrackEduSecondary
                                )
                                StatRow(
                                    label = "Classes Missed",
                                    value = "${studentStat.missedSessions}",
                                    badgeColor = if (studentStat.missedSessions > 0) AttendanceAbsentContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    valueColor = if (studentStat.missedSessions > 0) AttendanceAbsentOnContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Formula Note
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = TrackEduPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Attendance Calculation Formula",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TrackEduPrimary
                                    )
                                    Text(
                                        text = if (hasRecords)
                                            "${studentStat.attendedSessions} ÷ ${studentStat.totalSessions} × 100 = ${String.format("%.1f", percentage)}%"
                                        else "No attendance recorded yet for this student.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Attendance Logs Header
            item {
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
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TrackEduPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Attendance Audit Log",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${studentRecords.size} Entries",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (studentRecords.isEmpty()) {
                item {
                    Text(
                        text = "No recorded attendance sessions for this student yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(studentRecords, key = { it.id }) { record ->
                    val isPresent = record.status == AttendanceStatus.PRESENT
                    val isSpecial = record.type != AttendanceType.REGULAR
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 0.5.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = record.subject,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSpecial) {
                                        Surface(
                                            color = TrackEduSecondaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = record.type.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TrackEduSecondary,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${record.date} • ${record.period}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                if (record.note.isNotBlank()) {
                                    Text(
                                        text = record.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isPresent) MaterialTheme.colorScheme.onSurfaceVariant else TrackEduError,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Surface(
                                color = if (isPresent) AttendancePresentContainer else AttendanceAbsentContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPresent) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (isPresent) AttendancePresentOnContainer else AttendanceAbsentOnContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isPresent) "PRESENT" else "ABSENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPresent) AttendancePresentOnContainer else AttendanceAbsentOnContainer,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showDeleteDialog) {
        DeleteStudentDialog(
            student = student,
            onDismiss = { showDeleteDialog = false },
            onConfirmDelete = {
                showDeleteDialog = false
                viewModel.deleteStudent(student.id)
            }
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    badgeColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            color = badgeColor,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

