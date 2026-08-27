package com.whakaara.core.designsystem.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * Mirrors the app's explicit theme choice (not the system setting alone).
 * Provided by [WakiTheme].
 */
val LocalWakiDarkTheme = compositionLocalOf { false }
