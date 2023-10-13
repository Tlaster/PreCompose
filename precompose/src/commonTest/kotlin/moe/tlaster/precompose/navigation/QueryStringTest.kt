package moe.tlaster.precompose.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QueryStringTest {
    @Test
    fun simpleQueryString() {
        QueryString("&foo=bar").let {
            assertTrue(it.map.size == 1)
            assertTrue(it.map.containsKey("foo"))
            assertTrue(it.map.containsValue(listOf("bar")))
            assertEquals(it.query("foo"), "bar")
        }

        QueryString("foo=bar&").let {
            assertTrue(it.map.size == 1)
            assertTrue(it.map.containsKey("foo"))
            assertTrue(it.map.containsValue(listOf("bar")))
            assertEquals(it.query("foo"), "bar")
        }

        QueryString("foo=bar&&").let {
            assertTrue(it.map.size == 1)
            assertTrue(it.map.containsKey("foo"))
            assertTrue(it.map.containsValue(listOf("bar")))
            assertEquals(it.query("foo"), "bar")
        }

        QueryString("foo=bar").let {
            assertTrue(it.map.size == 1)
            assertTrue(it.map.containsKey("foo"))
            assertTrue(it.map.containsValue(listOf("bar")))
            assertEquals(it.query("foo"), "bar")
        }

        QueryString("a=1&b=2").let {
            assertTrue(it.map.size == 2)
            assertTrue(it.map.containsKey("a"))
            assertTrue(it.map.containsValue(listOf("1")))
            assertTrue(it.map.containsKey("b"))
            assertTrue(it.map.containsValue(listOf("2")))
            assertEquals(it.query("a"), "1")
            assertEquals(it.query("b"), "2")
        }

        QueryString("a=1&b=2&").let {
            assertTrue(it.map.size == 2)
            assertTrue(it.map.containsKey("a"))
            assertTrue(it.map.containsValue(listOf("1")))
            assertTrue(it.map.containsKey("b"))
            assertTrue(it.map.containsValue(listOf("2")))
            assertEquals(it.query("a"), "1")
            assertEquals(it.query("b"), "2")
        }

        QueryString("a=1&&b=2&").let {
            assertTrue(it.map.size == 2)
            assertTrue(it.map.containsKey("a"))
            assertTrue(it.map.containsValue(listOf("1")))
            assertTrue(it.map.containsKey("b"))
            assertTrue(it.map.containsValue(listOf("2")))
            assertEquals(it.query("a"), "1")
            assertEquals(it.query("b"), "2")
        }

        QueryString("a=1&a=2").let {
            assertTrue(it.map.size == 1)
            assertTrue(it.map.containsKey("a"))
            assertTrue(it.map.containsValue(listOf("1", "2")))
            assertEquals(it.queryList("a"), listOf("1", "2"))
        }

        assertTrue(QueryString("a=1;a=2").map.isEmpty())

        QueryString("a=").let {
            assertTrue(it.map.size == 1)
            assertTrue(it.map.containsKey("a"))
            assertEquals(it.queryList("a"), emptyList<String>())
        }

        QueryString("a=&").let {
            assertTrue(it.map.size == 1)
            assertTrue(it.map.containsKey("a"))
            assertEquals(it.queryList("a"), emptyList<String>())
        }

        QueryString("a=&&").let {
            assertTrue(it.map.size == 1)
            assertTrue(it.map.containsKey("a"))
            assertEquals(it.queryList("a"), emptyList<String>())
        }

        assertTrue(QueryString("").map.isEmpty())
    }
}
