package com.whakaara.onboarding.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.whakaara.core.designsystem.WakiPrimaryButton
import com.whakaara.core.designsystem.WakiScreenBackground
import com.whakaara.core.designsystem.theme.FontScalePreviews
import com.whakaara.core.designsystem.theme.Spacings.space80
import com.whakaara.core.designsystem.theme.Spacings.spaceMedium
import com.whakaara.core.designsystem.theme.Spacings.spaceSmall
import com.whakaara.core.designsystem.theme.Spacings.spaceXLarge
import com.whakaara.core.designsystem.theme.Spacings.spaceXxSmall
import com.whakaara.core.designsystem.theme.ThemePreviews
import com.whakaara.core.designsystem.theme.WakiTheme
import com.whakaara.core.designsystem.theme.wakiOrange
import com.whakaara.model.onboarding.OnboardingItems
import kotlinx.coroutines.launch
import net.vbuild.verwoodpages.onboarding.R

@Composable
fun OnboardingContent(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    pages: Array<OnboardingItems>,
    snackbarHostState: SnackbarHostState,
    navigateToHome: () -> Unit,
    updatePreferences: () -> Unit
) {
    val scope = rememberCoroutineScope()
    WakiScreenBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { index ->
            when (pages[index]) {
                OnboardingItems.WELCOME -> WelcomeOnboarding()
                OnboardingItems.NOTIFICATIONS -> NotificationsOnboarding(snackbarHostState = snackbarHostState)
                OnboardingItems.BATTERY_OPTIMIZATION -> DisableBatteryOptimizationOnboarding()
                OnboardingItems.WIDGET -> WidgetOnboarding()
            }
        }

        BottomSection(
            pagesSize = pages.size,
            pagerState = pagerState
        ) {
            if (pagerState.currentPage == pages.size - 1) {
                updatePreferences()
                navigateToHome()
            } else {
                scope.launch {
                    pagerState.animateScrollToPage(
                        page = pagerState.currentPage + 1,
                        animationSpec = tween()
                    )
                }
            }
        }
        }
    }
}

@Composable
fun BottomSection(
    pagesSize: Int,
    pagerState: PagerState,
    onButtonClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PageIndicators(
            pagesSize = pagesSize,
            pagerState = pagerState
        )

        Spacer(modifier = Modifier.height(24.dp))

        WakiPrimaryButton(
            text = if (pagerState.currentPage == 0) {
                "Let's get started"
            } else if (pagerState.currentPage == pagesSize - 1) {
                stringResource(id = R.string.onboarding_button_complete)
            } else {
                stringResource(id = R.string.onboarding_button_next)
            },
            onClick = onButtonClick
        )
    }
}

@Composable
fun PageIndicators(
    pagesSize: Int,
    pagerState: PagerState
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagesSize) { iteration ->
            Indicator(isSelected = pagerState.currentPage == iteration)
        }
    }
}

@Composable
private fun Indicator(isSelected: Boolean) {
    val size =
        animateDpAsState(
            targetValue = if (isSelected) spaceMedium else spaceSmall,
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
            label = "indicator size"
        )
    val color = if (isSelected) wakiOrange else wakiOrange.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .padding(spaceXxSmall)
            .clip(CircleShape)
            .background(color)
            .size(size.value)
    )
}

@Composable
@ThemePreviews
@FontScalePreviews
fun OnboardingContentPreview() {
    val pages = OnboardingItems.entries.toTypedArray()
    WakiTheme {
        OnboardingContent(
            pages = pages,
            pagerState = rememberPagerState(pageCount = { pages.size }),
            snackbarHostState = remember { SnackbarHostState() },
            navigateToHome = {},
            updatePreferences = {}
        )
    }
}
