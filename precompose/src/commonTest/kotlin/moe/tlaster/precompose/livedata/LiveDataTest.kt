package moe.tlaster.precompose.livedata

import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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
    fun testSameOwner() {
        var liveDataValue = 1
        val liveData = LiveData(liveDataValue)
        val lifecycleOwner1 = TestLifecycleOwner()
        val lifecycleOwner2 = TestLifecycleOwner()
        val observer = { value: Int ->
            liveDataValue = value
        }
        liveData.observe(lifecycleOwner1, observer)
        assertFailsWith(IllegalArgumentException::class) {
            liveData.observe(lifecycleOwner2, observer)
        }
    }

    @Test
    fun testAdd2ObserversWithSameOwnerAndRemove() {
        val initialValue = 1
        var v1 = initialValue
        var v2 = initialValue
        val liveData = LiveData(initialValue)
        val lifecycleOwner = TestLifecycleOwner()
        val o1 = { value: Int ->
            v1 = value
        }
        val o2 = { value: Int ->
            v2 = value
        }
        liveData.observe(lifecycleOwner, o1)
        liveData.observe(lifecycleOwner, o2)
        assertTrue(liveData.hasObserver())
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.Active
        liveData.value = 2
        assertEquals(2, v1)
        assertEquals(2, v2)
        liveData.removeObservers(lifecycleOwner)
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
        liveData.value = 3
        assertNotEquals(3, liveDataValue)
    }

    @Test
    fun testInactiveRegistry() {
        var liveDataValue = 1
        val liveData = LiveData(liveDataValue)
        val lifecycleOwner = TestLifecycleOwner()
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.Destroyed
        val observer = { value: Int ->
            liveDataValue = value
        }
        liveData.observe(lifecycleOwner, observer)
        assertFalse(liveData.hasObserver())
        assertFalse(lifecycleOwner.lifecycle.hasObserver())
    }

    @Test
    fun testObserverRemovalInCallback() {
        val liveData = LiveData(1)
        val lifecycleOwner = TestLifecycleOwner()
        val observer = object : TestObserver<Int> {
            override fun invoke(value: Int) {
                assertTrue(liveData.hasObserver())
                liveData.removeObserver(this)
                assertFalse(liveData.hasObserver())
            }
        }
        liveData.observe(lifecycleOwner, observer)
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.Active
        liveData.value = 2
        assertFalse(liveData.hasObserver())
    }

    @Test
    fun testSetValueDuringSetValue() {
        val liveData = LiveData(1)
        val lifecycleOwner = TestLifecycleOwner()
        val observer = object : TestObserver<Int> {
            override fun invoke(value: Int) {
                if (value == 2) {
                    liveData.value = 3
                }
            }
        }
        liveData.observe(lifecycleOwner, observer)
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.Active
        liveData.value = 2
        assertEquals(3, liveData.value)
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

interface TestObserver<T> : (T) -> Unit
