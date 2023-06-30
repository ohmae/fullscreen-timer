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
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import net.mm2d.timer.BuildConfig
import kotlin.properties.ReadOnlyProperty

private const val SUFFIX_BOOLEAN = "_BOOLEAN"
private const val SUFFIX_INT = "_INT"
private const val SUFFIX_LONG = "_LONG"
private const val SUFFIX_FLOAT = "_FLOAT"
private const val SUFFIX_STRING = "_STRING"

sealed interface Key {
    enum class Main : Key {
        DATA_VERSION_INT,
        VERSION_AT_INSTALL_INT,
        VERSION_AT_LAST_LAUNCHED_INT,
        VERSION_BEFORE_UPDATE_INT,
        MODE_STRING,
        FOREGROUND_COLOR_INT,
        BACKGROUND_COLOR_INT,
        HOUR_ENABLED_BOOLEAN,
        HOUR_FORMAT_24_BOOLEAN,
        TIMER_TIME_LONG,
        SOUND_VOLUME_INT,
        FULLSCREEN_BOOLEAN,
        ORIENTATION_INT,
    }

    enum class Stopwatch : Key {
        DATA_VERSION_INT,
        STARTED_BOOLEAN,
        START_LONG,
        MILESTONE_LONG,
    }

    enum class Timer : Key {
        DATA_VERSION_INT,
        STARTED_BOOLEAN,
        START_LONG,
        MILESTONE_LONG,
    }
}

enum class DataStoreFile {
    MAIN,
    STOPWATCH_STATE,
    TIMER_STATE,
    ;

    fun fileName(): String =
        BuildConfig.APPLICATION_ID + "." + name.lowercase()
}

fun preferences(
    file: DataStoreFile,
    migrations: List<DataMigration<Preferences>> = listOf(),
): ReadOnlyProperty<Context, DataStore<Preferences>> =
    preferencesDataStore(
        name = file.fileName(),
        produceMigrations = { migrations },
    )

fun Preferences.edit(editor: (preferences: MutablePreferences) -> Unit): Preferences =
    toMutablePreferences().also(editor).toPreferences()

fun <K> K.intKey(): Preferences.Key<Int>
    where K : Enum<*>,
          K : Key {
    if (BuildConfig.DEBUG) {
        require(name.endsWith(SUFFIX_INT)) {
            "$this is used for Int, suffix \"$SUFFIX_INT\" is required."
        }
    }
    return intPreferencesKey(name)
}

fun <K> K.stringKey(): Preferences.Key<String>
    where K : Enum<*>,
          K : Key {
    if (BuildConfig.DEBUG) {
        require(name.endsWith(SUFFIX_STRING)) {
            "$this is used for String, suffix \"$SUFFIX_STRING\" is required."
        }
    }
    return stringPreferencesKey(name)
}

fun <K> K.booleanKey(): Preferences.Key<Boolean>
    where K : Enum<*>,
          K : Key {
    if (BuildConfig.DEBUG) {
        require(name.endsWith(SUFFIX_BOOLEAN)) {
            "$this is used for Boolean, suffix \"$SUFFIX_BOOLEAN\" is required."
        }
    }
    return booleanPreferencesKey(name)
}

fun <K> K.longKey(): Preferences.Key<Long>
    where K : Enum<*>,
          K : Key {
    if (BuildConfig.DEBUG) {
        require(name.endsWith(SUFFIX_LONG)) {
            "$this is used for Long, suffix \"$SUFFIX_LONG\" is required."
        }
    }
    return longPreferencesKey(name)
}
