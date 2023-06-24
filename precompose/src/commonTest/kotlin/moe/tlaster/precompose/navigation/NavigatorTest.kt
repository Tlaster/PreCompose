package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.lifecycle.TestLifecycleOwner
import moe.tlaster.precompose.stateholder.StateHolder
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigatorTest {
    @Test
    fun testNavigate() {
        val navigator = Navigator()
        navigator.init(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                )
            ),
            StateHolder(),
            TestSavedStateHolder(),
            TestLifecycleOwner(),
        )
        navigator.navigate("foo/bar/1")
        navigator.navigate("foo/bar/1/baz")
        navigator.goBack()
        assertEquals(2, navigator.stackManager.backStacks.value.size)
        assertEquals("foo/bar/{id}", navigator.stackManager.backStacks.value.last().route.route)
    }

    @Test
    fun testPendingNavigate() {
        val navigator = Navigator()
        navigator.navigate("foo/bar/1")
        assertEquals(0, navigator.stackManager.backStacks.value.size)
        navigator.init(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                )
            ),
            StateHolder(),
            TestSavedStateHolder(),
            TestLifecycleOwner()
        )
        assertEquals(2, navigator.stackManager.backStacks.value.size)
        assertEquals("foo/bar/{id}", navigator.stackManager.backStacks.value.last().route.route)
    }
}
