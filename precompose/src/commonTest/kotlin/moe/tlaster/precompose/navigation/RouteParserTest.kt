package moe.tlaster.precompose.navigation

import kotlin.test.*

class RouteParserTest {
    @Test
    fun pathKeys() {
        pathKeys("/{lang:[a-z]{2}}") { keys -> assertEquals(listOf("lang"), keys) }

        pathKeys("/edit/{id}?") { keys -> assertEquals(listOf("id"), keys) }

        pathKeys("/path/{id}/{start}?/{end}?") { keys -> assertEquals(listOf("id", "start", "end"), keys) }

        pathKeys("/*") { keys -> assertEquals(1, keys.size) }

        pathKeys("/foo/?*") { keys -> assertEquals(1, keys.size) }

        pathKeys("/foo") { keys -> assertEquals(0, keys.size) }
        pathKeys("/") { keys -> assertEquals(0, keys.size) }
        pathKeys("/foo/bar") { keys -> assertEquals(0, keys.size) }
        pathKeys("/foo/*") { keys -> assertEquals(1, keys.size) }
        pathKeys("/foo/*name") { keys -> assertEquals(1, keys.size) }
        pathKeys("/foo/{x}") { keys -> assertEquals(1, keys.size) }
    }


    @Test
    fun pathKeyMap() {
        pathKeyMap("/{lang:[a-z]{2}}") { map -> assertEquals("[a-z]{2}", map["lang"]) }
        pathKeyMap("/{id:[0-9]+}") { map -> assertEquals("[0-9]+", map["id"]) }
        pathKeyMap("/edit/{id}?") { keys -> assertEquals(null, keys["id"]) }
        pathKeyMap("/path/{id}/{start}?/{end}?") { keys ->
            assertEquals(null, keys["id"])
            assertEquals(null, keys["start"])
            assertEquals(null, keys["end"])
        }
        pathKeyMap("/*") { keys -> assertEquals("\\.*", keys["*"]) }
        pathKeyMap("/foo/?*") { keys -> assertEquals("\\.*", keys["*"]) }
        pathKeyMap("/foo/*name") { keys -> assertEquals("\\.*", keys["name"]) }
    }

    private fun pathKeys(pattern: String, consumer: (List<String>) -> Unit) {
        consumer.invoke(RouteParser.pathKeys(pattern))
    }

    private fun pathKeyMap(pattern: String, consumer: (Map<String, String?>) -> Unit) {
        val map: MutableMap<String, String?> = HashMap()
        RouteParser.pathKeys(pattern) { key: String, value: String? -> map[key] = value }
        consumer.invoke(map)
    }

    @Test
    fun wildOnRoot() {
        val parser = RouteParser()
        parser.insert(route("/foo/?*", "foo"))
        parser.insert(route("/bar/*", "bar"))
        parser.insert(route("/*", "root"))
        parser.find("/").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "root")
        }
        parser.find("/foo").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "foo")
        }
        parser.find("/bar").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "root")
        }
        parser.find("/foox").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "root")
        }
        parser.find("/foo/").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "foo")
        }
        parser.find("/foo/x").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "foo")
        }
        parser.find("/bar/x").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "bar")
        }
    }

    @Test
    fun searchString() {
        val parser = RouteParser()
        parser.insert(route("/regex/{nid:[0-9]+}", "nid"))
        parser.insert(route("/regex/{zid:[0-9]+}/edit", "zid"))
        parser.insert(route("/articles/{id}", "id"))
        parser.insert(route("/articles/*", "*"))

        parser.find("/regex/678/edit").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "zid")
        }
        parser.find("/articles/tail/match").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "*")
        }
        parser.find("/articles/123").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "id")
        }
    }

    @Test
    fun searchParam() {
        val parser = RouteParser()
        parser.insert(route("/articles/{id}","id"))
        parser.insert(route("/articles/*","catchall"))

        parser.find("/articles/123").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "id")
        }
        parser.find("/articles/a/b").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "catchall")
        }
    }

    @Test
    fun multipleRegex() {
        val parser = RouteParser()
        parser.insert(route("/{lang:[a-z][a-z]}/{page:[^.]+}/","1515"))

        parser.find("/12/f/").let {
            assertNull(it)
        }
        parser.find("/ar/page/").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "1515")
        }
        parser.find("/arx/page/").let {
            assertNull(it)
        }
    }

    @Test
    fun regexWithQuantity() {
        val parser = RouteParser()
        parser.insert(route("/{lang:[a-z]{2}}/","qx"))
        parser.find("/12/").let {
            assertNull(it)
        }
        parser.find("/ar/").let {
            assertNotNull(it)
            assertTrue(it.route is TestRoute)
            assertEquals(it.route.id, "qx")
        }
    }

    private fun route(path: String, id: String): Route {
        return TestRoute(path, id)
    }
}

class TestRoute(
    override val route: String,
    val id: String,
    override val pathKeys: List<String> = emptyList(),
) : Route