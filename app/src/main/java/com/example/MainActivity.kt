package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.TrackEduBottomBar
import com.example.ui.components.TrackEduNavigationDrawerContent
import com.example.ui.components.TrackEduTopBar
import com.example.ui.screens.AddStudentScreen
import com.example.ui.screens.AttendanceRecordsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditStudentScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MarkAttendanceScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupTeacherScreen
import com.example.ui.screens.SpecialCaseScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.StudentsRosterScreen
import com.example.ui.theme.TrackEduTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var sharedViewModel: AttendanceViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AttendanceViewModel = viewModel()
            sharedViewModel = viewModel
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            // Ensure status bar icon contrast matches the active theme
            LaunchedEffect(isDarkTheme) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !isDarkTheme
            }

            TrackEduTheme(darkTheme = isDarkTheme) {
                MainApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel?.checkSessionOnResume()
    }
}

@Composable
fun MainApp(viewModel: AttendanceViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationsCount.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // 1. Back button closes drawer first if open
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    // 2. Back button security: back from Dashboard, Login, Splash, Landing, SetupTeacher never cycles back into previous sessions
    BackHandler(
        enabled = !drawerState.isOpen &&
                currentScreen != AppScreen.DASHBOARD &&
                currentScreen != AppScreen.LOGIN &&
                currentScreen != AppScreen.SPLASH &&
                currentScreen != AppScreen.LANDING &&
                currentScreen != AppScreen.SETUP_TEACHER
    ) {
        viewModel.navigateBack()
    }

    val isAuthenticated = currentScreen != AppScreen.SPLASH &&
            currentScreen != AppScreen.LANDING &&
            currentScreen != AppScreen.LOGIN &&
            currentScreen != AppScreen.SETUP_TEACHER

    val showTopBar = currentScreen in listOf(
        AppScreen.DASHBOARD,
        AppScreen.STUDENTS,
        AppScreen.REPORTS,
        AppScreen.SETTINGS,
        AppScreen.NOTIFICATIONS
    )

    val showBottomBar = currentScreen in listOf(
        AppScreen.DASHBOARD,
        AppScreen.STUDENTS,
        AppScreen.MARK_ATTENDANCE,
        AppScreen.REPORTS,
        AppScreen.SETTINGS
    )

    // Compute user avatar initials dynamically
    val userInitials = currentUser.name.split(" ")
        .filter { it.isNotBlank() && !it.startsWith("Dr.") && !it.startsWith("Prof.") }
        .map { it.take(1) }
        .joinToString("")
        .ifBlank { currentUser.name.take(2) }
        .uppercase()
        .take(2)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isAuthenticated,
        drawerContent = {
            if (isAuthenticated) {
                TrackEduNavigationDrawerContent(
                    viewModel = viewModel,
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.systemBars,
            topBar = {
                if (showTopBar) {
                    TrackEduTopBar(
                        title = "TrackEdu",
                        subtitle = when (currentScreen) {
                            AppScreen.DASHBOARD -> "${currentUser.department} • Teacher"
                            AppScreen.STUDENTS -> "Student Directory"
                            AppScreen.REPORTS -> "Academic Analytics"
                            AppScreen.SETTINGS -> "Faculty Administration"
                            AppScreen.NOTIFICATIONS -> "Notification Center"
                            else -> "Faculty Attendance"
                        },
                        unreadCount = unreadNotifications,
                        userInitials = userInitials,
                        onMenuClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        },
                        onNotificationsClick = {
                            viewModel.navigateTo(AppScreen.NOTIFICATIONS)
                        },
                        onProfileClick = {
                            viewModel.navigateTo(AppScreen.SETTINGS)
                        }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    TrackEduBottomBar(
                        currentScreen = currentScreen,
                        onNavigate = { screen ->
                            viewModel.navigateTo(screen)
                        }
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { innerPadding ->
            val isSessionExpiredDialogVisible by viewModel.isSessionExpiredDialogVisible.collectAsState()

            if (isSessionExpiredDialogVisible) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissSessionExpiredDialog() },
                    title = {
                        Text(
                            text = "Session Expired",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Your 5-minute authentication session has expired for student data security. Please sign in again.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissSessionExpiredDialog() }
                        ) {
                            Text("SIGN IN AGAIN")
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        AppScreen.SPLASH -> SplashScreen(viewModel = viewModel)
                        AppScreen.LANDING -> LandingScreen(viewModel = viewModel)
                        AppScreen.SETUP_TEACHER -> SetupTeacherScreen(viewModel = viewModel)
                        AppScreen.LOGIN -> LoginScreen(viewModel = viewModel)
                        AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                        AppScreen.STUDENTS -> StudentsRosterScreen(viewModel = viewModel)
                        AppScreen.MARK_ATTENDANCE -> MarkAttendanceScreen(viewModel = viewModel)
                        AppScreen.RECORDS -> AttendanceRecordsScreen(viewModel = viewModel)
                        AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                        AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        AppScreen.ADD_STUDENT -> AddStudentScreen(viewModel = viewModel)
                        AppScreen.EDIT_STUDENT -> EditStudentScreen(viewModel = viewModel)
                        AppScreen.STUDENT_DETAIL -> StudentDetailScreen(viewModel = viewModel)
                        AppScreen.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                        AppScreen.SPECIAL_CASE -> SpecialCaseScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
