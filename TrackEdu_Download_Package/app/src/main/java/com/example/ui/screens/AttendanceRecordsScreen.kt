package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.ui.components.DonutProgressRing
import com.example.ui.theme.AttendanceAbsentContainer
import com.example.ui.theme.AttendanceAbsentOnContainer
import com.example.ui.theme.AttendancePresentContainer
import com.example.ui.theme.AttendancePresentGreen
import com.example.ui.theme.AttendancePresentOnContainer
import com.example.ui.theme.TrackEduError
import com.example.ui.theme.TrackEduPrimary
import com.example.ui.theme.TrackEduPrimaryContainer
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AttendanceViewModel

@Composable
fun AttendanceRecordsScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val currentDate by viewModel.selectedDate.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("All") } // "All", "Present", "Absent"

    val studentsMap = remember(allStudents) {
        allStudents.associateBy { it.id }
    }

    val recordsForDate = remember(allRecords, currentDate) {
        allRecords.filter { it.date == currentDate }
    }

    val filteredRecords = remember(recordsForDate, searchQuery, filterStatus, studentsMap) {
        recordsForDate.filter { record ->
            val student = studentsMap[record.studentId]
            val matchesSearch = searchQuery.isBlank() ||
                    (student != null && (student.name.contains(searchQuery, ignoreCase = true) || student.rollNumber.contains(searchQuery, ignoreCase = true)))

            val matchesFilter = when (filterStatus) {
                "Present" -> record.status == AttendanceStatus.PRESENT
                "Absent" -> record.status == AttendanceStatus.ABSENT
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    val totalRecords = recordsForDate.size
    val presentRecords = recordsForDate.count { it.status == AttendanceStatus.PRESENT }
    val absentRecords = totalRecords - presentRecords
    val turnOut = if (totalRecords > 0) (presentRecords.toDouble() / totalRecords.toDouble()) * 100.0 else 0.0

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
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("records_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Attendance Records",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.showToast("Exported day sheet ($currentDate.csv) to device storage")
                    },
                    modifier = Modifier.testTag("export_csv_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export CSV",
                        tint = TrackEduPrimary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Date Switcher
            item {
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
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Day")
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
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Day")
                    }
                }
            }

            // Session Overview Bento Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "SESSION SUMMARY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TrackEduPrimary
                            )
                            Text(
                                text = "CS502 - Database Systems",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Period 2 (10:15 - 11:15 AM) • Section A",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Surface(
                                    color = AttendancePresentContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$presentRecords Present",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AttendancePresentOnContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    color = AttendanceAbsentContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$absentRecords Absent",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AttendanceAbsentOnContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        DonutProgressRing(
                            percentage = turnOut,
                            size = 80.dp,
                            strokeWidth = 7.dp,
                            labelText = "ATTENDANCE"
                        )
                    }
                }
            }

            // Search & Filter Tabs
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by student name or roll...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
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

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            label = "All ($totalRecords)",
                            isSelected = filterStatus == "All",
                            onClick = { filterStatus = "All" }
                        )
                        FilterChip(
                            label = "Present ($presentRecords)",
                            isSelected = filterStatus == "Present",
                            onClick = { filterStatus = "Present" }
                        )
                        FilterChip(
                            label = "Absent ($absentRecords)",
                            isSelected = filterStatus == "Absent",
                            isAlert = true,
                            onClick = { filterStatus = "Absent" }
                        )
                    }
                }
            }

            // Roster List for this Date
            if (filteredRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No records logged for $currentDate yet.\nUse 'Mark Attendance' to take roll call.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredRecords, key = { it.id }) { record ->
                    val student = studentsMap[record.studentId]
                    val isPresent = record.status == AttendanceStatus.PRESENT

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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isPresent) MaterialTheme.colorScheme.surfaceContainerHigh else AttendanceAbsentContainer
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student?.getInitials() ?: "ST",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPresent) TrackEduPrimary else AttendanceAbsentOnContainer
                                    )
                                }

                                Column {
                                    Text(
                                        text = student?.name ?: "Unknown Student",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Roll: ${student?.rollNumber ?: "N/A"} • ${record.period}",
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
}
