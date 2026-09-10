package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// TrackEdu Dark Navy Palette (Master Reference)
val NavyBackground = Color(0xFF0B162B)
val NavySecondary = Color(0xFF111D35)
val NavyCard = Color(0xFF17233D)
val NavyCardElevated = Color(0xFF1F2E4D)
val NavyBorder = Color(0xFF263859)

// Accent & Primary Colors
val PrimaryBlue = Color(0xFF2563EB)
val DeepBlue = Color(0xFF1E3A8A)
val BrightCyan = Color(0xFF2DD4BF)
val TealAccent = Color(0xFF14B8A6)
val TealDark = Color(0xFF0D9488)

// Status & Semantic Colors
val SuccessGreen = Color(0xFF16A34A)
val WarningAmber = Color(0xFFF59E0B)
val CriticalRed = Color(0xFFDC2626)

// Text Colors
val TextPrimaryDark = Color(0xFFE2E8F0)
val TextSecondaryDark = Color(0xFFCBD5E1)
val TextMutedDark = Color(0xFF94A3B8)
val PureWhite = Color(0xFFFFFFFF)

// Light Theme Palette
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFFFFFFF)
val LightElevated = Color(0xFFF1F5F9)
val LightBorder = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF64748B)

// Semantic Attendance Colors & Containers
val AttendancePresentGreen = SuccessGreen
val AttendancePresentContainer = Color(0xFFDCFCE7)
val AttendancePresentOnContainer = Color(0xFF14532D)

val AttendanceAbsentRed = CriticalRed
val AttendanceAbsentContainer = Color(0xFFFFDAD6)
val AttendanceAbsentOnContainer = Color(0xFF93000A)

val AttendanceWarningAmber = WarningAmber
val AttendanceWarningContainer = Color(0xFFFEF3C7)
val AttendanceWarningOnContainer = Color(0xFF92400E)

val TrackEduError = CriticalRed
val TrackEduOnError = Color(0xFFFFFFFF)
val TrackEduErrorContainer = Color(0xFFFFDAD6)
val TrackEduOnErrorContainer = Color(0xFF93000A)

// Legacy aliases for backward compatibility across components
val TrackEduPrimary = PrimaryBlue
val TrackEduOnPrimary = PureWhite
val TrackEduPrimaryContainer = DeepBlue
val TrackEduOnPrimaryContainer = Color(0xFFDCE1FF)

val TrackEduSecondary = TealAccent
val TrackEduOnSecondary = PureWhite
val TrackEduSecondaryContainer = Color(0xFF86F2E4)
val TrackEduOnSecondaryContainer = Color(0xFF004D40)

val TrackEduBackground = NavyBackground
val TrackEduSurface = NavyCard
val TrackEduOutline = NavyBorder

