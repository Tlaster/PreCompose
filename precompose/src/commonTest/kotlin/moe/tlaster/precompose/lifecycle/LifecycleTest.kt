package moe.tlaster.precompose.lifecycle

import kotlin.test.Test
import kotlin.test.assertEquals

class LifecycleTest {
    @Test
    fun lifeCycleTest() {
        val lifecycleOwner = TestLifecycleOwner()
        assertEquals(Lifecycle.State.Initialized, lifecycleOwner.lifecycle.currentState)
        lifecycleOwner.lifecycle.updateState(Lifecycle.State.Active)
        assertEquals(Lifecycle.State.Active, lifecycleOwner.lifecycle.currentState)
        lifecycleOwner.lifecycle.updateState(Lifecycle.State.Initialized)
        assertEquals(Lifecycle.State.Active, lifecycleOwner.lifecycle.currentState)
        lifecycleOwner.lifecycle.updateState(Lifecycle.State.InActive)
        assertEquals(Lifecycle.State.InActive, lifecycleOwner.lifecycle.currentState)
        lifecycleOwner.lifecycle.updateState(Lifecycle.State.Active)
        assertEquals(Lifecycle.State.Active, lifecycleOwner.lifecycle.currentState)
        lifecycleOwner.lifecycle.updateState(Lifecycle.State.Destroyed)
        assertEquals(Lifecycle.State.Destroyed, lifecycleOwner.lifecycle.currentState)
        lifecycleOwner.lifecycle.updateState(Lifecycle.State.Active)
        assertEquals(Lifecycle.State.Destroyed, lifecycleOwner.lifecycle.currentState)
    }
}
