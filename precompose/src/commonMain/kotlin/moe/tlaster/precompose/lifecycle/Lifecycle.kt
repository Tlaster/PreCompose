package moe.tlaster.precompose.lifecycle

import kotlinx.coroutines.flow.StateFlow

interface Lifecycle {
    enum class State {
        Initialized,
        Active,
        InActive,
        Destroyed,
    }

    val currentState: State
    val currentStateFlow: StateFlow<State>
    fun removeObserver(observer: LifecycleObserver)
    fun addObserver(observer: LifecycleObserver)
    fun hasObserver(): Boolean
}
