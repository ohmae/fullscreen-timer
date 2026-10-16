/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.ui.navigation

import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationEventState
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
@Suppress("NonAsciiCharacters")
class NavigationDisplayTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun `isPredictiveBackInProgress Idle状態のときはfalseを返すこと`() {
        val state = mockk<NavigationEventState<*>> {
            every { transitionState } returns NavigationEventTransitionState.Idle
        }

        assertThat(state.isPredictiveBackInProgress()).isFalse()
    }

    @Test
    fun `isPredictiveBackInProgress 前方遷移のInProgress状態のときはfalseを返すこと`() {
        val inProgress = mockk<NavigationEventTransitionState.InProgress> {
            every { direction } returns NavigationEventTransitionState.TRANSITIONING_FORWARD
        }
        val state = mockk<NavigationEventState<*>> {
            every { transitionState } returns inProgress
        }

        assertThat(state.isPredictiveBackInProgress()).isFalse()
    }

    @Test
    fun `isPredictiveBackInProgress 戻る遷移のInProgress状態のときはtrueを返すこと`() {
        val inProgress = mockk<NavigationEventTransitionState.InProgress> {
            every { direction } returns NavigationEventTransitionState.TRANSITIONING_BACK
        }
        val state = mockk<NavigationEventState<*>> {
            every { transitionState } returns inProgress
        }

        assertThat(state.isPredictiveBackInProgress()).isTrue()
    }

    @Test
    fun `NavigationDisplay 子画面表示中にシステム戻る操作で前の画面に戻ること`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val navigator = Navigator(
            backStack = NavBackStack<TestNavKey>(TestNavKey.Main),
            navGraph = navGraph {
                from<TestNavKey.Main> { to<TestNavKey.Details>() }
            },
            onExit = {},
        )
        try {
            controller.get().setContent {
                NavigationDisplay(
                    navigator = navigator,
                    entryProvider = entryProvider {
                        entry<TestNavKey.Main> { Box(Modifier.size(100.dp)) }
                        entry<TestNavKey.Details> { Box(Modifier.size(100.dp)) }
                    },
                )
            }
            composeRule.runOnIdle {
                assertThat(navigator.isNavigationReady).isTrue()
            }
            navigator.navigate(TestNavKey.Details)
            composeRule.runOnIdle {
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
                assertThat(navigator.isNavigationReady).isTrue()
            }

            controller.get().onBackPressedDispatcher.onBackPressed()
            composeRule.runOnIdle {
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main)
                assertThat(navigator.isNavigationReady).isTrue()
            }
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test
    fun `NavigationDisplay ルート画面では戻る操作を消費せず上位に委譲すること`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        var activityBackPressed = false
        controller.get().onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activityBackPressed = true
                }
            },
        )
        val navigator = Navigator(
            backStack = NavBackStack<TestNavKey>(TestNavKey.Main),
            navGraph = navGraph {
                from<TestNavKey.Main> { to<TestNavKey.Details>() }
            },
            onExit = {},
        )
        try {
            controller.get().setContent {
                NavigationDisplay(
                    navigator = navigator,
                    entryProvider = entryProvider {
                        entry<TestNavKey.Main> { Box(Modifier.size(100.dp)) }
                    },
                )
            }
            composeRule.runOnIdle {
                assertThat(navigator.isNavigationReady).isTrue()
            }

            controller.get().onBackPressedDispatcher.onBackPressed()
            composeRule.runOnIdle {
                assertThat(activityBackPressed).isTrue()
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main)
            }
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test
    fun `NavigationDisplay 画面遷移中の戻る操作はブロックされ遷移が完了すること`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        var activityBackPressed = false
        controller.get().onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activityBackPressed = true
                }
            },
        )
        val navigator = Navigator(
            backStack = NavBackStack<TestNavKey>(TestNavKey.Main),
            navGraph = navGraph {
                from<TestNavKey.Main> { to<TestNavKey.Details>() }
            },
            onExit = {},
        )
        try {
            controller.get().setContent {
                NavigationDisplay(
                    navigator = navigator,
                    entryProvider = entryProvider {
                        entry<TestNavKey.Main> { Box(Modifier.size(100.dp)) }
                        entry<TestNavKey.Details> { Box(Modifier.size(100.dp)) }
                    },
                )
            }
            composeRule.runOnIdle {
                assertThat(navigator.isNavigationReady).isTrue()
            }

            composeRule.mainClock.autoAdvance = false
            composeRule.runOnIdle { navigator.navigate(TestNavKey.Details) }
            Snapshot.sendApplyNotifications()
            composeRule.mainClock.advanceTimeBy(50)

            assertThat(navigator.isNavigationReady).isFalse()
            controller.get().onBackPressedDispatcher.onBackPressed()

            assertThat(activityBackPressed).isFalse()

            composeRule.mainClock.autoAdvance = true
            composeRule.runOnIdle {
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
                assertThat(navigator.isNavigationReady).isTrue()
            }
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test
    fun `navigate 遷移中の操作を保留してSceneが再開した後に実行すること`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val navigator = Navigator(
            backStack = NavBackStack<TestNavKey>(TestNavKey.Main),
            navGraph = navGraph {
                from<TestNavKey.Main> { to<TestNavKey.Details>() }
                from<TestNavKey.Details> { to<TestNavKey.Child>() }
            },
            onExit = {},
        )
        try {
            controller.get().setContent {
                NavigationDisplay(
                    navigator = navigator,
                    entryProvider = entryProvider {
                        entry<TestNavKey.Main> { Box(Modifier.size(100.dp)) }
                        entry<TestNavKey.Details> { Box(Modifier.size(100.dp)) }
                        entry<TestNavKey.Child> { Box(Modifier.size(100.dp)) }
                    },
                )
            }
            composeRule.runOnIdle {
                assertThat(navigator.isNavigationReady).isTrue()
            }
            composeRule.mainClock.autoAdvance = false
            composeRule.runOnIdle { navigator.navigate(TestNavKey.Details) }
            composeRule.mainClock.advanceTimeBy(100)
            composeRule.runOnIdle {
                assertThat(navigator.isNavigationReady).isFalse()
                navigator.navigate(TestNavKey.Child)
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
            }

            composeRule.mainClock.autoAdvance = true
            composeRule.runOnIdle {
                assertThat(navigator.backStack)
                    .containsExactly(TestNavKey.Main, TestNavKey.Details, TestNavKey.Child).inOrder()
                assertThat(navigator.isNavigationReady).isTrue()
            }
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test
    fun `NavigationDisplay 予測型戻るを確定すると再開待ちせず戻り連続した戻るは上位へ漏れないこと`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val navigator = createChildNavigator()
        var fallbackCount = 0
        controller.get().onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    fallbackCount++
                }
            },
        )
        try {
            controller.get().setContent { TestDisplay(navigator) }
            composeRule.runOnIdle { assertThat(navigator.isNavigationReady).isTrue() }
            val dispatcher = controller.get().onBackPressedDispatcher
            composeRule.mainClock.autoAdvance = false
            composeRule.runOnIdle {
                dispatcher.dispatchOnBackStarted(backEvent(0f))
            }
            Snapshot.sendApplyNotifications()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.runOnIdle { dispatcher.dispatchOnBackProgressed(backEvent(0.5f)) }
            Snapshot.sendApplyNotifications()
            composeRule.mainClock.advanceTimeBy(100)
            composeRule.runOnIdle {
                assertThat(navigator.isNavigationReady).isFalse()
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
                dispatcher.onBackPressed()
                // 確定時点でスタックが変わる必要がある。RESUMED待ちではジェスチャーが完了しない。
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main)
            }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.runOnIdle { dispatcher.onBackPressed() }
            assertThat(fallbackCount).isEqualTo(0)
            composeRule.mainClock.autoAdvance = true
            composeRule.runOnIdle {
                assertThat(navigator.isNavigationReady).isTrue()
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main)
                dispatcher.onBackPressed()
                assertThat(fallbackCount).isEqualTo(1)
            }
        } finally {
            composeRule.mainClock.autoAdvance = true
            controller.pause().stop().destroy()
        }
    }

    @Test
    fun `NavigationDisplay 予測型戻るのキャンセルで画面を維持し次の戻る操作が正常に完了すること`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val navigator = createChildNavigator()
        try {
            controller.get().setContent { TestDisplay(navigator) }
            composeRule.runOnIdle { assertThat(navigator.isNavigationReady).isTrue() }
            val dispatcher = controller.get().onBackPressedDispatcher
            composeRule.mainClock.autoAdvance = false
            composeRule.runOnIdle {
                dispatcher.dispatchOnBackStarted(backEvent(0f))
            }
            Snapshot.sendApplyNotifications()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.runOnIdle { dispatcher.dispatchOnBackProgressed(backEvent(0.5f)) }
            Snapshot.sendApplyNotifications()
            composeRule.mainClock.advanceTimeBy(100)
            composeRule.runOnIdle {
                assertThat(navigator.isNavigationReady).isFalse()
                dispatcher.dispatchOnBackCancelled()
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
            }
            composeRule.mainClock.autoAdvance = true
            composeRule.runOnIdle {
                assertThat(navigator.isNavigationReady).isTrue()
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
                dispatcher.onBackPressed()
            }
            composeRule.runOnIdle {
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main)
                assertThat(navigator.isNavigationReady).isTrue()
            }
        } finally {
            composeRule.mainClock.autoAdvance = true
            controller.pause().stop().destroy()
        }
    }

    @Test
    fun `NavigationDisplay 複数Entryを表示するSceneではシステム戻るでSceneの戻り先まで取り除くこと`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val navigator = createChildNavigator(NavBackStack(TestNavKey.Main, TestNavKey.Details, TestNavKey.Child))
        val strategy = SceneStrategy { entries ->
            if (entries.size == 3) PairScene(entries.takeLast(2), entries.take(1)) else null
        }
        try {
            controller.get().setContent {
                NavigationDisplay(
                    navigator = navigator,
                    sceneStrategies = listOf(strategy),
                    entryProvider = testEntryProvider,
                )
            }
            composeRule.runOnIdle { assertThat(navigator.isNavigationReady).isTrue() }

            composeRule.runOnIdle { controller.get().onBackPressedDispatcher.onBackPressed() }

            composeRule.runOnIdle {
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main)
                assertThat(navigator.isNavigationReady).isTrue()
            }
        } finally {
            controller.pause().stop().destroy()
        }
    }

    private fun createChildNavigator(
        backStack: NavBackStack<TestNavKey> = NavBackStack(TestNavKey.Main, TestNavKey.Details),
    ): Navigator<TestNavKey> =
        Navigator(
            backStack = backStack,
            navGraph = navGraph {
                from<TestNavKey.Main> { to<TestNavKey.Details>() }
                from<TestNavKey.Details> { to<TestNavKey.Child>() }
            },
            onExit = { error("システム戻るはルートで上位に委譲する") },
        )

    @Composable
    private fun TestDisplay(
        navigator: Navigator<TestNavKey>,
    ) {
        NavigationDisplay(navigator = navigator, entryProvider = testEntryProvider)
    }

    private val testEntryProvider = entryProvider<TestNavKey> {
        entry<TestNavKey.Main> { Box(Modifier.size(100.dp)) }
        entry<TestNavKey.Details> { Box(Modifier.size(100.dp)) }
        entry<TestNavKey.Child> { Box(Modifier.size(100.dp)) }
    }

    private fun backEvent(
        progress: Float,
    ): BackEventCompat = BackEventCompat(0f, 0f, progress, BackEventCompat.EDGE_LEFT)

    private data class PairScene(
        override val entries: List<NavEntry<TestNavKey>>,
        override val previousEntries: List<NavEntry<TestNavKey>>,
    ) : Scene<TestNavKey> {
        override val key: Any = "pair"
        override val content: @Composable () -> Unit = {
            entries.forEach { it.Content() }
        }
    }

    private sealed interface TestNavKey : NavKey {
        data object Main : TestNavKey

        data object Details : TestNavKey

        data object Child : TestNavKey
    }
}
