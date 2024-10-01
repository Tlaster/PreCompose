package moe.tlaster.precompose.navigation

import androidx.lifecycle.Lifecycle
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

fun runMainTest(block: () -> Unit) = runTest {
    withContext(Dispatchers.Main.immediate) {
        block()
    }
}

class BackStackEntryTest {
    @Test
    fun testActive() = runMainTest {
        val parentStateHolder = TestViewModelStoreProvider()
        val entry = BackStackEntry(
            uuid4().toString(),
            TestRoute("foo/bar", "foo/bar"),
            "foo/bar",
            emptyMap(),
            parentStateHolder,
        )
        entry.viewModelStore
        assertTrue(parentStateHolder.contains(entry.stateId))
        assertEquals(Lifecycle.State.CREATED, entry.lifecycle.currentState)
        entry.active()
        assertEquals(Lifecycle.State.RESUMED, entry.lifecycle.currentState)
    }

    @Test
    fun testInActive() = runMainTest {
        val parentStateHolder = TestViewModelStoreProvider()
        val entry = BackStackEntry(
            uuid4().toString(),
            TestRoute("foo/bar", "foo/bar"),
            "foo/bar",
            emptyMap(),
            parentStateHolder,
        )
        entry.viewModelStore
        entry.active()
        assertEquals(Lifecycle.State.RESUMED, entry.lifecycle.currentState)
        entry.inActive()
        assertEquals(Lifecycle.State.CREATED, entry.lifecycle.currentState)
        assertTrue(parentStateHolder.contains(entry.stateId))
    }

    @Test
    fun testDestroy() = runMainTest {
        val parentStateHolder = TestViewModelStoreProvider()
        val entry = BackStackEntry(
            uuid4().toString(),
            TestRoute("foo/bar", "foo/bar"),
            "foo/bar",
            emptyMap(),
            parentStateHolder,
        )
        entry.active()
        assertEquals(Lifecycle.State.RESUMED, entry.lifecycle.currentState)
        entry.inActive()
        assertEquals(Lifecycle.State.CREATED, entry.lifecycle.currentState)
        entry.destroy()
        assertEquals(Lifecycle.State.DESTROYED, entry.lifecycle.currentState)
        assertFalse(parentStateHolder.contains(entry.stateId))
    }

    @Test
    fun testDestroyAfterTransition() = runMainTest {
        val parentStateHolder = TestViewModelStoreProvider()
        val entry = BackStackEntry(
            uuid4().toString(),
            TestRoute("foo/bar", "foo/bar"),
            "foo/bar",
            emptyMap(),
            parentStateHolder,
        )
        entry.viewModelStore
        entry.active()
        entry.destroy()
        assertEquals(Lifecycle.State.RESUMED, entry.lifecycle.currentState)
        assertTrue(parentStateHolder.contains(entry.stateId))
        entry.inActive()
        assertEquals(Lifecycle.State.DESTROYED, entry.lifecycle.currentState)
        assertFalse(parentStateHolder.contains(entry.stateId))
    }
}
