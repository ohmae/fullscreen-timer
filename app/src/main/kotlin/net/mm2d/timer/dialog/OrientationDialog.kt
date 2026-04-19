/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.mm2d.timer.R
import net.mm2d.timer.settings.Orientation
import net.mm2d.timer.ui.theme.AppTheme

@Composable
fun OrientationDialog(
    selectedOrientation: Orientation,
    onChooseOrientation: (Orientation) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(),
    ) {
        Surface(
            modifier = Modifier.sizeIn(minWidth = DialogMinWidth, maxWidth = DialogMaxWidth),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.menu_title_orientation),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
                Column {
                    Orientation.entries.forEach { orientation ->
                        OrientationItem(
                            orientation = orientation,
                            selected = orientation == selectedOrientation,
                            onClick = { onChooseOrientation(orientation) },
                        )
                    }
                }
                TextButton(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp, end = 16.dp, bottom = 8.dp),
                    onClick = onDismissRequest,
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun OrientationItem(
    orientation: Orientation,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(orientation.icon),
            contentDescription = null,
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(orientation.description),
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
    )
}

@Preview(showBackground = true)
@Composable
private fun OrientationDialogPreview() {
    AppTheme {
        OrientationDialog(
            selectedOrientation = Orientation.LANDSCAPE,
            onChooseOrientation = {},
            onDismissRequest = {},
        )
    }
}
