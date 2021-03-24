package moe.tlaster.precompose.lifecycle

import moe.tlaster.precompose.standard.copyForEach

class LifecycleRegistry : Lifecycle {
    private val observers = arrayListOf<LifecycleObserver>()
    override var currentState: Lifecycle.State = Lifecycle.State.Initialized
        set(value) {
            field = value
            dispatchState(value)
        }

    private fun dispatchState(value: Lifecycle.State) {
        observers.copyForEach {
            it.onStateChanged(value)
        }
    }

    override fun removeObserver(observer: LifecycleObserver) {
        observers.remove(observer)
    }

    override fun addObserver(observer: LifecycleObserver) {
        observers.add(observer)
    }

    override fun hasObserver(): Boolean {
        return observers.isNotEmpty()
    }
}
