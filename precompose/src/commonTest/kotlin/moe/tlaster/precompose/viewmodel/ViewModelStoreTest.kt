package moe.tlaster.precompose.viewmodel

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ViewModelStoreTest {
    @Test
    fun testClear() {
        val store = ViewModelStore()
        val viewModel1 = TestViewModel()
        val viewModel2 = TestViewModel()
        val mockViewModel = TestViewModel()
        store.put("a", viewModel1)
        store.put("b", viewModel2)
        store.put("mock", mockViewModel)
        assertFalse(viewModel1.cleared)
        assertFalse(viewModel2.cleared)
        store.clear()
        assertTrue(viewModel1.cleared)
        assertTrue(viewModel2.cleared)
        assertNull(store["a"])
        assertNull(store["b"])
    }

    internal class TestViewModel : ViewModel() {
        var cleared = false
        override fun onCleared() {
            cleared = true
        }
    }
}
