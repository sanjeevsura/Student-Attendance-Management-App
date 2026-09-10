package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.UserRole
import com.example.data.repository.AttendanceTier
import com.example.ui.components.DonutProgressRing
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
import com.example.ui.theme.TrackEduSecondaryContainer
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.allStudents.collectAsState()
    val studentsWithStats by viewModel.studentsWithStats.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val currentDate by viewModel.selectedDate.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Dynamic formatted display date from system
    val displayDate = remember(currentDate) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displaySdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
            displaySdf.format(sdf.parse(currentDate) ?: Date()).uppercase()
        } catch (_: Exception) { currentDate.uppercase() }
    }

    // Dynamic time-based greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    // Calculated stats from Room Database (100% dynamic, zero fake fallbacks)
    val totalStudents = students.size
    val todayRecords = allRecords.filter { it.date == currentDate }
    val presentToday = todayRecords.count { it.status == AttendanceStatus.PRESENT }
    val absentToday = todayRecords.count { it.status == AttendanceStatus.ABSENT }
    val todayTurnout = if (todayRecords.isNotEmpty()) {
        (presentToday.toDouble() / todayRecords.size.toDouble()) * 100.0
    } else 0.0

    val shortageStudents = studentsWithStats.filter { it.attendancePercentage != null && it.attendancePercentage < 75.0 }
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()

    // Dynamic campus health calculated from Room DB
    val studentsWithRecordedAttendance = studentsWithStats.filter { it.attendancePercentage != null }
    val campusHealth = if (studentsWithRecordedAttendance.isNotEmpty()) {
        studentsWithRecordedAttendance.map { it.attendancePercentage!! }.average().toInt()
    } else 0
    val healthStatus = when {
        campusHealth >= 85 -> "Optimal"
        campusHealth >= 75 -> "Good"
        campusHealth > 0 -> "Attention"
        else -> "No Data"
    }

    // Dynamic attendance streak: count consecutive days from today backwards that have records
    val streakDays = remember(allRecords) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val recordedDates = allRecords.map { it.date }.toSet()
        var streak = 0
        val cal = Calendar.getInstance()
        for (i in 0..364) {
            val dateStr = sdf.format(cal.time)
            if (recordedDates.contains(dateStr)) streak++ else if (i > 0) break
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        streak.coerceAtLeast(0)
    }

    // Dynamic 7-day trend data from Room DB
    val weeklyTrendData = remember(allRecords, currentDate) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        (6 downTo 0).map { daysAgo ->
            val c2 = Calendar.getInstance()
            c2.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dateStr = sdf.format(c2.time)
            val label = SimpleDateFormat("EEE", Locale.getDefault()).format(c2.time).take(1)
            val dayRecs = allRecords.filter { it.date == dateStr }
            val pct = if (dayRecs.isNotEmpty())
                (dayRecs.count { it.status == AttendanceStatus.PRESENT }.toDouble() / dayRecs.size * 100).toInt()
            else -1  // -1 means no data
            val isToday = daysAgo == 0
            Triple(label, pct, isToday)
        }
    }

    // Dynamic initials for profile badge
    val avatarInitials = currentUser.name.split(" ")
        .filter { it.isNotBlank() && !it.startsWith("Dr.") && !it.startsWith("Prof.") }
        .map { it.take(1) }
        .joinToString("")
        .ifBlank { currentUser.name.take(2) }
        .uppercase()
        .take(2)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Welcome & Greeting (Dynamic user data)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$greeting,\n${currentUser.name}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 28.sp
                    )
                }

                // Faculty Profile Circle Badge
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(TrackEduPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatarInitials,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = TrackEduPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${currentUser.department} • ${currentUser.role.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            // Streak & Health Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Surface(
                    color = TrackEduSecondaryContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(TrackEduSecondary)
                        )
                        Text(
                            text = "Campus Health: $campusHealth% ($healthStatus)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TrackEduSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = TrackEduPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$streakDays-Day Session Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = TrackEduPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Daily Insight Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = TrackEduPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "DAILY ATTENDANCE INSIGHT",
                            style = MaterialTheme.typography.labelSmall,
                            color = TrackEduPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Active Cohort",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (todayRecords.isNotEmpty()) {
                            "Today's active turn-out is ${todayTurnout.toInt()}%. ${shortageStudents.size} students are currently flagged under the 75% statutory bar."
                        } else {
                            "No attendance recorded yet for today ($currentDate). Tap 'MARK ATTENDANCE' below to take roll call."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 3. Today's Attendance Hero Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            tint = TrackEduPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "TODAY — $displayDate",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (todayRecords.isNotEmpty()) "Turnout: ${todayTurnout.toInt()}%" else "Pending Roll Call",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Numbers & Ring
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column {
                            Text(
                                text = "Registered Roll Call",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$totalStudents ",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Pupils",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = AttendancePresentContainer,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AttendancePresentOnContainer,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "$presentToday Present",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AttendancePresentOnContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                color = AttendanceAbsentContainer,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = AttendanceAbsentOnContainer,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "$absentToday Absent",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AttendanceAbsentOnContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Turnout Progress Ring
                    DonutProgressRing(
                        percentage = todayTurnout,
                        size = 86.dp,
                        strokeWidth = 7.5.dp
                    )
                }

                // CTA Button: Highly visible, strong contrast MARK ATTENDANCE
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.MARK_ATTENDANCE) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrackEduPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("mark_today_attendance_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MARK ATTENDANCE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // 4. Quick Operations (Teacher Direct Actions 2x2 Grid)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Quick Operations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Mark Roll",
                    subtitle = "Record lecture session",
                    icon = Icons.Default.AssignmentTurnedIn,
                    iconBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                    iconTint = TrackEduPrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_mark_roll",
                    onClick = { viewModel.navigateTo(AppScreen.MARK_ATTENDANCE) }
                )

                QuickActionCard(
                    title = "Add Student",
                    subtitle = "Direct student enrollment",
                    icon = Icons.Default.PersonAdd,
                    iconBg = TrackEduSecondaryContainer,
                    iconTint = TrackEduSecondary,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_add_student",
                    onClick = { viewModel.navigateTo(AppScreen.ADD_STUDENT) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Students",
                    subtitle = "${students.size} enrolled",
                    icon = Icons.Default.Groups,
                    iconBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                    iconTint = TrackEduPrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_view_students",
                    onClick = { viewModel.navigateTo(AppScreen.STUDENTS) }
                )

                QuickActionCard(
                    title = "Special Attendance",
                    subtitle = "OD / Medical / Sports",
                    icon = Icons.Default.AssignmentTurnedIn,
                    iconBg = TrackEduSecondaryContainer,
                    iconTint = TrackEduSecondary,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_special_case",
                    onClick = { viewModel.navigateTo(AppScreen.SPECIAL_CASE) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Analytics",
                    subtitle = "Weekly & monthly logs",
                    icon = Icons.Default.Insights,
                    iconBg = MaterialTheme.colorScheme.surfaceVariant,
                    iconTint = TrackEduPrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_analytics",
                    onClick = { viewModel.navigateTo(AppScreen.REPORTS) }
                )

                QuickActionCard(
                    title = "Notifications",
                    subtitle = if (unreadCount > 0) "$unreadCount unread" else "All caught up",
                    icon = Icons.Default.Notifications,
                    iconBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                    iconTint = TrackEduPrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_notifications",
                    onClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) }
                )
            }
        }

        // 5. Weekly Trend Analysis Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Weekly Trend Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val daysWithData = weeklyTrendData.count { it.second >= 0 }
                        Text(
                            text = if (daysWithData > 0) "Last 7 Days · $daysWithData days recorded" else "No attendance data recorded yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(AttendancePresentGreen)
                        )
                        Text(
                            text = "Avg ${if (campusHealth > 0) "$campusHealth%" else "N/A"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AttendancePresentGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Dynamic Bar Chart from Room DB (last 7 days)
                if (weeklyTrendData.all { it.second < 0 }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No attendance data recorded yet. Start marking attendance to see trends.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyTrendData.forEach { (label, value, isToday) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (value >= 0) {
                                    Text(
                                        text = "$value%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = if (isToday) AttendancePresentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height((value.coerceAtLeast(4) * 0.9).dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                if (isToday) AttendancePresentGreen else TrackEduPrimaryContainer
                                            )
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isToday) TrackEduPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Assigned Batches — Dynamic from DB
        val sections = remember(studentsWithStats) {
            studentsWithStats.groupBy { it.student.section }.entries
                .sortedBy { it.key }
                .take(4)
        }
        if (sections.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Section Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${sections.size} Sections",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrackEduPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                sections.forEach { (sectionName, sectionStudents) ->
                    val avgPct = if (sectionStudents.any { it.attendancePercentage != null }) {
                        sectionStudents.mapNotNull { it.attendancePercentage }.average().toInt()
                    } else null

                    val (statusLabel, accentColor, statusBg, statusColor) = when {
                        avgPct == null -> listOf("No Data", Color(0xFF64748B), MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant)
                        avgPct >= 85 -> listOf("Optimal", AttendancePresentGreen, AttendancePresentContainer, AttendancePresentOnContainer)
                        avgPct >= 75 -> listOf("Good", TrackEduPrimary, MaterialTheme.colorScheme.surfaceContainerHigh, TrackEduPrimary)
                        avgPct >= 65 -> listOf("Attention", AttendanceWarningAmber, AttendanceWarningContainer, AttendanceWarningOnContainer)
                        else -> listOf("Critical", TrackEduError, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                    }
                    @Suppress("UNCHECKED_CAST")
                    BatchItem(
                        batchName = sectionName.ifBlank { "Unassigned" },
                        subtitle = "${sectionStudents.size} Students",
                        percentage = if (avgPct != null) "$avgPct%" else "--",
                        status = statusLabel as String,
                        accentColor = accentColor as Color,
                        statusBg = statusBg as Color,
                        statusColor = statusColor as Color
                    )
                }
            }
        }

        // 7. Shortage Risk Alert (<75%)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        text = "Shortage Risk Alert",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "View All (${shortageStudents.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TrackEduPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.STUDENTS) }
                )
            }

            Text(
                text = "Students under mandatory 75% attendance criterion.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (shortageStudents.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AttendancePresentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "All enrolled students currently meet or exceed the 75% attendance criterion!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                shortageStudents.forEach { studentStat ->
                    RiskStudentItem(
                        name = studentStat.student.name,
                        rollNumber = studentStat.student.rollNumber,
                        batch = "${studentStat.student.department.take(3).uppercase()} - ${studentStat.student.section}",
                        percentage = "${studentStat.attendancePercentage?.toInt() ?: 0}%",
                        shortfall = "-${(75 - (studentStat.attendancePercentage?.toInt() ?: 0)).coerceAtLeast(1)}% Short",
                        tier = studentStat.statusTier,
                        onAlert = {
                            viewModel.showToast("Alert notice dispatched to ${studentStat.student.name}'s guardian")
                        },
                        onClick = {
                            viewModel.navigateTo(AppScreen.STUDENT_DETAIL, studentStat.student.id)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun BatchItem(
    batchName: String,
    subtitle: String,
    percentage: String,
    status: String,
    accentColor: Color,
    statusBg: Color,
    statusColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
                Column {
                    Text(
                        text = batchName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = percentage,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RiskStudentItem(
    name: String,
    rollNumber: String,
    batch: String,
    percentage: String,
    shortfall: String,
    tier: AttendanceTier,
    onAlert: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                // Avatar with initials
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = TrackEduPrimary,
                        fontSize = 13.sp
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Surface(
                            color = if (tier == AttendanceTier.CRITICAL) AttendanceAbsentContainer else AttendanceWarningContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (tier == AttendanceTier.CRITICAL) "Critical" else "Warning",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (tier == AttendanceTier.CRITICAL) AttendanceAbsentOnContainer else AttendanceWarningOnContainer,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Roll: $rollNumber • $batch",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = percentage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (tier == AttendanceTier.CRITICAL) TrackEduError else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = shortfall,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                }

                IconButton(
                    onClick = onAlert,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Alert",
                        tint = TrackEduPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
