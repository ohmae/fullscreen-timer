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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mm2d.timer.settings.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class SoundEffect @Inject constructor(
    settingsRepository: SettingsRepository,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var volume: Int = 0
    private var job: Job? = null
    private var toneGenerator: ToneGenerator? = null

    init {
        scope.launch {
            settingsRepository.flow.collect {
                updateVolume(it.soundVolume)
            }
        }
    }

    private fun updateVolume(
        newVolume: Int,
    ) {
        if (volume == newVolume) return
        volume = newVolume
        toneGenerator?.release()
        toneGenerator = null
    }

    fun play() {
        playTone(ToneGenerator.TONE_PROP_BEEP)
    }

    fun playStop() {
        job?.cancel()
        job = scope.launch {
            repeat(3) {
                playTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE)
                delay(1.seconds)
            }
            job = null
        }
    }

    private fun playTone(
        toneType: Int,
    ) {
        if (volume <= 0) return
        getOrCreateToneGenerator()?.startTone(toneType)
    }

    @Synchronized
    private fun getOrCreateToneGenerator(): ToneGenerator? {
        if (volume <= 0) return null
        return toneGenerator ?: runCatching {
            ToneGenerator(AudioManager.STREAM_SYSTEM, volume * 10)
        }.getOrNull()?.also {
            toneGenerator = it
        }
    }
}
