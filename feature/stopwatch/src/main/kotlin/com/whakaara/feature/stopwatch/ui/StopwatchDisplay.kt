package com.whakaara.feature.stopwatch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.whakaara.core.designsystem.theme.FontScalePreviews
import com.whakaara.core.designsystem.theme.ThemePreviews
import com.whakaara.core.designsystem.theme.WakiTheme

@Composable
fun StopwatchDisplay(
    modifier: Modifier = Modifier,
    formattedTime: String
) {
    val parts = formattedTime.split(":")
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${parts[0]}:${parts[1]}:${parts[2]}",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = ".${parts[3]}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Row(
            modifier = Modifier.width(280.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StopwatchUnitLabel("hr")
            StopwatchUnitLabel("min")
            StopwatchUnitLabel("sec")
            StopwatchUnitLabel("ms")
        }
    }
}

@Composable
private fun StopwatchUnitLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.width(40.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun StopwatchDisplayLandscape(
    modifier: Modifier = Modifier,
    formattedTime: String,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    StopwatchDisplay(modifier = modifier, formattedTime = formattedTime)
}

@Composable
@ThemePreviews
@FontScalePreviews
fun StopwatchDisplayLandscapePreview() {
    WakiTheme {
        StopwatchDisplayLandscape(
            formattedTime = "10:10:00:000"
        )
    }
}

@Composable
@ThemePreviews
@FontScalePreviews
fun StopwatchDisplayPreview() {
    WakiTheme {
        StopwatchDisplay(
            formattedTime = "10:10:00:000"
        )
    }
}
