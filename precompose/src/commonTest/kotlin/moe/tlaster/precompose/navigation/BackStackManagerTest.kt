package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.TestLifecycleOwner
import moe.tlaster.precompose.navigation.route.GroupRoute
import moe.tlaster.precompose.stateholder.StateHolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BackStackManagerTest {
    @Test
    fun testInitialRoute() {
        val manager = BackStackManager()
        manager.init(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = TestLifecycleOwner(),
            persistNavState = false,
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
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = TestLifecycleOwner(),
            persistNavState = false,
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
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = TestLifecycleOwner(),
            persistNavState = false,
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
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = TestLifecycleOwner(),
            persistNavState = false,
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
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = TestLifecycleOwner(),
            persistNavState = false,
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
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = TestLifecycleOwner(),
            persistNavState = false,
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
    fun testPopUpToWithInclusiveLast() {
        val manager = BackStackManager()
        manager.init(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = TestLifecycleOwner(),
            persistNavState = false,
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
    fun testPopUpToWithInclusive() {
        val manager = BackStackManager()
        manager.init(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = TestLifecycleOwner(),
            persistNavState = false,
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
    fun testLaunchSingleTopWithPopUpToWithInclusive() {
        val manager = BackStackManager()
        manager.init(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = TestLifecycleOwner(),
            persistNavState = false,
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
    fun testMultipleLaunchSingleTopWithPopUpTo() {
        val manager = BackStackManager()
        manager.init(
            RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/baz", "foo/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = TestLifecycleOwner(),
            persistNavState = false,
        )

        fun navigate(path: String, navOptions: NavOptions) {
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
    fun testLifecycle() {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        manager.init(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = lifecycleOwner,
            persistNavState = false,
        )
        val entry = manager.backStacks.value[0]
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.Active
        assertEquals(Lifecycle.State.Active, entry.lifecycle.currentState)
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.InActive
        assertEquals(Lifecycle.State.InActive, entry.lifecycle.currentState)
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.Destroyed
        assertEquals(0, manager.backStacks.value.size)
        assertEquals(Lifecycle.State.Destroyed, entry.lifecycle.currentState)
    }

    // @Test
    // fun testRequestNavigationLock() {
    //     val manager = BackStackManager()
    //     val lifecycleOwner = TestLifecycleOwner()
    //     manager.init(
    //         routeGraph = RouteGraph(
    //             "foo/bar",
    //             listOf(
    //                 TestRoute("foo/bar", "foo/bar"),
    //                 TestRoute("foo/bar/{id}", "foo/bar/{id}"),
    //                 TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
    //             ),
    //         ),
    //         stateHolder = StateHolder(),
    //         savedStateHolder = TestSavedStateHolder(),
    //         lifecycleOwner = lifecycleOwner,
    //         persistNavState = false,
    //     )
    //     assertTrue(manager.canNavigate)
    //     var currentEntry = manager.backStacks.value.last()
    //     currentEntry.active()
    //     manager.push("foo/bar/1")
    //     assertTrue(manager.canNavigate)
    //     currentEntry = manager.backStacks.value.last()
    //     currentEntry.active()
    //     manager.pop()
    //     assertFalse(manager.canNavigate)
    //     currentEntry.inActive()
    //     assertTrue(manager.canNavigate)
    // }

    @Test
    fun testGroupNavigation() {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        manager.init(
            routeGraph = RouteBuilder("/home").apply {
                testRoute("/home", "home")
                group("/group", "/detail") {
                    testRoute("/detail", "detail")
                }
            }.build(),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = lifecycleOwner,
            persistNavState = false,
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
    fun testNestedGroupNavigation() {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        manager.init(
            routeGraph = RouteBuilder("/home").apply {
                testRoute("/home", "home")
                group("/group", "/detail") {
                    testRoute("/detail", "detail")
                    group("/nested", "/nestedDetail") {
                        testRoute("/nestedDetail", "nestedDetail")
                    }
                }
            }.build(),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = lifecycleOwner,
            persistNavState = false,
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
    fun testGroupNavigationWithPopUpTo() {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        manager.init(
            routeGraph = RouteBuilder("/home").apply {
                testRoute("/home", "home")
                group("/group", "/detail") {
                    testRoute("/detail", "detail")
                }
            }.build(),
            stateHolder = StateHolder(),
            savedStateHolder = TestSavedStateHolder(),
            lifecycleOwner = lifecycleOwner,
            persistNavState = false,
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

    @Test
    fun testSavingBackStack() {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        val saveableStateHolder = TestSavedStateHolder()

        manager.init(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = saveableStateHolder,
            lifecycleOwner = lifecycleOwner,
            persistNavState = true,
        )

        manager.push("foo/bar/1")
        manager.push("foo/bar/1/baz")

        saveableStateHolder.performSave().apply {
            assertEquals(
                listOf("foo/bar", "foo/bar/1", "foo/bar/1/baz"),
                get(STACK_SAVED_STATE_KEY)!!.single(),
            )
        }
    }

    @Test
    fun testRestoringBackStack() {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        val savedStateHolder = TestSavedStateHolder(
            restored = mapOf(
                STACK_SAVED_STATE_KEY to listOf(listOf("foo/bar", "foo/bar/1", "foo/bar/1/baz")),
            ),
        )

        manager.init(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = savedStateHolder,
            lifecycleOwner = lifecycleOwner,
            persistNavState = true,
        )

        assertEquals(
            listOf("foo/bar", "foo/bar/1", "foo/bar/1/baz"),
            manager.backStacks.value.map { it.path },
        )
    }

    @Test
    fun testBackStackNotPersistedWhenDisabled() {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        val saveableStateHolder = TestSavedStateHolder()

        manager.init(
            routeGraph = RouteGraph(
                "foo/bar",
                listOf(
                    TestRoute("foo/bar", "foo/bar"),
                    TestRoute("foo/bar/{id}", "foo/bar/{id}"),
                    TestRoute("foo/bar/{id}/baz", "foo/bar/{id}/baz"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = saveableStateHolder,
            lifecycleOwner = lifecycleOwner,
            persistNavState = false,
        )

        manager.push("foo/bar/1")
        manager.push("foo/bar/1/baz")

        saveableStateHolder.performSave().apply {
            assertEquals(
                null,
                get(STACK_SAVED_STATE_KEY),
            )
        }
    }

    /**
     * #146
     */
    @Test
    fun testNavigateWithPopupToWithDuplicateScene() {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        val saveableStateHolder = TestSavedStateHolder()

        manager.init(
            routeGraph = RouteGraph(
                "screen1",
                listOf(
                    TestRoute("screen1", "screen1"),
                    TestRoute("screen2", "screen2"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = saveableStateHolder,
            lifecycleOwner = lifecycleOwner,
            persistNavState = false,
        )

        fun navigate(path: String, navOptions: NavOptions) {
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

        assertEquals(
            listOf("1-screen1", "3-screen1", "4-screen1"),
            manager.backStacks.value.map { it.stateId },
        )
    }

    @Test
    fun testGoBackTwiceImmediately() {
        val manager = BackStackManager()
        val lifecycleOwner = TestLifecycleOwner()
        val saveableStateHolder = TestSavedStateHolder()

        manager.init(
            routeGraph = RouteGraph(
                "screen1",
                listOf(
                    TestRoute("screen1", "screen1"),
                    TestRoute("screen2", "screen2"),
                ),
            ),
            stateHolder = StateHolder(),
            savedStateHolder = saveableStateHolder,
            lifecycleOwner = lifecycleOwner,
            persistNavState = false,
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
}
