package moe.tlaster.precompose.navigation

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class BackDispatcherTest {

    @Test
    fun onBackPress_should_call_handleBackPress_on_the_last_enabled_handler() {
        val dispatcher = BackDispatcher()
        val handler1 = object : BackHandler {
            override val isEnabled = true
            override fun handleBackPress() {}
            override fun handleBackProgressed(progress: Float) {}
            override fun handleBackCancelled() {}
            override fun handleBackStarted() {}
        }
        val handler2 = object : BackHandler {
            override val isEnabled = true
            override fun handleBackPress() {}
            override fun handleBackProgressed(progress: Float) {}
            override fun handleBackCancelled() {}
            override fun handleBackStarted() {}
        }
        dispatcher.register(handler1)
        dispatcher.register(handler2)

        dispatcher.onBackPress()

        assertEquals(handler2, dispatcher.handlers.last())
    }

    @Test
    fun canHandleBackPress_should_return_true_if_any_handler_is_enabled() = runTest {
        val dispatcher = BackDispatcher()
        val handler1 = object : BackHandler {
            override val isEnabled = true
            override fun handleBackPress() {}
            override fun handleBackProgressed(progress: Float) {}
            override fun handleBackCancelled() {}
            override fun handleBackStarted() {}
        }
        val handler2 = object : BackHandler {
            override val isEnabled = true
            override fun handleBackPress() {}
            override fun handleBackProgressed(progress: Float) {}
            override fun handleBackCancelled() {}
            override fun handleBackStarted() {}
        }
        dispatcher.register(handler1)
        dispatcher.register(handler2)

        assertEquals(true, dispatcher.canHandleBackPress.first())
    }
}
