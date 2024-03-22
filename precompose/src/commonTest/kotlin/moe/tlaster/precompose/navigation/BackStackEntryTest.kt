package moe.tlaster.precompose.navigation

import com.benasher44.uuid.uuid4
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.stateholder.StateHolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackStackEntryTest {
    @Test
    fun testActive() {
        val parentStateHolder = StateHolder()
        val entry = BackStackEntry(
            uuid4().toString(),
            TestRoute("foo/bar", "foo/bar"),
            "foo/bar",
            emptyMap(),
            parentStateHolder,
            TestSavedStateHolder(),
        )
        assertTrue(parentStateHolder.contains(entry.stateId))
        assertEquals(Lifecycle.State.Initialized, entry.lifecycle.currentState)
        entry.active()
        assertEquals(Lifecycle.State.Active, entry.lifecycle.currentState)
    }

    @Test
    fun testInActive() {
        val parentStateHolder = StateHolder()
        val entry = BackStackEntry(
            uuid4().toString(),
            TestRoute("foo/bar", "foo/bar"),
            "foo/bar",
            emptyMap(),
            parentStateHolder,
            TestSavedStateHolder(),
        )
        entry.active()
        assertEquals(Lifecycle.State.Active, entry.lifecycle.currentState)
        entry.inActive()
        assertEquals(Lifecycle.State.InActive, entry.lifecycle.currentState)
        assertTrue(parentStateHolder.contains(entry.stateId))
    }

    @Test
    fun testDestroy() {
        val parentStateHolder = StateHolder()
        val entry = BackStackEntry(
            uuid4().toString(),
            TestRoute("foo/bar", "foo/bar"),
            "foo/bar",
            emptyMap(),
            parentStateHolder,
            TestSavedStateHolder(),
        )
        entry.active()
        assertEquals(Lifecycle.State.Active, entry.lifecycle.currentState)
        entry.inActive()
        assertEquals(Lifecycle.State.InActive, entry.lifecycle.currentState)
        entry.destroy()
        assertEquals(Lifecycle.State.Destroyed, entry.lifecycle.currentState)
        assertFalse(parentStateHolder.contains(entry.stateId))
    }

    @Test
    fun testDestroyAfterTransition() {
        val parentStateHolder = StateHolder()
        val entry = BackStackEntry(
            uuid4().toString(),
            TestRoute("foo/bar", "foo/bar"),
            "foo/bar",
            emptyMap(),
            parentStateHolder,
            TestSavedStateHolder(),
        )
        entry.active()
        entry.destroy()
        assertEquals(Lifecycle.State.Active, entry.lifecycle.currentState)
        assertTrue(parentStateHolder.contains(entry.stateId))
        entry.inActive()
        assertEquals(Lifecycle.State.Destroyed, entry.lifecycle.currentState)
        assertFalse(parentStateHolder.contains(entry.stateId))
    }
}
