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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceType
import com.example.ui.theme.AttendancePresentContainer
import com.example.ui.theme.AttendancePresentGreen
import com.example.ui.theme.AttendancePresentOnContainer
import com.example.ui.theme.TrackEduPrimary
import com.example.ui.theme.TrackEduPrimaryContainer
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpecialCaseScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val allStudents by viewModel.allStudents.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStudentId by remember { mutableStateOf<Long?>(null) }
    var selectedStudentName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(selectedDate.ifBlank { today }) }
    var subject by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AttendanceType.OD) }

    val filteredStudents = remember(searchQuery, allStudents) {
        if (searchQuery.isBlank()) emptyList()
        else allStudents.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.rollNumber.contains(searchQuery, ignoreCase = true)
        }.take(6)
    }

    data class SpecialTypeInfo(val type: AttendanceType, val label: String)
    val specialTypes = listOf(
        SpecialTypeInfo(AttendanceType.OD, "On Duty (OD)"),
        SpecialTypeInfo(AttendanceType.MEDICAL, "Medical Leave"),
        SpecialTypeInfo(AttendanceType.SPORTS, "Sports Duty"),
        SpecialTypeInfo(AttendanceType.EXAM, "Exam Exemption"),
        SpecialTypeInfo(AttendanceType.LEAVE, "Authorized Leave")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Column {
                Text(
                    text = "Special Attendance Entry",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Teacher Direct Record \u2022 OD / Medical / Sports / Exam / Leave",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            color = TrackEduPrimaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = TrackEduPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Direct Entry Mode \u2014 Saves instantly to student attendance record",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

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
                    text = "Attendance Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    specialTypes.forEach { info ->
                        val isSelected = selectedType == info.type
                        Surface(
                            color = if (isSelected) TrackEduPrimary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedType = info.type }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = info.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

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
                    text = "Select Student",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (selectedStudentId != null) {
                    Surface(
                        color = AttendancePresentContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AttendancePresentGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = selectedStudentName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AttendancePresentOnContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Change",
                                style = MaterialTheme.typography.labelSmall,
                                color = AttendancePresentGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    selectedStudentId = null
                                    selectedStudentName = ""
                                    searchQuery = ""
                                }
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name or roll number...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrackEduPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    filteredStudents.forEach { student ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedStudentId = student.id
                                    selectedStudentName = "${student.name} (${student.rollNumber})"
                                    searchQuery = ""
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(TrackEduPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = TrackEduPrimary, modifier = Modifier.size(18.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${student.rollNumber} \u2022 ${student.department}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (searchQuery.isNotBlank() && filteredStudents.isEmpty()) {
                        Text(
                            text = "No students found for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Session Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (yyyy-MM-dd)") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TrackEduPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject / Course (optional)") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TrackEduPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Teacher Remarks / Verification Note") },
                    leadingIcon = { Icon(Icons.Default.AssignmentTurnedIn, null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TrackEduPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        Button(
            onClick = {
                val sid = selectedStudentId
                if (sid == null) {
                    viewModel.showToast("Select a student first", isError = true)
                    return@Button
                }
                if (date.isBlank()) {
                    viewModel.showToast("Date is required", isError = true)
                    return@Button
                }
                viewModel.saveSpecialCaseAttendance(
                    studentId = sid,
                    date = date.trim(),
                    subject = subject.trim().ifBlank { "Academic Course" },
                    type = selectedType,
                    remarks = remarks.trim().ifBlank { "Verified by ${currentUser.name}" }
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = TrackEduPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "RECORD SPECIAL ATTENDANCE",
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
        OutlinedButton(
            onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}
