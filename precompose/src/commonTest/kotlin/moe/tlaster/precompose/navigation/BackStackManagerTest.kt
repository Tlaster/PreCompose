package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.TestLifecycleOwner
import moe.tlaster.precompose.stateholder.StateHolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BackStackManagerTest {
    @Test
    fun testInitialRoute() {
        val manager = BackStackManager()
        manager.init(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                )
            ),
            StateHolder(),
            TestLifecycleOwner()
        )
        val backStacks = manager.backStacks.value
        assertEquals(1, backStacks.size)
        assertEquals("foo/bar", backStacks[0].route.route)
        assertIs<TestRoute>(backStacks[0].route)
        assertEquals("foo/bar", (backStacks[0].route as TestRoute).id)
    }

    @Test
    fun testPush() {
        val manager = BackStackManager()
        manager.init(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                )
            ),
            StateHolder(),
            TestLifecycleOwner()
        )
        manager.push("foo/bar/1")
        val backStacks = manager.backStacks.value
        assertEquals(2, backStacks.size)
        assertEquals("foo/bar/{id}", backStacks[1].route.route)
        assertEquals("1", backStacks[1].pathMap["id"])
        assertIs<TestRoute>(backStacks[1].route)
        assertEquals("foo/bar/{id}", (backStacks[1].route as TestRoute).id)
    }

    @Test
    fun testPop() {
        val manager = BackStackManager()
        manager.init(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                )
            ),
            StateHolder(),
            TestLifecycleOwner()
        )
        manager.push("foo/bar/1")
        manager.push("foo/bar/1/baz")
        manager.pop()
        val backStacks = manager.backStacks.value
        assertEquals(2, backStacks.size)
        assertEquals("foo/bar/{id}", backStacks[1].route.route)
        assertEquals("1", backStacks[1].pathMap["id"])
        assertIs<TestRoute>(backStacks[1].route)
        assertEquals("foo/bar/{id}", (backStacks[1].route as TestRoute).id)
    }

    @Test
    fun testLaunchSingleTop() {
        val manager = BackStackManager()
        manager.init(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                )
            ),
            StateHolder(),
            TestLifecycleOwner()
        )
        manager.push("foo/bar/1")
        manager.push("foo/bar/1/baz")
        manager.push("foo/bar", NavOptions(launchSingleTop = true))
        val backStacks = manager.backStacks.value
        assertEquals(3, backStacks.size)
        assertEquals("foo/bar/{id}", backStacks[0].route.route)
        assertEquals("1", backStacks[0].pathMap["id"])
        assertIs<TestRoute>(backStacks[0].route)
        assertEquals("foo/bar/{id}", (backStacks[0].route as TestRoute).id)
        assertEquals("foo/bar/{id}/baz", backStacks[1].route.route)
        assertEquals("1", backStacks[1].pathMap["id"])
        assertIs<TestRoute>(backStacks[1].route)
        assertEquals("foo/bar/{id}/baz", (backStacks[1].route as TestRoute).id)
        assertEquals("foo/bar", backStacks[2].route.route)
        assertIs<TestRoute>(backStacks[2].route)
        assertEquals("foo/bar", (backStacks[2].route as TestRoute).id)
    }

    @Test
    fun testLaunchSingleTopWithIncludePath() {
        val manager = BackStackManager()
        manager.init(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                )
            ),
            StateHolder(),
            TestLifecycleOwner()
        )
        manager.push("foo/bar/1")
        manager.push("foo/bar/1/baz")
        manager.push("foo/bar/2")
        manager.push("foo/bar/1", NavOptions(launchSingleTop = true, includePath = true))
        val backStacks = manager.backStacks.value
        assertEquals(4, backStacks.size)
        assertEquals("foo/bar", backStacks[0].route.route)
        assertEquals("foo/bar/{id}/baz", backStacks[1].route.route)
        assertEquals("1", backStacks[1].pathMap["id"])
        assertEquals("foo/bar/{id}", backStacks[2].route.route)
        assertEquals("2", backStacks[2].pathMap["id"])
        assertEquals("foo/bar/{id}", backStacks[3].route.route)
        assertEquals("1", backStacks[3].pathMap["id"])
    }

    @Test
    fun testPopUpTo() {
        val manager = BackStackManager()
        manager.init(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                )
            ),
            StateHolder(),
            TestLifecycleOwner()
        )
        manager.push("foo/bar/1")
        manager.push("foo/bar/1/baz")
        manager.push("foo/bar/2")
        manager.push("foo/bar/3", NavOptions(popUpTo = PopUpTo("foo/bar/{id}/baz")))
        val backStacks = manager.backStacks.value
        assertEquals(4, backStacks.size)
        assertEquals("foo/bar", backStacks[0].route.route)
        assertEquals("foo/bar/{id}", backStacks[1].route.route)
        assertEquals("1", backStacks[1].pathMap["id"])
        assertEquals("foo/bar/{id}/baz", backStacks[2].route.route)
        assertEquals("1", backStacks[2].pathMap["id"])
        assertEquals("foo/bar/{id}", backStacks[3].route.route)
        assertEquals("3", backStacks[3].pathMap["id"])
    }

    @Test
    fun testPopUpToWithInclusive() {
        val manager = BackStackManager()
        manager.init(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                )
            ),
            StateHolder(),
            TestLifecycleOwner()
        )
        manager.push("foo/bar/1")
        manager.push("foo/bar/1/baz")
        manager.push("foo/bar/2")
        manager.push("foo/bar/3", NavOptions(popUpTo = PopUpTo("foo/bar/{id}/baz", inclusive = true)))
        val backStacks = manager.backStacks.value
        assertEquals(3, backStacks.size)
        assertEquals("foo/bar", backStacks[0].route.route)
        assertEquals("foo/bar/{id}", backStacks[1].route.route)
        assertEquals("1", backStacks[1].pathMap["id"])
        assertEquals("foo/bar/{id}", backStacks[1].route.route)
        assertEquals("3", backStacks[2].pathMap["id"])
    }

    @Test
    fun testLaunchSingleTopWithPopUpToWithInclusive() {
        val manager = BackStackManager()
        manager.init(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                )
            ),
            StateHolder(),
            TestLifecycleOwner()
        )
        manager.push("foo/bar/1")
        manager.push("foo/bar/1/baz")
        manager.push("foo/bar/2")
        manager.push("foo/bar/1", NavOptions(launchSingleTop = true, popUpTo = PopUpTo("foo/bar/{id}/baz", inclusive = true)))
        val backStacks = manager.backStacks.value
        assertEquals(2, backStacks.size)
        assertEquals("foo/bar", backStacks[0].route.route)
        assertEquals("foo/bar/{id}", backStacks[1].route.route)
        assertEquals("1", backStacks[1].pathMap["id"])
    }

    @Test
    fun testLifecycle() {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        manager.init(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                )
            ),
            StateHolder(),
            lifecycleOwner
        )
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.Active
        assertEquals(Lifecycle.State.Active, manager.backStacks.value[0].lifecycle.currentState)
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.InActive
        assertEquals(Lifecycle.State.InActive, manager.backStacks.value[0].lifecycle.currentState)
        // TODO: [Android] OnConfigurationChanged also trigger this, which cause backstacks being cleared
        // lifecycleOwner.lifecycle.currentState = Lifecycle.State.Destroyed
        // assertEquals(Lifecycle.State.Destroyed, manager.backStacks.value[0].lifecycle.currentState)
    }
}
