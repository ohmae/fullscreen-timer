/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.ui.navigation

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.Serializable
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
@Suppress("NonAsciiCharacters")
class NavigatorTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun `isNavigationReady 現在の画面がRESUMEDのときのみtrueになること`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val navigator = createNavigator(backStack)

        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.STARTED))

        assertThat(navigator.isNavigationReady).isFalse()

        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.RESUMED))

        assertThat(navigator.isNavigationReady).isTrue()

        navigator.navigate(TestNavKey.Details)

        assertThat(navigator.isNavigationReady).isFalse()
    }

    @Test
    fun `navigate fromが最前面と一致する場合は正常に遷移すること`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val navigator = createNavigator(backStack)
        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.RESUMED))

        navigator.navigate(from = TestNavKey.Main, to = TestNavKey.Details)

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
    }

    @Test
    fun `navigate fromが最前面と一致しない場合は即時でも実行されないこと`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val navigator = Navigator(
            backStack,
            navGraph {
                from<TestNavKey.Main> { to<TestNavKey.Child>() }
                from<TestNavKey.Details> { to<TestNavKey.Child>() }
            },
            {},
        )
        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.RESUMED))

        navigator.navigate(from = TestNavKey.Details, to = TestNavKey.Child)

        assertThat(backStack).containsExactly(TestNavKey.Main)
    }

    @Test
    fun `navigate fromが最前面と一致する場合は再開まで遷移を保留すること`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val navigator = createNavigator(backStack)
        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.STARTED))

        navigator.navigate(from = TestNavKey.Main, to = TestNavKey.Details)

        assertThat(backStack).containsExactly(TestNavKey.Main)

        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.RESUMED))

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
    }

    @Test
    fun `navigate 最前面以外からの遷移要求は保留枠を消費しないこと`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val navigator = Navigator(
            backStack,
            navGraph {
                from<TestNavKey.Main> {
                    to<TestNavKey.Details>()
                    to<TestNavKey.Child>()
                }
                from<TestNavKey.Details> { to<TestNavKey.Child>() }
            },
            {},
        )
        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.STARTED))

        navigator.navigate(from = TestNavKey.Details, to = TestNavKey.Child)
        navigator.navigate(from = TestNavKey.Main, to = TestNavKey.Details)
        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.RESUMED))

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
    }

    @Test
    fun `navigate fromが同じ型でも引数が異なれば遷移せず同値なら遷移すること`() {
        val current = TestNavKey.Item(1)
        val destination = TestNavKey.Item(3)
        val backStack = NavBackStack<TestNavKey>(current)
        val navigator = Navigator(
            backStack,
            navGraph { from<TestNavKey.Item> { to<TestNavKey.Item>() } },
            {},
        )
        navigator.attachLifecycle(current, lifecycle(Lifecycle.State.RESUMED))

        navigator.navigate(from = TestNavKey.Item(2), to = destination)

        assertThat(backStack).containsExactly(current)

        navigator.navigate(from = TestNavKey.Item(1), to = destination)

        assertThat(backStack).containsExactly(current, destination).inOrder()
    }

    @Test
    fun `navigate 遷移中の次の遷移を完了後に実行すること`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val navigator = createNavigator(backStack)
        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.RESUMED))

        navigator.navigate(TestNavKey.Details)
        navigator.navigate(TestNavKey.Child)

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()

        var lifecycleState = Lifecycle.State.STARTED
        val lifecycle = lifecycle { lifecycleState }
        navigator.attachLifecycle(TestNavKey.Details, lifecycle)
        navigator.navigate(TestNavKey.Child)

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()

        lifecycleState = Lifecycle.State.RESUMED
        navigator.updateNavigationReadiness()

        assertThat(backStack)
            .containsExactly(TestNavKey.Main, TestNavKey.Details, TestNavKey.Child)
            .inOrder()
    }

    @Test
    fun `goBack 遷移中の戻る操作を一件だけ保留すること`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        var exited = false
        val navigator = createNavigator(backStack) { exited = true }
        var mainLifecycleState = Lifecycle.State.STARTED
        val mainLifecycle = lifecycle { mainLifecycleState }
        navigator.attachLifecycle(TestNavKey.Main, mainLifecycle)
        var detailsLifecycleState = Lifecycle.State.STARTED
        val detailsLifecycle = lifecycle { detailsLifecycleState }
        navigator.attachLifecycle(TestNavKey.Details, detailsLifecycle)

        navigator.goBack()
        navigator.goBack()

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()

        detailsLifecycleState = Lifecycle.State.RESUMED
        navigator.updateNavigationReadiness()
        navigator.goBack()
        navigator.goBack()

        assertThat(backStack).containsExactly(TestNavKey.Main)
        assertThat(exited).isFalse()

        mainLifecycleState = Lifecycle.State.RESUMED
        navigator.updateNavigationReadiness()

        assertThat(exited).isTrue()
    }

    @Test
    fun `goBack fromが最前面と一致しない場合は即時でも実行されないこと`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        val navigator = createNavigator(backStack)
        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.RESUMED))

        navigator.goBack(from = TestNavKey.Main)

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
    }

    @Test
    fun `goBack fromが最前面と一致する場合は正常に戻ること`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        val navigator = createNavigator(backStack)
        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.RESUMED))

        navigator.goBack(from = TestNavKey.Details)

        assertThat(backStack).containsExactly(TestNavKey.Main)
    }

    @Test
    fun `goBack バックスタックがルート1件のときはonExitが呼ばれスタックが維持されること`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        var exited = false
        val navigator = createNavigator(backStack) { exited = true }
        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.RESUMED))

        navigator.goBack()

        assertThat(backStack).containsExactly(TestNavKey.Main)
        assertThat(exited).isTrue()
    }

    @Test
    fun `onSystemBack predictive back中は遷移を完了すること`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        val navigator = createNavigator(backStack)
        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.STARTED))

        navigator.onSystemBack(ignoreNavigationReady = false)

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()

        navigator.onSystemBack(ignoreNavigationReady = true)

        assertThat(backStack).containsExactly(TestNavKey.Main)
    }

    @Test
    fun `onSystemBack Sceneの複数Entryを一括で取り除き連続操作は保留すること`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details, TestNavKey.Child)
        var exited = false
        val navigator = createNavigator(backStack) { exited = true }
        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.STARTED))
        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.STARTED))
        navigator.attachLifecycle(TestNavKey.Child, lifecycle(Lifecycle.State.RESUMED))

        navigator.onSystemBack(ignoreNavigationReady = false, popCount = 2)
        navigator.onSystemBack(ignoreNavigationReady = false)

        assertThat(backStack).containsExactly(TestNavKey.Main)
        assertThat(navigator.isNavigationReady).isFalse()
        assertThat(exited).isFalse()
    }

    @Test
    fun `onSystemBack 遷移中は複数Entryの削除を保留し予測型戻るなら許可すること`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details, TestNavKey.Child)
        val navigator = createNavigator(backStack)
        navigator.attachLifecycle(TestNavKey.Child, lifecycle(Lifecycle.State.STARTED))

        navigator.onSystemBack(ignoreNavigationReady = false, popCount = 2)

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details, TestNavKey.Child).inOrder()

        navigator.onSystemBack(ignoreNavigationReady = true, popCount = 2)

        assertThat(backStack).containsExactly(TestNavKey.Main)
    }

    @Test
    fun `onSystemBack 削除件数が過大でもルートを残すこと`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        var exited = false
        val navigator = createNavigator(backStack) { exited = true }
        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.RESUMED))

        navigator.onSystemBack(ignoreNavigationReady = false, popCount = 10)

        assertThat(backStack).containsExactly(TestNavKey.Main)
        assertThat(exited).isFalse()
    }

    @Test
    fun `onSystemBack 削除件数がゼロ以下なら終了しないこと`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        var exited = false
        val navigator = createNavigator(backStack) { exited = true }
        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.RESUMED))

        navigator.onSystemBack(ignoreNavigationReady = false, popCount = 0)
        navigator.onSystemBack(ignoreNavigationReady = false, popCount = -1)

        assertThat(backStack).containsExactly(TestNavKey.Main)
        assertThat(exited).isFalse()
    }

    @Test
    fun `navigate 保留済みなら別の操作で上書きしないこと`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        val navigator = createNavigator(backStack)
        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.STARTED))

        navigator.navigate(TestNavKey.Child)
        navigator.goBack()
        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.RESUMED))

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details, TestNavKey.Child).inOrder()

        navigator.attachLifecycle(TestNavKey.Child, lifecycle(Lifecycle.State.RESUMED))
        navigator.updateNavigationReadiness()

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details, TestNavKey.Child).inOrder()
    }

    @Test
    fun `goBack 保留中に次のnavigateが来ても先着の戻るを維持すること`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        val navigator = createNavigator(backStack)
        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.STARTED))

        navigator.goBack()
        navigator.navigate(TestNavKey.Child)
        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.RESUMED))

        assertThat(backStack).containsExactly(TestNavKey.Main)
    }

    @Test
    fun `onSystemBack 保留した複数Entryの削除件数を維持すること`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details, TestNavKey.Child)
        val navigator = createNavigator(backStack)
        navigator.attachLifecycle(TestNavKey.Child, lifecycle(Lifecycle.State.STARTED))

        navigator.onSystemBack(ignoreNavigationReady = false, popCount = 2)
        navigator.attachLifecycle(TestNavKey.Child, lifecycle(Lifecycle.State.RESUMED))

        assertThat(backStack).containsExactly(TestNavKey.Main)
    }

    @Test
    fun `onSystemBack 予測型戻るの確定後に保留した戻るで終了しないこと`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        var exited = false
        val navigator = createNavigator(backStack) { exited = true }
        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.STARTED))

        navigator.goBack()
        navigator.onSystemBack(ignoreNavigationReady = true)
        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.RESUMED))

        assertThat(backStack).containsExactly(TestNavKey.Main)
        assertThat(exited).isFalse()
        assertThat(navigator.isNavigationReady).isTrue()
    }

    @Test
    fun `onSystemBack 予測型戻るの確定後に保留した遷移を実行しないこと`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details, TestNavKey.Child)
        val navigator = Navigator(
            backStack = backStack,
            navGraph = object : NavGraph<TestNavKey> {
                override fun canNavigate(
                    from: TestNavKey,
                    to: TestNavKey,
                ): Boolean = true
            },
            onExit = {},
        )
        navigator.attachLifecycle(TestNavKey.Child, lifecycle(Lifecycle.State.STARTED))

        navigator.navigate(TestNavKey.Main)
        navigator.onSystemBack(ignoreNavigationReady = true)
        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.RESUMED))

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
    }

    @Test
    fun `navigate 同じ画面や許可されない遷移は保留操作を消費しないこと`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val navigator = createNavigator(backStack)
        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.STARTED))

        navigator.navigate(TestNavKey.Main)
        navigator.navigate(TestNavKey.Child)
        navigator.navigate(TestNavKey.Details)
        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.RESUMED))

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
    }

    @Test
    fun `detachLifecycle 古い画面の破棄で新しいライフサイクルを解除しないこと`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val navigator = createNavigator(backStack)
        val oldLifecycle = lifecycle(Lifecycle.State.STARTED)
        val newLifecycle = lifecycle(Lifecycle.State.RESUMED)
        navigator.attachLifecycle(TestNavKey.Main, oldLifecycle)
        navigator.attachLifecycle(TestNavKey.Main, newLifecycle)

        navigator.detachLifecycle(TestNavKey.Main, oldLifecycle)

        assertThat(navigator.isNavigationReady).isTrue()

        navigator.detachLifecycle(TestNavKey.Main, newLifecycle)

        assertThat(navigator.isNavigationReady).isFalse()
    }

    @Test
    fun `detachLifecycle 最前面のライフサイクルが解除されたら操作不可になること`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        val navigator = createNavigator(backStack)
        val detailsLifecycle = lifecycle(Lifecycle.State.RESUMED)
        val mainLifecycle = lifecycle(Lifecycle.State.STARTED)
        navigator.attachLifecycle(TestNavKey.Main, mainLifecycle)
        navigator.attachLifecycle(TestNavKey.Details, detailsLifecycle)

        assertThat(navigator.isNavigationReady).isTrue()

        navigator.detachLifecycle(TestNavKey.Details, detailsLifecycle)

        assertThat(navigator.isNavigationReady).isFalse()
    }

    @Test
    fun `detachLifecycle 最前面以外のライフサイクルが解除されても最前面がRESUMEDなら操作可能を維持すること`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        val navigator = createNavigator(backStack)
        val detailsLifecycle = lifecycle(Lifecycle.State.RESUMED)
        val mainLifecycle = lifecycle(Lifecycle.State.STARTED)
        navigator.attachLifecycle(TestNavKey.Main, mainLifecycle)
        navigator.attachLifecycle(TestNavKey.Details, detailsLifecycle)

        navigator.detachLifecycle(TestNavKey.Main, mainLifecycle)

        assertThat(navigator.isNavigationReady).isTrue()
    }

    @Test
    fun `rememberNavigator 単一のinitialKeyでバックスタックが初期化されること`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        var navigator: Navigator<TestNavKey>? = null
        controller.get().setContent {
            navigator = rememberNavigator(
                initialKey = TestNavKey.Main,
                navGraph = navGraph {
                    from<TestNavKey.Main> { to<TestNavKey.Details>() }
                },
                onExit = {},
            )
        }

        assertThat(navigator).isNotNull()
        assertThat(navigator?.backStack).containsExactly(TestNavKey.Main)
    }

    @Test
    fun `rememberNavigator 保存後の再生成で引数と順序を復元し復元先から戻れること`() {
        val graph = navGraph {
            from<TestNavKey.Details> { to<TestNavKey.Item>() }
        }
        lateinit var navigator: Navigator<TestNavKey>
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val savedState = Bundle()
        val content: @Composable () -> Unit = {
            navigator = rememberNavigator(
                initialKeys = listOf(TestNavKey.Main, TestNavKey.Details),
                navGraph = graph,
                onExit = {},
            )
        }
        try {
            controller.get().setContent(content = content)
            composeRule.runOnIdle {
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
                navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.RESUMED))
                navigator.navigate(TestNavKey.Item(42))
            }
            composeRule.waitForIdle()
            controller.saveInstanceState(savedState)
        } finally {
            controller.pause().stop().destroy()
        }
        val originalNavigator = navigator
        val restoredController = Robolectric.buildActivity(ComponentActivity::class.java)
            .create(savedState).start().resume().visible()
        try {
            restoredController.get().setContent(content = content)
            composeRule.runOnIdle {
                assertThat(navigator).isNotSameInstanceAs(originalNavigator)
                assertThat(navigator.backStack)
                    .containsExactly(TestNavKey.Main, TestNavKey.Details, TestNavKey.Item(42)).inOrder()
                assertThat(navigator.isNavigationReady).isFalse()
                navigator.goBack(TestNavKey.Item(42))
                assertThat(navigator.backStack).hasSize(3)
                navigator.attachLifecycle(TestNavKey.Item(42), lifecycle(Lifecycle.State.RESUMED))
                assertThat(navigator.backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
            }
        } finally {
            restoredController.pause().stop().destroy()
        }
    }

    @Test
    fun `rememberNavigator 単一要素のinitialKeysでバックスタックが初期化されること`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        var navigator: Navigator<TestNavKey>? = null
        controller.get().setContent {
            navigator = rememberNavigator(
                initialKeys = listOf(TestNavKey.Main),
                navGraph = navGraph {
                    from<TestNavKey.Main> { to<TestNavKey.Details>() }
                },
                onExit = {},
            )
        }

        assertThat(navigator).isNotNull()
        assertThat(navigator?.backStack).containsExactly(TestNavKey.Main)
    }

    @Test
    fun `rememberNavigator 空のinitialKeysでIllegalArgumentExceptionが発生すること`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        assertThrows(IllegalArgumentException::class.java) {
            controller.get().setContent {
                rememberNavigator(
                    initialKeys = emptyList<TestNavKey>(),
                    navGraph = navGraph {},
                    onExit = {},
                )
            }
        }
    }

    @Test
    fun `rememberNavigator onExitが更新された場合に最新のコールバックが呼ばれること`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val testNavGraph = navGraph<TestNavKey> {}
        var exitCallCount1 = 0
        var exitCallCount2 = 0
        var onExitAction by mutableStateOf<() -> Unit>({ exitCallCount1++ })
        var navigator: Navigator<TestNavKey>? = null

        try {
            controller.get().setContent {
                navigator = rememberNavigator(
                    backStack = backStack,
                    navGraph = testNavGraph,
                    onExit = onExitAction,
                )
            }

            val initialNavigator = navigator
            assertThat(initialNavigator).isNotNull()

            onExitAction = { exitCallCount2++ }
            composeRule.waitForIdle()

            initialNavigator?.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.RESUMED))
            initialNavigator?.goBack()

            assertThat(navigator).isSameInstanceAs(initialNavigator)
            assertThat(exitCallCount1).isEqualTo(0)
            assertThat(exitCallCount2).isEqualTo(1)
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test
    fun `navGraph node 単一の宛先を登録できること`() {
        val graph = navGraph {
            from<TestNavKey.Main> {
                to<TestNavKey.Details>()
            }
        }

        assertThat(graph.canNavigate(TestNavKey.Main, TestNavKey.Details)).isTrue()
        assertThat(graph.canNavigate(TestNavKey.Main, TestNavKey.Child)).isFalse()
        assertThat(graph.canNavigate(TestNavKey.Details, TestNavKey.Main)).isFalse()
    }

    @Test
    fun `navGraph node 同一起点への複数回設定で宛先がマージされること`() {
        val graph = navGraph {
            from<TestNavKey.Main> {
                to<TestNavKey.Details>()
            }
            from<TestNavKey.Main> {
                to<TestNavKey.Child>()
            }
        }

        assertThat(graph.canNavigate(TestNavKey.Main, TestNavKey.Details)).isTrue()
        assertThat(graph.canNavigate(TestNavKey.Main, TestNavKey.Child)).isTrue()
    }

    @Test
    fun `canNavigate 許可された遷移先であればtrueを返すこと`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val navigator = createNavigator(backStack)

        assertThat(navigator.canNavigate(TestNavKey.Details)).isTrue()
    }

    @Test
    fun `canNavigate 同一の画面であればfalseを返すこと`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val navigator = createNavigator(backStack)

        assertThat(navigator.canNavigate(TestNavKey.Main)).isFalse()
    }

    @Test
    fun `canNavigate 許可されていない遷移先であればfalseを返すこと`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val navigator = createNavigator(backStack)

        assertThat(navigator.canNavigate(TestNavKey.Child)).isFalse()
    }

    @Test
    fun `canNavigate バックスタックが空であればfalseを返すこと`() {
        val backStack = NavBackStack<TestNavKey>()
        val navigator = createNavigator(backStack)

        assertThat(navigator.canNavigate(TestNavKey.Details)).isFalse()
    }

    @Test
    fun `navigate バックスタックが空のときは何もしないこと`() {
        val backStack = NavBackStack<TestNavKey>()
        val navigator = createNavigator(backStack)

        navigator.navigate(TestNavKey.Main)
        navigator.navigate(from = TestNavKey.Main, to = TestNavKey.Details)

        assertThat(backStack).isEmpty()
    }

    @Test
    fun `canGoBack バックスタックが複数あればtrueを返すこと`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        val navigator = createNavigator(backStack)

        assertThat(navigator.canGoBack()).isTrue()
    }

    @Test
    fun `canGoBack バックスタックが1件であればfalseを返すこと`() {
        val backStack = NavBackStack<TestNavKey>(TestNavKey.Main)
        val navigator = createNavigator(backStack)

        assertThat(navigator.canGoBack()).isFalse()
    }

    @Test
    fun `canGoBack fromが最前面と一致していればtrueを返すこと`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        val navigator = createNavigator(backStack)

        assertThat(navigator.canGoBack(TestNavKey.Details)).isTrue()
    }

    @Test
    fun `canGoBack fromが最前面と一致していなければfalseを返すこと`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        val navigator = createNavigator(backStack)

        assertThat(navigator.canGoBack(TestNavKey.Main)).isFalse()
    }

    @Test
    fun `canGoBack バックスタックが空であればfalseを返すこと`() {
        val backStack = NavBackStack<TestNavKey>()
        val navigator = createNavigator(backStack)

        assertThat(navigator.canGoBack()).isFalse()
    }

    @Test
    fun `rememberNavigatorNavEntryDecorator ライフサイクルの変化で操作可否を更新すること`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val navigator = createNavigator(NavBackStack(TestNavKey.Main))
        val owner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry.createUnsafe(this)
        }
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        controller.get().setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                val entries = rememberDecoratedNavEntries(
                    backStack = navigator.backStack,
                    entryDecorators = listOf(rememberNavigatorNavEntryDecorator(navigator)),
                    entryProvider = entryProvider { entry<TestNavKey.Main> {} },
                )
                entries.first().Content()
            }
        }

        assertThat(navigator.isNavigationReady).isFalse()
        owner.lifecycle.currentState = Lifecycle.State.RESUMED
        assertThat(navigator.isNavigationReady).isTrue()
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        assertThat(navigator.isNavigationReady).isFalse()
        owner.lifecycle.currentState = Lifecycle.State.RESUMED
        assertThat(navigator.isNavigationReady).isTrue()

        controller.pause().stop().destroy()
    }

    @Test
    fun `rememberNavigatorNavEntryDecorator 描画を破棄すると監視とライフサイクルの登録を解除すること`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val navigator = createNavigator(NavBackStack(TestNavKey.Main))
        val owner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry.createUnsafe(this)
        }
        owner.lifecycle.currentState = Lifecycle.State.RESUMED
        val initialObserverCount = owner.lifecycle.observerCount
        controller.get().setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                val entries = rememberDecoratedNavEntries(
                    backStack = navigator.backStack,
                    entryDecorators = listOf(rememberNavigatorNavEntryDecorator(navigator)),
                    entryProvider = entryProvider { entry<TestNavKey.Main> {} },
                )
                entries.first().Content()
            }
        }
        assertThat(navigator.isNavigationReady).isTrue()
        assertThat(owner.lifecycle.observerCount).isGreaterThan(initialObserverCount)

        val content = controller.get().findViewById<ViewGroup>(android.R.id.content)
        (content.getChildAt(0) as AbstractComposeView).disposeComposition()

        assertThat(navigator.isNavigationReady).isFalse()
        assertThat(owner.lifecycle.observerCount).isEqualTo(initialObserverCount)
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        owner.lifecycle.currentState = Lifecycle.State.RESUMED
        assertThat(navigator.isNavigationReady).isFalse()

        controller.pause().stop().destroy()
    }

    @Test
    fun `goBack 最前面以外からの戻る要求は保留枠を消費しないこと`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        val navigator = createNavigator(backStack)

        navigator.goBack(TestNavKey.Main)
        navigator.navigate(TestNavKey.Child)
        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.RESUMED))

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details, TestNavKey.Child).inOrder()
    }

    @Test
    fun `navigate 保留後に遷移が禁止された場合は再開時に破棄し次の操作を受け付けること`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        var allowed = true
        val navigator = Navigator(
            backStack,
            object : NavGraph<TestNavKey> {
                override fun canNavigate(
                    from: TestNavKey,
                    to: TestNavKey,
                ): Boolean = allowed
            },
            {},
        )
        navigator.navigate(TestNavKey.Child)
        allowed = false

        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.RESUMED))

        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()
        assertThat(navigator.isNavigationReady).isTrue()
        navigator.goBack()
        assertThat(backStack).containsExactly(TestNavKey.Main)
    }

    @Test
    fun `navigate 同じ型でも引数が異なる画面へ遷移でき同値のキーへの連打は無視すること`() {
        val first = TestNavKey.Item(1)
        val second = TestNavKey.Item(2)
        val backStack = NavBackStack<TestNavKey>(first)
        val navigator = Navigator(
            backStack,
            navGraph { from<TestNavKey.Item> { to<TestNavKey.Item>() } },
            {},
        )
        navigator.attachLifecycle(first, lifecycle(Lifecycle.State.RESUMED))

        assertThat(navigator.canNavigate(TestNavKey.Item(1))).isFalse()
        assertThat(navigator.canNavigate(second)).isTrue()
        navigator.navigate(TestNavKey.Item(1))
        navigator.navigate(second)
        navigator.goBack(first)
        navigator.attachLifecycle(second, lifecycle(Lifecycle.State.RESUMED))

        assertThat(backStack).containsExactly(first, second).inOrder()
        navigator.goBack(TestNavKey.Item(2))
        assertThat(backStack).containsExactly(first)
    }

    @Test
    fun `attachLifecycle 背面の再開では保留操作を実行せず最前面の再開で一度だけ実行すること`() {
        val backStack = NavBackStack(TestNavKey.Main, TestNavKey.Details)
        var exitCount = 0
        val navigator = createNavigator(backStack) { exitCount++ }
        navigator.goBack()

        navigator.attachLifecycle(TestNavKey.Main, lifecycle(Lifecycle.State.RESUMED))
        navigator.updateNavigationReadiness()
        assertThat(navigator.isNavigationReady).isFalse()
        assertThat(backStack).containsExactly(TestNavKey.Main, TestNavKey.Details).inOrder()

        navigator.attachLifecycle(TestNavKey.Details, lifecycle(Lifecycle.State.RESUMED))
        navigator.updateNavigationReadiness()
        assertThat(backStack).containsExactly(TestNavKey.Main)
        assertThat(navigator.isNavigationReady).isTrue()
        assertThat(exitCount).isEqualTo(0)
    }

    private fun createNavigator(
        backStack: NavBackStack<TestNavKey>,
        onExit: () -> Unit = {},
    ): Navigator<TestNavKey> =
        Navigator(
            backStack = backStack,
            navGraph = navGraph {
                from<TestNavKey.Main> { to<TestNavKey.Details>() }
                from<TestNavKey.Details> { to<TestNavKey.Child>() }
            },
            onExit = onExit,
        )

    private fun lifecycle(
        state: Lifecycle.State,
    ): Lifecycle = lifecycle { state }

    private fun lifecycle(
        state: () -> Lifecycle.State,
    ): Lifecycle =
        mockk {
            every { currentState } answers { state() }
        }

    @Serializable
    private sealed interface TestNavKey : NavKey {
        @Serializable
        data object Main : TestNavKey

        @Serializable
        data object Details : TestNavKey

        @Serializable
        data class Item(
            val id: Int,
        ) : TestNavKey

        @Serializable
        data object Child : TestNavKey
    }
}
