/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.mm2d.timer.MainViewModel.Button
import net.mm2d.timer.MainViewModel.UiEvent
import net.mm2d.timer.MainViewModel.UiState
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.ui.theme.AppTheme

@Composable
fun MainScreen(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!uiState.initialized) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(uiState.backgroundColor)),
        )
        return
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(uiState.backgroundColor)),
    ) {
        val clockModifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(CLOCK_PADDING)
        ClockDisplay(
            uiState = uiState,
            modifier = if (uiState.mode == Mode.CLOCK) {
                clockModifier
            } else {
                clockModifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = stringResource(R.string.action_toggle_timer_state),
                    role = Role.Button,
                    onClick = { onEvent(UiEvent.ClickTime) },
                )
            },
        )
        MainControls(
            uiState = uiState,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun MainControls(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = CONTROLS_HORIZONTAL_PADDING,
                end = CONTROLS_HORIZONTAL_PADDING,
                bottom = CONTROLS_BOTTOM_PADDING,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        MainControlButton(
            button = uiState.firstButton,
            buttonOpacity = uiState.buttonOpacity,
            useLightContent = uiState.shouldUseDarkForeground,
            onClick = { onEvent(UiEvent.ClickFirstButton) },
        )
        MainControlButton(
            button = uiState.secondButton,
            buttonOpacity = uiState.buttonOpacity,
            useLightContent = uiState.shouldUseDarkForeground,
            onClick = { onEvent(UiEvent.ClickSecondButton) },
        )
        MainControlButton(
            button = Button.SETTINGS,
            buttonOpacity = uiState.buttonOpacity,
            useLightContent = uiState.shouldUseDarkForeground,
            onClick = { onEvent(UiEvent.ClickSettings) },
        )
    }
}

@Composable
private fun MainControlButton(
    button: Button,
    buttonOpacity: Float,
    useLightContent: Boolean,
    onClick: () -> Unit,
) {
    if (button == Button.HIDDEN) {
        Spacer(Modifier.size(CONTROL_WIDTH, CONTROL_HEIGHT))
        return
    }
    val contentColor = if (useLightContent) Color.White else Color.Black
    val borderColor = contentColor.copy(alpha = if (useLightContent) 0.2f else 0.07f)
    val shape = RoundedCornerShape(CONTROL_CORNER_RADIUS)
    Box(
        modifier = Modifier
            .size(CONTROL_WIDTH, CONTROL_HEIGHT)
            .alpha(buttonOpacity.coerceIn(0f, 1f))
            .clip(shape)
            .border(CONTROL_BORDER_WIDTH, borderColor, shape)
            .clickable(
                onClickLabel = stringResource(button.descriptionRes),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(button.iconRes),
            contentDescription = stringResource(button.descriptionRes),
            modifier = Modifier.size(CONTROL_ICON_SIZE),
            colorFilter = ColorFilter.tint(contentColor),
        )
    }
}

@get:DrawableRes
private val Button.iconRes: Int
    get() = when (this) {
        Button.HIDDEN -> error("Hidden button has no icon")
        Button.START -> R.drawable.ic_start
        Button.PAUSE -> R.drawable.ic_pause
        Button.RESET -> R.drawable.ic_reset
        Button.TIMER -> R.drawable.ic_timer
        Button.SETTINGS -> R.drawable.ic_settings
    }

@get:StringRes
private val Button.descriptionRes: Int
    get() = when (this) {
        Button.HIDDEN -> error("Hidden button has no description")
        Button.START -> R.string.action_start
        Button.PAUSE -> R.string.action_pause
        Button.RESET -> R.string.action_reset
        Button.TIMER -> R.string.action_set_timer
        Button.SETTINGS -> R.string.action_open_settings
    }

@Preview(
    name = "Stopwatch",
    widthDp = 800,
    heightDp = 360,
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MainScreenPreview() {
    AppTheme {
        MainScreen(
            uiState = UiState(
                initialized = true,
                mode = Mode.STOPWATCH,
                timeMillis = 83_456L,
                foregroundColor = android.graphics.Color.WHITE,
                backgroundColor = android.graphics.Color.BLACK,
                shouldUseDarkForeground = true,
            ),
            onEvent = {},
        )
    }
}

private val CLOCK_PADDING = 8.dp
private val CONTROLS_HORIZONTAL_PADDING = 16.dp
private val CONTROLS_BOTTOM_PADDING = 8.dp
private val CONTROL_WIDTH = 80.dp
private val CONTROL_HEIGHT = 40.dp
private val CONTROL_ICON_SIZE = 24.dp
private val CONTROL_CORNER_RADIUS = 4.dp
private val CONTROL_BORDER_WIDTH = 1.dp
