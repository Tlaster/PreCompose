package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.navigation.route.GroupRoute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RouteBuilderTest {
    @Test
    fun testEmptyRoute() {
        val graph = RouteBuilder("").build()
        assertTrue(graph.routes.isEmpty())
    }

    @Test
    fun testSingleRoute() {
        RouteBuilder("/home").apply {
            testRoute("/home", "home")
        }.build().apply {
            assertTrue(routes.size == 1)
            routes.first().let {
                assertTrue(it is TestRoute)
                assertEquals("/home", it.route)
                assertEquals("home", it.id)
            }
        }
    }

    @Test
    fun testMultipleRouteWithSameRoute() {
        assertFailsWith(IllegalArgumentException::class, "Duplicate route can not be applied") {
            RouteBuilder("/home").apply {
                testRoute("/home", "home")
                testRoute("/home", "home")
            }.build()
        }
    }

    @Test
    fun testGroupRoute() {
        val graph = RouteBuilder("/home").apply {
            testRoute("/home", "home")
            group("/group", "/detail") {
                testRoute("/detail", "detail")
            }
        }.build()
        assertEquals(3, graph.routes.size)
        assertContains(graph.routes, TestRoute("/home", "home"))
        assertContains(graph.routes, GroupRoute("/group", TestRoute("/detail", "detail")))
        assertContains(graph.routes, TestRoute("/detail", "detail"))
    }

    @Test
    fun testNestedGroupRoute() {
        val graph = RouteBuilder("/home").apply {
            testRoute("/home", "home")
            group("/group", "/detail") {
                testRoute("/detail", "detail")
                group("/group2", "/detail2") {
                    testRoute("/detail2", "detail2")
                }
            }
        }.build()
        assertEquals(5, graph.routes.size)
        assertContains(graph.routes, TestRoute("/home", "home"))
        assertContains(graph.routes, GroupRoute("/group", TestRoute("/detail", "detail")))
        assertContains(graph.routes, TestRoute("/detail", "detail"))
        assertContains(graph.routes, GroupRoute("/group2", TestRoute("/detail2", "detail2")))
        assertContains(graph.routes, TestRoute("/detail2", "detail2"))
    }
}
