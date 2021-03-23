package moe.tlaster.precompose.livedata

import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LiveDataTest {
    @Test
    fun testObserverToggle() {
        var liveDataValue = 1
        val liveData = LiveData(liveDataValue)
        val lifecycleOwner = TestLifecycleOwner()
        val observer = { value: Int ->
            liveDataValue = value
        }
        liveData.observe(lifecycleOwner, observer)
        assertTrue(liveData.hasObserver())
        assertTrue(lifecycleOwner.lifecycle.hasObserver())
        liveData.removeObserver(observer)
        assertFalse(liveData.hasObserver())
        assertFalse(lifecycleOwner.lifecycle.hasObserver())
    }

    @Test
    fun testRemoveDestroyedObserver() {
        var liveDataValue = 1
        val liveData = LiveData(liveDataValue)
        val lifecycleOwner = TestLifecycleOwner()
        val observer = { value: Int ->
            liveDataValue = value
        }
        liveData.observe(lifecycleOwner, observer)
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.Active
        assertTrue(liveData.hasObserver())
        assertTrue(lifecycleOwner.lifecycle.hasObserver())
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.Destroyed
        assertFalse(liveData.hasObserver())
        assertFalse(lifecycleOwner.lifecycle.hasObserver())
    }

    @Test
    fun testNotifyActiveInactive() {
        var liveDataValue = 1
        val liveData = LiveData(liveDataValue)
        val lifecycleOwner = TestLifecycleOwner()
        val observer = { value: Int ->
            liveDataValue = value
        }
        liveData.observe(lifecycleOwner, observer)
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.Active
        liveData.value = 2
        assertEquals(2, liveDataValue)
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.InActive
        liveData.value = 3
        assertEquals(2, liveDataValue)
        liveData.value = 4
        assertEquals(2, liveDataValue)
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.Active
        assertEquals(4, liveDataValue)
    }
}

class TestLifecycleOwner : LifecycleOwner {
    override val lifecycle by lazy {
        LifecycleRegistry()
    }
}
