package com.whakaara.onboarding.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.whakaara.core.designsystem.theme.FontScalePreviews
import com.whakaara.core.designsystem.theme.Shapes
import com.whakaara.core.designsystem.theme.Spacings.space20
import com.whakaara.core.designsystem.theme.Spacings.space200
import com.whakaara.core.designsystem.theme.Spacings.spaceMedium
import com.whakaara.core.designsystem.theme.ThemePreviews
import com.whakaara.core.designsystem.theme.WakiTheme
import com.whakaara.core.designsystem.theme.wakiOrange
import net.vbuild.verwoodpages.onboarding.R

@Composable
fun WelcomeOnboarding(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    val configuration = LocalConfiguration.current
    val displayIcon = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED || (configuration.orientation != Configuration.ORIENTATION_LANDSCAPE && windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.EXPANDED)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = spaceMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (displayIcon) {
            Box(
                Modifier
                    .size(space200)
                    .clip(Shapes.medium)
                    .background(color = wakiOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(id = R.drawable.translucent),
                    contentDescription = stringResource(id = R.string.onboarding_welcome_icon_description)
                )
            }
            Spacer(modifier = Modifier.height(space20))
        }
        Text(
            modifier = Modifier.width(300.dp),
            text = "Welcome to Waki 👋",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.width(300.dp),
            text = "Your smart alarm that understands time, place and context.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
@ThemePreviews
@FontScalePreviews
fun WelcomeOnboardingPreview() {
    WakiTheme {
        WelcomeOnboarding()
    }
}
