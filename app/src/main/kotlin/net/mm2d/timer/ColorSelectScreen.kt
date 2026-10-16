/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.repeatOnLifecycle
import net.mm2d.color.chooser.compose.ColorChooserScreen
import net.mm2d.timer.ColorSelectViewModel.UiEffect
import net.mm2d.timer.ColorSelectViewModel.UiEvent
import net.mm2d.timer.ColorSelectViewModel.UiState
import net.mm2d.timer.ui.theme.AppTheme

@Composable
fun ColorSelectScreen(
    target: ColorTarget,
    goBack: () -> Unit,
    viewModel: ColorSelectViewModel = hiltViewModel(),
) {
    LaunchedEffect(target) {
        viewModel.onEvent(UiEvent.Load(target))
    }
    ColorSelectScreenEffect(
        viewModel = viewModel,
        onBack = goBack,
    )
    val uiState by viewModel.getUiStateStream().collectAsStateWithLifecycle()
    ColorSelectScreenContent(
        target = target,
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun ColorSelectScreenEffect(
    viewModel: ColorSelectViewModel,
    onBack: () -> Unit,
) {
    val currentOnBack by rememberUpdatedState(onBack)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.getUiEffectStream().collect { effect ->
                when (effect) {
                    UiEffect.GoBack -> currentOnBack()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorSelectScreenContent(
    target: ColorTarget,
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = dropUnlessResumed { onEvent(UiEvent.ClickBack) },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                title = { Text(text = stringResource(id = target.titleRes)) },
                actions = {
                    IconButton(
                        onClick = dropUnlessResumed { onEvent(UiEvent.ClickSave(target)) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(id = R.string.ok),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = dropUnlessResumed { onEvent(UiEvent.ClickBack) },
                        ) {
                            Text(
                                text = stringResource(id = R.string.cancel),
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = dropUnlessResumed { onEvent(UiEvent.ClickSave(target)) },
                        ) {
                            Text(
                                text = stringResource(id = R.string.ok),
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        val initialColor = uiState.initialColor
        if (initialColor != null) {
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState),
            ) {
                ColorChooserScreen(
                    initialColor = initialColor,
                    onColorChanged = { onEvent(UiEvent.ChangeColor(it)) },
                    withAlpha = false,
                    disableInnerScroll = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ColorSelectScreenPreview() {
    AppTheme {
        ColorSelectScreenContent(
            target = ColorTarget.FOREGROUND,
            uiState = UiState(initialColor = Color.White),
            onEvent = {},
        )
    }
}
