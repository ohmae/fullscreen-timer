/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.sound

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.mm2d.timer.settings.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundEffect @Inject constructor(
    settingsRepository: SettingsRepository,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var volume: Int = 0

    init {
        scope.launch {
            settingsRepository.flow.collect {
                volume = it.soundVolume
            }
        }
    }

    fun play() {
        ToneGenerator(AudioManager.STREAM_SYSTEM, volume * 10)
            .startTone(ToneGenerator.TONE_PROP_BEEP)
    }
}
