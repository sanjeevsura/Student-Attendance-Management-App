package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun StudentsRosterScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val studentsWithStats by viewModel.studentsWithStats.collectAsState()
    val searchQuery by viewModel.studentSearchQuery.collectAsState()
    val filterTab by viewModel.studentFilterTab.collectAsState()

    var studentToDelete by remember { mutableStateOf<StudentWithStats?>(null) }

    // Filter and search logic
    val filteredStudents = remember(studentsWithStats, searchQuery, filterTab) {
        studentsWithStats.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.student.name.contains(searchQuery, ignoreCase = true) ||
                    item.student.rollNumber.contains(searchQuery, ignoreCase = true) ||
                    item.student.department.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (filterTab) {
                "Low < 75%" -> item.attendancePercentage != null && item.attendancePercentage < 75.0
                "Good >= 75%" -> item.attendancePercentage != null && item.attendancePercentage >= 75.0
                "CSE - III A" -> item.student.section.contains("A", ignoreCase = true)
                "CSE - III B" -> item.student.section.contains("B", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    val lowCount = studentsWithStats.count { it.attendancePercentage != null && it.attendancePercentage < 75.0 }
    val goodCount = studentsWithStats.size - lowCount

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with title and count
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Student Directory",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${studentsWithStats.size} Registered Students • Room Database",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Register button
                    Surface(
                        color = TrackEduPrimaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(AppScreen.ADD_STUDENT) }
                            .testTag("add_student_header_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Add",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.studentSearchQuery.value = it },
                    placeholder = { Text("Search by student name or roll number...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.studentSearchQuery.value = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedBorderColor = TrackEduPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_search_input")
                )

                // Filter Tabs Scrollable
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        label = "All (${studentsWithStats.size})",
                        isSelected = filterTab == "All",
                        onClick = { viewModel.studentFilterTab.value = "All" },
                        testTag = "filter_all"
                    )
                    FilterChip(
                        label = "Low < 75% ($lowCount)",
                        isSelected = filterTab == "Low < 75%",
                        isAlert = true,
                        onClick = { viewModel.studentFilterTab.value = "Low < 75%" },
                        testTag = "filter_low"
                    )
                    FilterChip(
                        label = "Good >= 75% ($goodCount)",
                        isSelected = filterTab == "Good >= 75%",
                        onClick = { viewModel.studentFilterTab.value = "Good >= 75%" },
                        testTag = "filter_good"
                    )
                    FilterChip(
                        label = "CSE - III A",
                        isSelected = filterTab == "CSE - III A",
                        onClick = { viewModel.studentFilterTab.value = "CSE - III A" },
                        testTag = "filter_sec_a"
                    )
                    FilterChip(
                        label = "CSE - III B",
                        isSelected = filterTab == "CSE - III B",
                        onClick = { viewModel.studentFilterTab.value = "CSE - III B" },
                        testTag = "filter_sec_b"
                    )
                }
            }

            // Student Roster List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No students match your filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try adjusting your search query or filter tags.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredStudents, key = { it.student.id }) { item ->
                        StudentCardItem(
                            item = item,
                            onViewProfile = {
                                viewModel.navigateTo(AppScreen.STUDENT_DETAIL, item.student.id)
                            },
                            onEdit = {
                                viewModel.navigateTo(AppScreen.EDIT_STUDENT, item.student.id)
                            },
                            onDelete = {
                                studentToDelete = item
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Student
        FloatingActionButton(
            onClick = { viewModel.navigateTo(AppScreen.ADD_STUDENT) },
            containerColor = TrackEduPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 16.dp)
                .testTag("fab_add_student")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Student",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Delete Confirmation Dialog
    studentToDelete?.let { target ->
        DeleteStudentDialog(
            student = target.student,
            onDismiss = { studentToDelete = null },
            onConfirmDelete = {
                viewModel.deleteStudent(target.student.id)
                studentToDelete = null
            }
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    isAlert: Boolean = false,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val bgColor = when {
        isSelected && isAlert -> AttendanceAbsentContainer
        isSelected -> TrackEduPrimaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val textColor = when {
        isSelected && isAlert -> AttendanceAbsentOnContainer
        isSelected -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun StudentCardItem(
    item: StudentWithStats,
    onViewProfile: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    val percentageInt = item.attendancePercentage?.toInt() ?: 0
    val isCritical = item.statusTier == AttendanceTier.CRITICAL
    val isWarning = item.statusTier == AttendanceTier.WARNING

    val badgeBg = when {
        isCritical -> AttendanceAbsentContainer
        isWarning -> AttendanceWarningContainer
        else -> AttendancePresentContainer
    }
    val badgeTextColor = when {
        isCritical -> AttendanceAbsentOnContainer
        isWarning -> AttendanceWarningOnContainer
        else -> AttendancePresentOnContainer
    }

    val progressColor = when {
        isCritical -> TrackEduError
        isWarning -> Color(0xFFD97706)
        else -> AttendancePresentGreen
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewProfile() }
            .testTag("student_card_${item.student.rollNumber}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top row: Avatar + Name/Roll + Attendance % Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar circle with initials
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCritical) AttendanceAbsentContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.student.getInitials(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCritical) AttendanceAbsentOnContainer else TrackEduPrimary
                        )
                    }

                    Column {
                        Text(
                            text = item.student.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Roll No: ${item.student.rollNumber} • ${item.student.section}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Attendance % Badge
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$percentageInt%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { ((item.attendancePercentage ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.attendedSessions} Attended / ${item.totalSessions} Total Sessions",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.statusTier.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = progressColor
                    )
                }
            }

            // Bottom Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call Guardian quick button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (item.student.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.student.phone}"))
                                context.startActivity(intent)
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call Guardian",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (item.student.phone.isNotBlank()) item.student.phone else "Call Guardian",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // View Details / More options
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onViewProfile() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TrackEduPrimary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = TrackEduPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Student Details") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Student", color = TrackEduError) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = TrackEduError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
