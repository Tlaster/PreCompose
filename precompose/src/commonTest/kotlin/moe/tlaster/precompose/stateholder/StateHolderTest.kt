package moe.tlaster.precompose.stateholder

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StateHolderTest {
    @Test
    fun testClear() {
        val store = StateHolder()
        val viewModel1 = TestObject()
        val viewModel2 = TestObject()
        val mockViewModel = TestObject()
        store.set("a", viewModel1)
        store.set("b", viewModel2)
        store.set("mock", mockViewModel)
        assertFalse(viewModel1.cleared)
        assertFalse(viewModel2.cleared)
        store.close()
        assertTrue(viewModel1.cleared)
        assertTrue(viewModel2.cleared)
        assertNull(store["a"])
        assertNull(store["b"])
    }

    @OptIn(ExperimentalStdlibApi::class)
    internal class TestObject() : AutoCloseable {
        var cleared = false
        override fun close() {
            cleared = true
        }
    }
}
