package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TealAccent
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AttendanceViewModel

@Composable
fun TrackEduNavigationDrawerContent(
    viewModel: AttendanceViewModel,
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationsCount.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Avatar initials derived dynamically from current user's name
    val avatarInitials = currentUser.name.split(" ")
        .filter { it.isNotBlank() && !it.startsWith("Dr.") && !it.startsWith("Prof.") }
        .map { it.take(1) }
        .joinToString("")
        .ifBlank { currentUser.name.take(2) }
        .uppercase()
        .take(2)

    ModalDrawerSheet(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // 1. Drawer Header: Profile & Identity with statusBarsPadding
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // TrackEdu Logo Badge
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "TrackEdu Logo",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "TrackEdu",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Faculty Attendance Management",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // User Profile Card
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp)
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(DeepBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatarInitials,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser.name.ifBlank { "Faculty Teacher" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = currentUser.department.ifBlank { "Department Faculty" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Role Pill
                            Surface(
                                color = Color(0xFF134E4A),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "TEACHER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF99F6E4),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Navigation Items
            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Dashboard
                DrawerNavItem(
                    label = "Dashboard",
                    icon = Icons.Default.Dashboard,
                    isSelected = currentScreen == AppScreen.DASHBOARD,
                    onClick = {
                        onNavigate(AppScreen.DASHBOARD)
                        onCloseDrawer()
                    },
                    testTag = "drawer_nav_dashboard"
                )

                // Students
                DrawerNavItem(
                    label = "Students",
                    icon = Icons.Default.Groups,
                    isSelected = currentScreen == AppScreen.STUDENTS ||
                            currentScreen == AppScreen.ADD_STUDENT ||
                            currentScreen == AppScreen.EDIT_STUDENT ||
                            currentScreen == AppScreen.STUDENT_DETAIL,
                    onClick = {
                        onNavigate(AppScreen.STUDENTS)
                        onCloseDrawer()
                    },
                    testTag = "drawer_nav_students"
                )

                // Attendance
                DrawerNavItem(
                    label = "Attendance",
                    icon = Icons.Default.AssignmentTurnedIn,
                    isSelected = currentScreen == AppScreen.MARK_ATTENDANCE || currentScreen == AppScreen.RECORDS,
                    onClick = {
                        onNavigate(AppScreen.MARK_ATTENDANCE)
                        onCloseDrawer()
                    },
                    testTag = "drawer_nav_attendance"
                )

                // Special Attendance (OD / Medical / Sports / Exam / Leave)
                DrawerNavItem(
                    label = "Special Attendance",
                    icon = Icons.Default.VerifiedUser,
                    isSelected = currentScreen == AppScreen.SPECIAL_CASE,
                    onClick = {
                        onNavigate(AppScreen.SPECIAL_CASE)
                        onCloseDrawer()
                    },
                    testTag = "drawer_nav_special_case"
                )

                // Reports
                DrawerNavItem(
                    label = "Reports",
                    icon = Icons.Default.Analytics,
                    isSelected = currentScreen == AppScreen.REPORTS,
                    onClick = {
                        onNavigate(AppScreen.REPORTS)
                        onCloseDrawer()
                    },
                    testTag = "drawer_nav_reports"
                )

                // Notifications
                DrawerNavItem(
                    label = "Notifications",
                    icon = Icons.Default.Notifications,
                    isSelected = currentScreen == AppScreen.NOTIFICATIONS,
                    badgeCount = unreadNotifications,
                    onClick = {
                        onNavigate(AppScreen.NOTIFICATIONS)
                        onCloseDrawer()
                    },
                    testTag = "drawer_nav_notifications"
                )

                // Settings
                DrawerNavItem(
                    label = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = currentScreen == AppScreen.SETTINGS,
                    onClick = {
                        onNavigate(AppScreen.SETTINGS)
                        onCloseDrawer()
                    },
                    testTag = "drawer_nav_settings"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(12.dp))

            // 3. Appearance / Theme Switcher
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                // Segmented Theme Toggle
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Light Mode Option
                        val isLightSelected = themeMode == "LIGHT"
                        Surface(
                            color = if (isLightSelected) PrimaryBlue else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setThemeMode("LIGHT") }
                                .testTag("theme_switch_light")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = if (isLightSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Light",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLightSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Dark Mode Option
                        val isDarkSelected = themeMode == "DARK"
                        Surface(
                            color = if (isDarkSelected) PrimaryBlue else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setThemeMode("DARK") }
                                .testTag("theme_switch_dark")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = if (isDarkSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Dark",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // System Default Option
                        val isSystemSelected = themeMode == "SYSTEM"
                        Surface(
                            color = if (isSystemSelected) PrimaryBlue else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setThemeMode("SYSTEM") }
                                .testTag("theme_switch_system")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Devices,
                                    contentDescription = null,
                                    tint = if (isSystemSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "System",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSystemSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(12.dp))

            // 4. Distinct Logout Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Surface(
                    color = CriticalRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoutDialog = true }
                        .testTag("drawer_logout_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log out",
                            tint = CriticalRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Log Out",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CriticalRed
                        )
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = CriticalRed,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Log out of TrackEdu?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "You will need to sign in again to access the application. Your database and student attendance records will remain safely saved.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onCloseDrawer()
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("LOG OUT", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun DrawerNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int? = null,
    testTag: String
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        },
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
                if (badgeCount != null && badgeCount > 0) {
                    Surface(
                        color = if (isSelected) Color.White else PrimaryBlue,
                        shape = CircleShape
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PrimaryBlue else Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = PrimaryBlue,
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
