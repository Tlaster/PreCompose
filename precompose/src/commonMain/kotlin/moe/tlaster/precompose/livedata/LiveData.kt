package moe.tlaster.precompose.livedata

import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleObserver
import moe.tlaster.precompose.lifecycle.LifecycleOwner

class LiveData<T>(initialValue: T) {
    private val observers = linkedMapOf<Observer<T>, ObserverWrapper>()
    var value: T = initialValue
        set(value) {
            field = value
            dispatchingValue(value)
        }

    fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        if (owner.lifecycle.currentState == Lifecycle.State.Destroyed) {
            return
        }
        val existing = observers[observer]
        if (existing != null && !existing.isAttachedTo(owner)) {
            throw IllegalArgumentException("Cannot add the same observer with different lifecycles")
        }
        if (existing != null) {
            return
        }
        val wrapper = observers.getOrPut(observer) {
            LifecycleBoundObserver(owner, observer)
        }
        owner.lifecycle.addObserver(wrapper)
    }

    private fun dispatchingValue(observerWrapper: ObserverWrapper, value: T) {
        considerNotify(observerWrapper, value)
    }

    private fun dispatchingValue(value: T) {
        observers.forEach {
            dispatchingValue(it.value, value)
        }
    }

    private fun considerNotify(observer: ObserverWrapper, value: T) {
        if (!observer.active) {
            return
        }
        if (!observer.shouldBeActive()) {
            observer.activeStateChanged(false)
            return
        }
        observer.observer.invoke(value)
    }

    private abstract inner class ObserverWrapper(
        val observer: Observer<T>,
    ) : LifecycleObserver {
        var active = false
        abstract fun shouldBeActive(): Boolean
        open fun isAttachedTo(owner: LifecycleOwner): Boolean {
            return false
        }

        open fun detachObserver() {}

        open fun activeStateChanged(newActive: Boolean) {
            if (newActive == active) {
                return
            }
            active = newActive
            if (active) {
                dispatchingValue(this, value)
            }
        }
    }

    private inner class LifecycleBoundObserver(
        private val owner: LifecycleOwner,
        observer: Observer<T>,
    ) : ObserverWrapper(observer) {
        override fun onStateChanged(state: Lifecycle.State) {
            val currentState = owner.lifecycle.currentState
            if (currentState == Lifecycle.State.Destroyed) {
                detachObserver()
                return
            }
            activeStateChanged(shouldBeActive())
        }

        override fun isAttachedTo(owner: LifecycleOwner): Boolean {
            return this.owner === owner
        }

        override fun detachObserver() {
            owner.lifecycle.removeObserver(this)
        }

        override fun shouldBeActive(): Boolean {
            return owner.lifecycle.currentState == Lifecycle.State.Active
        }
    }
}
