package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.hasSameElementsAs

fun <T> Graph<T, *>.findCycles(node: T): List<List<T>> {
    fun findCycles(node: T, path: List<T>): List<List<T>> {
        if (path.size > 2 && path.first() == node) return listOf(path + node)
        return nodes[node]!!.neighbors()
                .filterNot{ path.drop(1).contains(it.value) }
                .flatMap{ findCycles(node, path + it.value) }
                .filter{ it.last() == node }
    }
    return findCycles(node, listOf(node))
}


class P82 {
    @Test fun `find cycles in graph`() {
        assertThat("[a]".toGraph().findCycles("a"), equalTo(listOf()))
        assertThat("[a-b]".toGraph().findCycles("a"), equalTo(listOf()))
        assertThat("[a-b, b-c, a-c]".toGraph().findCycles("a"), hasSameElementsAs(listOf(
                listOf("a", "b", "c", "a"),
                listOf("a", "c", "b", "a")
        )))
        assertThat("[a-b, b-c, a-c, a-d, d-c]".toGraph().findCycles("a"), hasSameElementsAs(listOf(
                listOf("a", "b", "c", "a"),
                listOf("a", "b", "d", "a"),
                listOf("a", "c", "b", "a"),
                listOf("a", "c", "d", "a"),
                listOf("a", "d", "b", "a"),
                listOf("a", "d", "c", "a")
        )))
    }
}