package moe.tlaster.precompose.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RouteBuilderTest {
    @Test
    fun testInitialRouteNotInRoutes() {
        assertFailsWith(IllegalArgumentException::class, "No initial route target fot this route graph") {
            RouteBuilder("/home").build()
        }
        assertFailsWith(IllegalArgumentException::class, "No initial route target fot this route graph") {
            RouteBuilder("/home").apply {
                testRoute("/detail", "1")
            }.build()
        }
    }

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
}
