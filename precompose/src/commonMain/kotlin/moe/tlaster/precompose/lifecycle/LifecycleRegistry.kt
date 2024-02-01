package moe.tlaster.precompose.lifecycle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LifecycleRegistry : Lifecycle {
    private var observers: List<LifecycleObserver> = emptyList()
    private val _currentStateFlow = MutableStateFlow(Lifecycle.State.Initialized)
    override val currentStateFlow: StateFlow<Lifecycle.State> = _currentStateFlow.asStateFlow()

    private var _state: Lifecycle.State = _currentStateFlow.value
        set(value) {
            if (field == Lifecycle.State.Destroyed || value == Lifecycle.State.Initialized) {
                observers = emptyList()
                return
            }
            field = value
            _currentStateFlow.value = value
            dispatchState(value)
        }

    override val currentState: Lifecycle.State get() = _state

    fun updateState(value: Lifecycle.State) {
        _state = value
    }

    private fun dispatchState(value: Lifecycle.State) {
        observers.forEach {
            it.onStateChanged(value)
        }
    }

    override fun removeObserver(observer: LifecycleObserver) {
        observers -= observer
    }

    override fun addObserver(observer: LifecycleObserver) {
        if (observers.contains(observer)) {
            return
        }
        observers += observer
        observer.onStateChanged(currentState)
    }

    override fun hasObserver(): Boolean {
        return observers.isNotEmpty()
    }
}
