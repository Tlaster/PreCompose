package moe.tlaster.precompose.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigatorTest {
    @Test
    fun testNavigate() = runMainTest {
        val navigator = Navigator()
        navigator.init(
            TestLifecycleOwner(),
            TestViewModelStoreOwner(),
        )
        navigator.setRouteGraph(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
        )
        navigator.navigate("foo/bar/1")
        navigator.navigate("foo/bar/1/baz")
        navigator.goBack()
        assertEquals(2, navigator.stackManager.backStacks.value.size)
        assertEquals("foo/bar/{id}", navigator.stackManager.backStacks.value.last().route.route)
    }

    @Test
    fun testPendingNavigate() = runMainTest {
        val navigator = Navigator()
        navigator.navigate("foo/bar/1")
        assertEquals(0, navigator.stackManager.backStacks.value.size)
        navigator.init(
            TestLifecycleOwner(),
            TestViewModelStoreOwner(),
        )
        navigator.setRouteGraph(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
        )
        assertEquals(2, navigator.stackManager.backStacks.value.size)
        assertEquals("foo/bar/{id}", navigator.stackManager.backStacks.value.last().route.route)
    }
}
