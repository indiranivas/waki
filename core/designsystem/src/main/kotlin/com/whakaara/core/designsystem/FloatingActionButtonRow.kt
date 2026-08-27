package com.whakaara.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.whakaara.core.designsystem.theme.BooleanPreviewProvider
import com.whakaara.core.designsystem.theme.Shapes
import com.whakaara.core.designsystem.theme.WakiTheme
import com.whakaara.core.designsystem.theme.wakiOrange

@Composable
fun FloatingActionButtonRow(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isStart: Boolean,
    isPlayButtonVisible: Boolean = true,
    isExtraButtonVisible: Boolean = isPlaying,
    stopIcon: ImageVector = Icons.Filled.Stop,
    extraIcon: ImageVector = Icons.Filled.Refresh,
    onStop: () -> Unit,
    onPlayPause: () -> Unit,
    onExtraButtonClicked: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(shape = Shapes.large),
            horizontalArrangement = Arrangement.End,
        ) {
            AnimatedVisibility(
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start),
                visible = !isStart,
            ) {
                TimerSideButton(
                    onClick = onStop,
                    imageVector = stopIcon,
                    contentDescription = stringResource(id = R.string.stop_timer_icon_content_description),
                )
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            AnimatedVisibility(
                visible = isPlayButtonVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FloatingActionButtonPlayPause(
                    isPlaying = isPlaying,
                    onClick = onPlayPause,
                )
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(shape = Shapes.large),
            horizontalArrangement = Arrangement.Start,
        ) {
            AnimatedVisibility(isExtraButtonVisible) {
                TimerSideButton(
                    onClick = onExtraButtonClicked,
                    imageVector = extraIcon,
                    contentDescription = stringResource(id = R.string.lap_reset_icon_content_description),
                )
            }
        }
    }
}

@Composable
private fun TimerSideButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = CircleShape,
            ),
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun FloatingActionButtonPlayPause(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    onClick: () -> Unit,
) {
    FloatingActionButton(
        modifier = modifier
            .size(80.dp)
            .testTag("floating action button play-pause"),
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp,
        ),
        containerColor = wakiOrange,
        contentColor = Color.White,
        onClick = onClick,
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Preview
@Composable
fun FloatingActionButtonStopPreview() {
    WakiTheme {
        TimerSideButton(
            onClick = {},
            imageVector = Icons.Filled.Refresh,
            contentDescription = "Reset",
        )
    }
}

@Preview
@Composable
fun FloatingActionButtonPlayPausePreview(
    @PreviewParameter(BooleanPreviewProvider::class) isPlaying: Boolean,
) {
    WakiTheme {
        FloatingActionButtonPlayPause(
            isPlaying = isPlaying,
            onClick = {},
        )
    }
}

@Preview
@Composable
fun FloatingActionButtonExtraActionPreview() {
    WakiTheme {
        TimerSideButton(
            onClick = {},
            imageVector = Icons.Filled.Add,
            contentDescription = "Add time",
        )
    }
}
