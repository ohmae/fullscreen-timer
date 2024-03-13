/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.settings

import android.content.Context
import android.graphics.Color
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import net.mm2d.timer.BuildConfig
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.MAIN,
        migrations = listOf(
            MigrationForData(),
            MigrationForVersion(),
        ),
    )
    private val dataStore: DataStore<Preferences> = context.dataStoreField
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val flow: Flow<Settings> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map {
            Settings(
                versionAtInstall = it[VERSION_AT_INSTALL] ?: 0,
                versionAtLastLaunched = it[VERSION_AT_LAST_LAUNCHED] ?: 0,
                versionBeforeUpdate = it[VERSION_BEFORE_UPDATE] ?: 0,
                mode = Mode.of(it[MODE]),
                foregroundColor = it[FOREGROUND_COLOR] ?: Color.WHITE,
                backgroundColor = it[BACKGROUND_COLOR] ?: Color.BLACK,
                hourEnabled = it[HOUR_ENABLED] ?: false,
                hourFormat24 = it[HOUR_FORMAT_24] ?: true,
                timerTime = it[TIMER_TIME] ?: TIMER_TIME_DEFAULT,
                soundVolume = it[SOUND_VOLUME] ?: 10,
                fullscreen = it[FULLSCREEN] ?: true,
                orientation = Orientation.of(it[ORIENTATION]),
                buttonOpacity = it[BUTTON_OPACITY]?.coerceIn(0f, 1f) ?: 1f,
            )
        }
        .shareIn(scope = scope, started = SharingStarted.Eagerly, replay = 1)

    suspend fun updateMode(mode: Mode) {
        dataStore.edit {
            it[MODE] = mode.name
        }
    }

    suspend fun updateForegroundColor(color: Int) {
        dataStore.edit {
            it[FOREGROUND_COLOR] = color
        }
    }

    suspend fun updateBackgroundColor(color: Int) {
        dataStore.edit {
            it[BACKGROUND_COLOR] = color
        }
    }

    suspend fun updateHourEnabled(enabled: Boolean) {
        dataStore.edit {
            it[HOUR_ENABLED] = enabled
        }
    }

    suspend fun updateHourFormat24(enabled: Boolean) {
        dataStore.edit {
            it[HOUR_FORMAT_24] = enabled
        }
    }

    suspend fun updateTimerTime(time: Long) {
        dataStore.edit {
            it[TIMER_TIME] = time
        }
    }

    suspend fun updateVolume(volume: Int) {
        dataStore.edit {
            it[SOUND_VOLUME] = volume
        }
    }

    suspend fun updateFullscreen(fullscreen: Boolean) {
        dataStore.edit {
            it[FULLSCREEN] = fullscreen
        }
    }

    suspend fun updateOrientation(orientation: Orientation) {
        dataStore.edit {
            it[ORIENTATION] = orientation.value
        }
    }

    suspend fun updateButtonOpacity(opacity: Float) {
        dataStore.edit {
            it[BUTTON_OPACITY] = opacity
        }
    }

    private class MigrationForData : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean =
            currentData[DATA_VERSION] != VERSION

        override suspend fun migrate(currentData: Preferences): Preferences =
            currentData.edit {
                it[DATA_VERSION] = VERSION
            }

        override suspend fun cleanUp() = Unit
    }

    private class MigrationForVersion : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean =
            currentData[VERSION_AT_LAST_LAUNCHED] != BuildConfig.VERSION_CODE

        override suspend fun migrate(currentData: Preferences): Preferences =
            currentData.edit { preferences ->
                preferences[VERSION_AT_INSTALL]?.let {
                    preferences[VERSION_AT_INSTALL] = BuildConfig.VERSION_CODE
                }
                preferences[VERSION_AT_LAST_LAUNCHED]?.let {
                    preferences[VERSION_BEFORE_UPDATE] = it
                }
                preferences[VERSION_AT_LAST_LAUNCHED] = BuildConfig.VERSION_CODE
            }

        override suspend fun cleanUp() = Unit
    }

    companion object {
        // 1 : 0.0.1
        private const val VERSION = 1
        const val TIMER_TIME_DEFAULT = 60_000L

        private val DATA_VERSION =
            Key.Main.DATA_VERSION_INT.intKey()
        private val VERSION_AT_INSTALL =
            Key.Main.VERSION_AT_INSTALL_INT.intKey()
        private val VERSION_AT_LAST_LAUNCHED =
            Key.Main.VERSION_AT_LAST_LAUNCHED_INT.intKey()
        private val VERSION_BEFORE_UPDATE =
            Key.Main.VERSION_BEFORE_UPDATE_INT.intKey()
        private val MODE =
            Key.Main.MODE_STRING.stringKey()
        private val FOREGROUND_COLOR =
            Key.Main.FOREGROUND_COLOR_INT.intKey()
        private val BACKGROUND_COLOR =
            Key.Main.BACKGROUND_COLOR_INT.intKey()
        private val HOUR_ENABLED =
            Key.Main.HOUR_ENABLED_BOOLEAN.booleanKey()
        private val HOUR_FORMAT_24 =
            Key.Main.HOUR_FORMAT_24_BOOLEAN.booleanKey()
        private val TIMER_TIME =
            Key.Main.TIMER_TIME_LONG.longKey()
        private val SOUND_VOLUME =
            Key.Main.SOUND_VOLUME_INT.intKey()
        private val FULLSCREEN =
            Key.Main.FULLSCREEN_BOOLEAN.booleanKey()
        private val ORIENTATION =
            Key.Main.ORIENTATION_INT.intKey()
        private val BUTTON_OPACITY =
            Key.Main.BUTTON_OPACITY_FLOAT.floatKey()
    }
}
