package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.containsAll
import org.kotlin99.common.tail

fun <V> Graph<V, *>.findCycles(node: V): List<List<V>> {
    fun findCycles(path: List<V>): List<List<V>> {
        if (path.size > 3 && path.first() == path.last()) return listOf(path)
        return nodes[path.last()]!!.neighbors()
            .filterNot { path.tail().contains(it.value) }
            .flatMap { findCycles(path + it.value) }
    }
    return findCycles(listOf(node))
}


class P82Test {
    @Test fun `find cycles in undirected graph`() {
        assertThat("[a]".toGraph().findCycles("a"), equalTo(listOf()))
        assertThat("[a-b]".toGraph().findCycles("a"), equalTo(listOf()))
        assertThat("[a-b, b-c, a-c]".toGraph().findCycles("a"), containsAll(listOf(
            listOf("a", "b", "c", "a"),
            listOf("a", "c", "b", "a")
        )))
        assertThat("[a-b, b-c, a-c, a-d, d-c]".toGraph().findCycles("a"), containsAll(listOf(
            listOf("a", "b", "c", "a"),
            listOf("a", "c", "b", "a"),
            listOf("a", "c", "d", "a"),
            listOf("a", "d", "c", "a"),
            listOf("a", "d", "c", "b", "a"),
            listOf("a", "b", "c", "d", "a")
        )))

        assertThat("[b-c, b-f, c-f, f-k, g-h, d]".toGraph().findCycles("f"), containsAll(listOf(
            listOf("f", "b", "c", "f"),
            listOf("f", "c", "b", "f")
        )))
    }

    @Test fun `find cycles in directed graph`() {
        assertThat("[a>b, b>c, a>c]".toGraph().findCycles("a"), containsAll(listOf()))
        assertThat("[a>b, b>c, c>a]".toGraph().findCycles("a"), containsAll(listOf(
            listOf("a", "b", "c", "a")
        )))
    }
}