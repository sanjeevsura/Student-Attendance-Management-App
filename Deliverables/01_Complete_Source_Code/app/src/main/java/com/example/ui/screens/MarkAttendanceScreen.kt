package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.AttendanceStatus
import com.example.data.model.Student
import com.example.data.repository.AttendanceTier
import com.example.data.repository.StudentWithStats
import com.example.ui.theme.AttendanceAbsentContainer
import com.example.ui.theme.AttendanceAbsentOnContainer
import com.example.ui.theme.AttendancePresentContainer
import com.example.ui.theme.AttendancePresentGreen
import com.example.ui.theme.AttendancePresentOnContainer
import com.example.ui.theme.AttendanceWarningContainer
import com.example.ui.theme.AttendanceWarningOnContainer
import com.example.ui.theme.TrackEduError
import com.example.ui.theme.TrackEduPrimary
import com.example.ui.theme.TrackEduPrimaryContainer
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val allStudents by viewModel.allStudents.collectAsState()
    val studentsWithStats by viewModel.studentsWithStats.collectAsState()
    val activeAttendance by viewModel.activeSessionAttendance.collectAsState()
    val currentDate by viewModel.selectedDate.collectAsState()
    val currentSubject by viewModel.selectedSubject.collectAsState()
    val currentBatch by viewModel.selectedBatch.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var subjectExpanded by remember { mutableStateOf(false) }

    val subjects = listOf(
        "CS502 - Database Systems",
        "CS501 - Operating Systems",
        "CS503 - Computer Networks",
        "CS504 - Software Engineering"
    )

    val statsMap = remember(studentsWithStats) {
        studentsWithStats.associateBy { it.student.id }
    }

    // Filter students by search
    val filteredStudents = remember(allStudents, searchQuery) {
        allStudents.filter { s ->
            searchQuery.isBlank() ||
                    s.name.contains(searchQuery, ignoreCase = true) ||
                    s.rollNumber.contains(searchQuery, ignoreCase = true)
        }
    }

    // Live counts
    val totalCount = allStudents.size
    val presentCount = allStudents.count {
        (activeAttendance[it.id] ?: AttendanceStatus.PRESENT) == AttendanceStatus.PRESENT
    }
    val absentCount = totalCount - presentCount
    val liveTurnout = if (totalCount > 0) (presentCount.toDouble() / totalCount.toDouble()) * 100.0 else 100.0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Session Context
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Mark Attendance",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentBatch,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Audit Log Button
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(AppScreen.RECORDS) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("audit_records_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TrackEduPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Audit Log",
                                style = MaterialTheme.typography.labelSmall,
                                color = TrackEduPrimary
                            )
                        }
                    }

                    // Date Switcher Ribbon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.shiftDate(-1) },
                            modifier = Modifier.size(32.dp).testTag("date_prev_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Day",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = TrackEduPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = currentDate,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { viewModel.shiftDate(1) },
                            modifier = Modifier.size(32.dp).testTag("date_next_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Day",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Subject Dropdown
                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = !subjectExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currentSubject,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .height(50.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            subjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj, style = MaterialTheme.typography.bodyMedium) },
                                    onClick = {
                                        viewModel.setSubject(subj)
                                        subjectExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Live KPI Summary Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = AttendancePresentContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$presentCount Present",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AttendancePresentOnContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    color = AttendanceAbsentContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$absentCount Absent",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AttendanceAbsentOnContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Turnout:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${liveTurnout.toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (liveTurnout >= 75.0) AttendancePresentGreen else TrackEduError
                                )
                            }
                        }
                    }

                    // Batch Actions (Mark All Present, Reset) + Search
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.markAllPresent() },
                            colors = ButtonDefaults.buttonColors(containerColor = TrackEduPrimaryContainer),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("mark_all_present_btn")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Mark All Present", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetAttendanceDraft() },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("reset_attendance_btn")
                        ) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Reset", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // Quick Search Box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter student roll...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                }
            }

            // Student Roster Stream
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredStudents, key = { it.id }) { student ->
                    val status = activeAttendance[student.id] ?: AttendanceStatus.PRESENT
                    val isPresent = status == AttendanceStatus.PRESENT
                    val studentStat = statsMap[student.id]

                    AttendanceRollItem(
                        student = student,
                        isPresent = isPresent,
                        historicalPercentage = studentStat?.attendancePercentage ?: 85.0,
                        onSetStatus = { newStatus ->
                            viewModel.setStudentAttendanceStatus(student.id, newStatus)
                        }
                    )
                }
            }
        }

        // Pinned Bottom Bar: Save Attendance
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 64.dp)
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Button(
                    onClick = { viewModel.saveActiveAttendance() },
                    colors = ButtonDefaults.buttonColors(containerColor = TrackEduPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_attendance_btn")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Attendance ($presentCount/$totalCount Present)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceRollItem(
    student: Student,
    isPresent: Boolean,
    historicalPercentage: Double,
    onSetStatus: (AttendanceStatus) -> Unit
) {
    val isShortage = historicalPercentage < 75.0

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("roll_item_${student.rollNumber}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.getInitials(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TrackEduPrimary
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        if (isShortage) {
                            Surface(
                                color = AttendanceAbsentContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${historicalPercentage.toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AttendanceAbsentOnContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Roll: ${student.rollNumber} • ${student.section}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // Toggle Pill Buttons: [Present] [Absent]
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present Button
                Surface(
                    color = if (isPresent) AttendancePresentContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(10.dp),
                    border = if (isPresent) androidx.compose.foundation.BorderStroke(1.5.dp, AttendancePresentGreen) else null,
                    modifier = Modifier
                        .clickable { onSetStatus(AttendanceStatus.PRESENT) }
                        .testTag("btn_present_${student.rollNumber}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Present",
                            tint = if (isPresent) AttendancePresentOnContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Present",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isPresent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isPresent) AttendancePresentOnContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Absent Button
                Surface(
                    color = if (!isPresent) AttendanceAbsentContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(10.dp),
                    border = if (!isPresent) androidx.compose.foundation.BorderStroke(1.5.dp, TrackEduError) else null,
                    modifier = Modifier
                        .clickable { onSetStatus(AttendanceStatus.ABSENT) }
                        .testTag("btn_absent_${student.rollNumber}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Absent",
                            tint = if (!isPresent) AttendanceAbsentOnContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Absent",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (!isPresent) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isPresent) AttendanceAbsentOnContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
