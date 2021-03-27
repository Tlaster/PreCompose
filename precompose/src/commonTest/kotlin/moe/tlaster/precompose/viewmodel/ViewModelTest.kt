package moe.tlaster.precompose.viewmodel

import moe.tlaster.precompose.standard.Disposable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViewModelTest {
    internal class DisposableImpl : Disposable {
        var wasDisposable = false

        override fun dispose() {
            wasDisposable = true
        }
    }

    internal class ViewModel : moe.tlaster.precompose.viewmodel.ViewModel()

    @Test
    fun testCloseableTag() {
        val vm = ViewModel()
        val impl = DisposableImpl()
        vm.setTagIfAbsent<Any>("totally_not_coroutine_context", impl)
        vm.clear()
        assertTrue(impl.wasDisposable)
    }

    @Test
    fun testCloseableTagAlreadyClearedVM() {
        val vm = ViewModel()
        vm.clear()
        val impl = DisposableImpl()
        vm.setTagIfAbsent<Any>("key", impl)
        assertTrue(impl.wasDisposable)
    }

    @Test
    fun testAlreadyAssociatedKey() {
        val vm = ViewModel()
        assertEquals("first", vm.setTagIfAbsent("key", "first"))
        assertEquals("first", vm.setTagIfAbsent("key", "second"))
    }
}
