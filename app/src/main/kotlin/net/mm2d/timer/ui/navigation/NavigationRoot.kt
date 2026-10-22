/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.ui.navigation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import net.mm2d.timer.ColorSelectScreen
import net.mm2d.timer.LicenseScreen
import net.mm2d.timer.MainScreen
import net.mm2d.timer.SettingsScreen
import net.mm2d.timer.ui.navigation.MainNavKey.ColorSelect
import net.mm2d.timer.ui.navigation.MainNavKey.License
import net.mm2d.timer.ui.navigation.MainNavKey.Main
import net.mm2d.timer.ui.navigation.MainNavKey.Settings

private val navGraph: NavGraph<MainNavKey> = navGraph {
    from<Main> {
        to<Settings>()
    }
    from<Settings> {
        to<License>()
        to<ColorSelect>()
    }
}

@Composable
fun NavigationRoot() {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val navigator = rememberNavigator(Main, navGraph) {
        onBackPressedDispatcher?.onBackPressed()
    }
    val entryProvider = remember(navigator) { mainEntryProvider(navigator) }
    NavigationDisplay(
        navigator = navigator,
        entryProvider = entryProvider,
    )
}

private fun mainEntryProvider(
    navigator: Navigator<MainNavKey>,
): (MainNavKey) -> NavEntry<MainNavKey> =
    entryProvider {
        entry<Main> { navKey ->
            MainScreen(
                navigateToSettings = {
                    navigator.navigate(from = navKey, to = Settings)
                },
            )
        }
        entry<Settings> { navKey ->
            SettingsScreen(
                navigateToLicense = {
                    navigator.navigate(from = navKey, to = License)
                },
                navigateToColorSelect = { target ->
                    navigator.navigate(from = navKey, to = ColorSelect(target))
                },
                goBack = {
                    navigator.goBack(navKey)
                },
            )
        }
        entry<License> { navKey ->
            LicenseScreen(
                onBackClick = {
                    navigator.goBack(navKey)
                },
            )
        }
        entry<ColorSelect> { navKey ->
            ColorSelectScreen(
                target = navKey.target,
                goBack = {
                    navigator.goBack(navKey)
                },
            )
        }
    }
