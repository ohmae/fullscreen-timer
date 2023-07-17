package net.mm2d.timer.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

fun <T> Flow<T>.observe(
    owner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit,
) = flowWithLifecycle(owner.lifecycle, minActiveState)
    .distinctUntilChanged()
    .apply { owner.lifecycleScope.launch { collect { action(it) } } }

fun <T> Flow<T>.observeOnce(
    owner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit,
) = flowWithLifecycle(owner.lifecycle, minActiveState)
    .apply { owner.lifecycleScope.launch { action(first()) } }
