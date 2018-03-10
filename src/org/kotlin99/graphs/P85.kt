package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotlin99.common.containsAll
import org.kotlin99.common.permutations

fun <V1, V2> Graph<V1, *>.isIsomorphicTo(graph: Graph<V2, *>) = this.isomorphicMappingTo(graph) != null

fun <V1, V2> Graph<V1, *>.isomorphicMappingTo(graph: Graph<V2, *>): List<Pair<V1, V2>>? {
    if (nodes.size != graph.nodes.size) return null

    val allMappings = nodes.values.toList().permutations().map { it zip graph.nodes.values }
    return allMappings.find { mapping ->
        mapping.all {
            val mappedNeighbors = it.first.neighbors().map { node -> mapping.find { it.first == node }!!.second }
            mappedNeighbors.toSet() == it.second.neighbors().toSet()
        }
    }?.map {
        Pair(it.first.value, it.second.value)
    }
}


class P85Test {
    @Test fun `graph isomorphism`() {
        assertThat("[a]".toGraph().isomorphicMappingTo("[1]".toGraph())!!, equalTo(listOf(Pair("a", "1"))))

        assertThat("[a-b]".toGraph().isomorphicMappingTo("[1-2]".toGraph())!!, containsAll(listOf(
            Pair("a", "1"),
            Pair("b", "2")
        )))
        assertFalse("[a-b]".toGraph().isIsomorphicTo("[1]".toGraph()))

        assertThat("[a-b, b-c]".toGraph().isomorphicMappingTo("[1-2, 2-3]".toGraph())!!, containsAll(listOf(
            Pair("a", "1"),
            Pair("b", "2"),
            Pair("c", "3")
        )))
        assertThat("[a-b, b-c]".toGraph().isomorphicMappingTo("[1-2, 1-3]".toGraph())!!, containsAll(listOf(
            Pair("a", "2"),
            Pair("b", "1"),
            Pair("c", "3")
        )))
        assertFalse("[a-b, b-c]".toGraph().isIsomorphicTo("[1-2, 3]".toGraph()))

        assertThat("[a-b, b-c, c-d, d-a]".toGraph().isomorphicMappingTo("[1-2, 2-3, 3-4, 4-1]".toGraph())!!, containsAll(listOf(
            Pair("a", "1"),
            Pair("b", "2"),
            Pair("c", "3"),
            Pair("d", "4")
        )))
        assertTrue("[a-b, b-c, c-d, d-a]".toGraph().isIsomorphicTo("[1-2, 2-3, 3-4, 4-1]".toGraph()))
        assertFalse("[a-b, b-c, c-d, d-a]".toGraph().isIsomorphicTo("[1-2, 2-3, 3-4, 4-2]".toGraph()))
    }
}