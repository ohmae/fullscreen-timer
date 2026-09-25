/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.settings

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import net.mm2d.timer.BuildConfig
import org.junit.Test

@Suppress("NonAsciiCharacters")
class MigrationForVersionTest {
    private val migration = SettingsRepository.MigrationForVersion()

    @Test
    fun `shouldMigrate 最終起動バージョンが現在のバージョンと異なる場合はtrueを返す`() =
        runTest {
            val preferences = mutablePreferencesOf(
                SettingsRepository.VERSION_AT_LAST_LAUNCHED to BuildConfig.VERSION_CODE - 1,
            )

            val shouldMigrate = migration.shouldMigrate(preferences)

            assertThat(shouldMigrate).isTrue()
        }

    @Test
    fun `shouldMigrate 最終起動バージョンが現在のバージョンと一致する場合はfalseを返す`() =
        runTest {
            val preferences = mutablePreferencesOf(
                SettingsRepository.VERSION_AT_LAST_LAUNCHED to BuildConfig.VERSION_CODE,
            )

            val shouldMigrate = migration.shouldMigrate(preferences)

            assertThat(shouldMigrate).isFalse()
        }

    @Test
    fun `migrate 初回起動時にインストールバージョンと最終起動バージョンを設定する`() =
        runTest {
            val initialPreferences = emptyPreferences()

            val migrated = migration.migrate(initialPreferences)

            assertThat(migrated[SettingsRepository.VERSION_AT_INSTALL]).isEqualTo(BuildConfig.VERSION_CODE)
            assertThat(migrated[SettingsRepository.VERSION_AT_LAST_LAUNCHED]).isEqualTo(BuildConfig.VERSION_CODE)
            assertThat(migrated[SettingsRepository.VERSION_BEFORE_UPDATE]).isNull()
        }

    @Test
    fun `migrate バージョン更新時にインストールバージョンを保持し更新前バージョンを記録する`() =
        runTest {
            val initialInstallVersion = 100
            val lastLaunchedVersion = 150
            val initialPreferences = mutablePreferencesOf(
                SettingsRepository.VERSION_AT_INSTALL to initialInstallVersion,
                SettingsRepository.VERSION_AT_LAST_LAUNCHED to lastLaunchedVersion,
            )

            val migrated = migration.migrate(initialPreferences)

            assertThat(migrated[SettingsRepository.VERSION_AT_INSTALL]).isEqualTo(initialInstallVersion)
            assertThat(migrated[SettingsRepository.VERSION_BEFORE_UPDATE]).isEqualTo(lastLaunchedVersion)
            assertThat(migrated[SettingsRepository.VERSION_AT_LAST_LAUNCHED]).isEqualTo(BuildConfig.VERSION_CODE)
        }
}
