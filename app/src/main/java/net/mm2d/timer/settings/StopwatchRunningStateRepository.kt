/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.settings

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StopwatchRunningStateRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.STOPWATCH_STATE,
        migrations = listOf(MigrationForData()),
    )
    private val dataStore: DataStore<Preferences> = context.dataStoreField

    val flow: Flow<StopwatchRunningState> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map {
            StopwatchRunningState(
                started = it[STARTED] ?: false,
                start = it[START] ?: 0L,
                milestone = it[MILESTONE] ?: 0L,
            )
        }

    suspend fun updateState(state: StopwatchRunningState) {
        dataStore.edit {
            it[STARTED] = state.started
            it[START] = state.start
            it[MILESTONE] = state.milestone
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

    companion object {
        // 1 : 0.0.1
        private const val VERSION = 1

        private val DATA_VERSION =
            Key.Stopwatch.DATA_VERSION_INT.intKey()
        private val STARTED =
            Key.Stopwatch.STARTED_BOOLEAN.booleanKey()
        private val START =
            Key.Stopwatch.START_LONG.longKey()
        private val MILESTONE =
            Key.Stopwatch.MILESTONE_LONG.longKey()
    }
}
