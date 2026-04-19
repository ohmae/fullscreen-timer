/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.mm2d.timer.R
import net.mm2d.timer.settings.Font

@Composable
fun FontDialog(
    selectedFont: Font,
    onChooseFont: (Font) -> Unit,
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
                    text = stringResource(id = R.string.menu_title_font),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
                Column {
                    Font.entries.forEach { font ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onChooseFont(font) }
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = font == selectedFont,
                                onClick = null,
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(font.descriptionRes()),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
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

@StringRes
private fun Font.descriptionRes(): Int =
    when (this) {
        Font.LED_7SEGMENT -> R.string.menu_description_font_led_7segment
        Font.ROBOTO -> R.string.menu_description_font_roboto
    }
