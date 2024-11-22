package moe.tlaster.precompose.navigation

import androidx.lifecycle.Lifecycle
import moe.tlaster.precompose.navigation.route.GroupRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class BackStackManagerTest {
    @Test
    fun testInitialRoute() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
        )
        val backStacks = manager.backStacks.value
        assertEquals(1, backStacks.size)
        assertEquals("foo/bar", backStacks[0].route.route)
        assertIs<TestRoute>(backStacks[0].route)
        assertEquals("foo/bar", (backStacks[0].route as TestRoute).id)
    }

    @Test
    fun testPush() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
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
    fun testPop() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
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
    fun testLaunchSingleTop() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
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
    fun testLaunchSingleTopWithIncludePath() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
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
    fun testPopUpTo() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
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
    fun testPopUpToWithInclusiveLast() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
        )
        manager.push("foo/bar/1")
        manager.push("foo/bar/1/baz")
        manager.push("foo/bar/2")
        manager.push("foo/bar/3", NavOptions(popUpTo = PopUpTo("foo/bar/{id}", inclusive = true)))
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
    fun testPopUpToWithInclusive() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
        )
        manager.push("foo/bar/1")
        manager.push("foo/bar/1/baz")
        manager.push("foo/bar/2")
        manager.push(
            "foo/bar/3",
            NavOptions(popUpTo = PopUpTo("foo/bar/{id}/baz", inclusive = true)),
        )
        val backStacks = manager.backStacks.value
        assertEquals(3, backStacks.size)
        assertEquals("foo/bar", backStacks[0].route.route)
        assertEquals("foo/bar/{id}", backStacks[1].route.route)
        assertEquals("1", backStacks[1].pathMap["id"])
        assertEquals("foo/bar/{id}", backStacks[1].route.route)
        assertEquals("3", backStacks[2].pathMap["id"])
    }

    @Test
    fun testLaunchSingleTopWithPopUpToWithInclusive() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
        )
        manager.push("foo/bar/1")
        manager.push("foo/bar/1/baz")
        manager.push("foo/bar/2")
        manager.push(
            "foo/bar/1",
            NavOptions(launchSingleTop = true, popUpTo = PopUpTo("foo/bar/{id}/baz", inclusive = true)),
        )
        val backStacks = manager.backStacks.value
        assertEquals(2, backStacks.size)
        assertEquals("foo/bar", backStacks[0].route.route)
        assertEquals("foo/bar/{id}", backStacks[1].route.route)
        assertEquals("1", backStacks[1].pathMap["id"])
    }

    @Test
    fun testMultipleLaunchSingleTopWithPopUpTo() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/baz", "foo/baz"),
                ),
            ),
        )

        fun navigate(path: String, navOptions: NavOptions) = runMainTest {
            val previousEntry = manager.backStacks.value.lastOrNull()
            manager.push(path, navOptions)
            // Mark the previous entry as inactive to simulate the lifecycle change by the NavHost
            previousEntry?.inActive()
        }

        navigate("foo/bar", NavOptions(launchSingleTop = true, popUpTo = PopUpTo.First(inclusive = true)))
        navigate("foo/baz", NavOptions(launchSingleTop = true, popUpTo = PopUpTo.First(inclusive = false)))
        navigate("foo/bar", NavOptions(launchSingleTop = true, popUpTo = PopUpTo.First(inclusive = true)))
        navigate("foo/baz", NavOptions(launchSingleTop = true, popUpTo = PopUpTo.First(inclusive = false)))
        val backStacks = manager.backStacks.value
        assertEquals(2, backStacks.size)
        assertEquals("foo/bar", backStacks[0].route.route)
        assertEquals("foo/baz", backStacks[1].route.route)
    }

    @Test
    fun testLifecycle() = runMainTest {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        manager.init(
            lifecycleOwner = lifecycleOwner,
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
        )
        val entry = manager.backStacks.value[0]
        lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(Lifecycle.State.RESUMED, entry.lifecycle.currentState)
        lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        assertEquals(Lifecycle.State.CREATED, entry.lifecycle.currentState)
        lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        assertEquals(0, manager.backStacks.value.size)
        assertEquals(Lifecycle.State.DESTROYED, entry.lifecycle.currentState)
    }

    @Test
    fun testGroupNavigation() = runMainTest {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteBuilder("/home").apply {
                testRoute("/home", "home")
                group("/group", "/detail") {
                    testRoute("/detail", "detail")
                }
            }.build(),
        )
        manager.push("/group")
        assertEquals(2, manager.backStacks.value.size)
        assertEquals("/home", manager.backStacks.value[0].route.route)
        assertEquals("/group", manager.backStacks.value[1].route.route)
        val groupRoute = manager.backStacks.value[1].route
        assertIs<GroupRoute>(groupRoute)
        assertEquals("/detail", groupRoute.initialRoute.route)
    }

    @Test
    fun testNestedGroupNavigation() = runMainTest {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteBuilder("/home").apply {
                testRoute("/home", "home")
                group("/group", "/detail") {
                    testRoute("/detail", "detail")
                    group("/nested", "/nestedDetail") {
                        testRoute("/nestedDetail", "nestedDetail")
                    }
                }
            }.build(),
        )
        manager.push("/group")
        assertEquals(2, manager.backStacks.value.size)
        assertEquals("/home", manager.backStacks.value[0].route.route)
        assertEquals("/group", manager.backStacks.value[1].route.route)
        val groupRoute = manager.backStacks.value[1].route
        assertIs<GroupRoute>(groupRoute)
        assertEquals("/detail", groupRoute.initialRoute.route)
        manager.push("/nested")
        assertEquals(3, manager.backStacks.value.size)
        assertEquals("/home", manager.backStacks.value[0].route.route)
        assertEquals("/group", manager.backStacks.value[1].route.route)
        assertEquals("/nested", manager.backStacks.value[2].route.route)
        val nestedGroupRoute = manager.backStacks.value[2].route
        assertIs<GroupRoute>(nestedGroupRoute)
        assertEquals("/nestedDetail", nestedGroupRoute.initialRoute.route)
    }

    @Test
    fun testGroupNavigationWithPopUpTo() = runMainTest {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteBuilder("/home").apply {
                testRoute("/home", "home")
                group("/group", "/detail") {
                    testRoute("/detail", "detail")
                }
            }.build(),
        )
        manager.push("/group")
        assertEquals(2, manager.backStacks.value.size)
        assertEquals("/home", manager.backStacks.value[0].route.route)
        assertEquals("/group", manager.backStacks.value[1].route.route)
        val groupRoute = manager.backStacks.value[1].route
        assertIs<GroupRoute>(groupRoute)
        assertEquals("/detail", groupRoute.initialRoute.route)
        manager.push("/detail", NavOptions(popUpTo = PopUpTo("/group", inclusive = true)))
        assertEquals(2, manager.backStacks.value.size)
        assertEquals("/home", manager.backStacks.value[0].route.route)
        assertEquals("/detail", manager.backStacks.value[1].route.route)
    }

    /**
     * #146
     */
    @Test
    fun testNavigateWithPopupToWithDuplicateScene() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "screen1",
                listOf(
                    TestRoute("screen1", "screen1"),
                    TestRoute("screen2", "screen2"),
                ),
            ),
        )

        fun navigate(path: String, navOptions: NavOptions) = runMainTest {
            val previousEntry = manager.backStacks.value.lastOrNull()
            manager.push(path, navOptions)
            // Mark the previous entry as inactive to simulate the lifecycle change by the NavHost
            previousEntry?.inActive()
        }

        navigate("screen2", NavOptions())
        navigate("screen1", NavOptions(popUpTo = PopUpTo("screen1")))
        navigate("screen1", NavOptions())

        assertEquals(
            listOf("screen1", "screen1", "screen1"),
            manager.backStacks.value.map { it.path },
        )

        // assertEquals(
        //     listOf("1-screen1", "3-screen1", "4-screen1"),
        //     manager.backStacks.value.map { it.stateId },
        // )
    }

    @Test
    fun testGoBackTwiceImmediately() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "screen1",
                listOf(
                    TestRoute("screen1", "screen1"),
                    TestRoute("screen2", "screen2"),
                ),
            ),
        )
        assertEquals(
            listOf("screen1"),
            manager.backStacks.value.map { it.path },
        )

        manager.push("screen1")
        manager.push("screen2")
        manager.push("screen1")

        manager.pop()
        manager.pop()

        assertEquals(
            listOf("screen1", "screen1"),
            manager.backStacks.value.map { it.path },
        )
    }

    @Test
    fun testGoBackWithPopupTo() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "screen1",
                listOf(
                    TestRoute("screen1", "screen1"),
                    TestRoute("screen2", "screen2"),
                    TestRoute("screen3", "screen3"),
                ),
            ),
        )
        assertEquals(
            listOf("screen1"),
            manager.backStacks.value.map { it.path },
        )

        manager.push("screen2")
        manager.push("screen3")

        manager.popWithOptions(PopUpTo("screen1", inclusive = false))

        assertEquals(
            listOf("screen1"),
            manager.backStacks.value.map { it.path },
        )
    }

    @Test
    fun testGoBackWithPopupToInclusive() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "screen1",
                listOf(
                    TestRoute("screen1", "screen1"),
                    TestRoute("screen2", "screen2"),
                    TestRoute("screen3", "screen3"),
                ),
            ),
        )
        assertEquals(
            listOf("screen1"),
            manager.backStacks.value.map { it.path },
        )

        manager.push("screen2")
        manager.push("screen3")

        manager.popWithOptions(PopUpTo("screen2", inclusive = true))

        assertEquals(
            listOf("screen1"),
            manager.backStacks.value.map { it.path },
        )
    }

    @Test
    fun testRouteGraphUpdateWithSameRoute() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "screen1",
                listOf(
                    TestRoute("screen1", "screen1"),
                    TestRoute("screen2", "screen2"),
                    TestRoute("screen3", "screen3"),
                ),
            ),
        )
        assertEquals(
            listOf("screen1"),
            manager.backStacks.value.map { it.path },
        )

        manager.push("screen2")
        manager.push("screen3")

        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "screen1",
                listOf(
                    TestRoute("screen1", "screen1"),
                    TestRoute("screen2", "screen2"),
                    TestRoute("screen3", "screen3"),
                ),
            ),
        )

        assertEquals(
            listOf("screen1", "screen2", "screen3"),
            manager.backStacks.value.map { it.path },
        )
    }

    @Test
    fun testRouteGraphUpdateWithDifferentRoute() = runMainTest {
        val manager = BackStackManager()

        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "screen1",
                listOf(
                    TestRoute("screen1", "screen1"),
                    TestRoute("screen2", "screen2"),
                    TestRoute("screen3", "screen3"),
                ),
            ),
        )
        assertEquals(
            listOf("screen1"),
            manager.backStacks.value.map { it.path },
        )

        manager.push("screen2")
        manager.push("screen3")

        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "screen1",
                listOf(
                    TestRoute("screen1", "screen1"),
                    TestRoute("screen2", "screen2"),
                    TestRoute("screen4", "screen4"),
                ),
            ),
        )

        assertEquals(
            listOf("screen1"),
            manager.backStacks.value.map { it.path },
        )
    }

    @Test
    fun testStateId() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "screen1",
                listOf(
                    TestRoute("screen1", "screen1"),
                    TestRoute("screen2", "screen2"),
                    TestRoute("screen3", "screen3"),
                ),
            ),
        )
        manager.push("screen2")
        manager.push("screen3")
        val lastEntry = manager.backStacks.value.last()
        val stateId = lastEntry.stateId
        manager.pop()
        lastEntry.destroyDirectly()
        manager.push("screen3")
        assertNotEquals(stateId, manager.backStacks.value.last().stateId)
    }

    @Test
    fun testStateIdWithoutDestroy() = runMainTest {
        val manager = BackStackManager()
        manager.init(
            lifecycleOwner = TestLifecycleOwner(),
            viewModelStoreOwner = TestViewModelStoreOwner(),
        )
        manager.setRouteGraph(
            routeGraph = RouteGraph(
                "screen1",
                listOf(
                    TestRoute("screen1", "screen1"),
                    TestRoute("screen2", "screen2"),
                    TestRoute("screen3", "screen3"),
                ),
            ),
        )
        manager.push("screen2")
        manager.push("screen3")
        val lastEntry = manager.backStacks.value.last()
        val stateId = lastEntry.stateId
        manager.pop()
        manager.push("screen3")
        assertNotEquals(stateId, manager.backStacks.value.last().stateId)
    }
}
