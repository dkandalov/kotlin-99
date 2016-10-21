package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotlin99.common.containsAll
import java.util.*

fun <T1, T2> Graph<T1, *>.isIsomorphicTo(graph: Graph<T2, *>): Boolean {
    return this.isomorphicMappingTo(graph) != null
}

fun <T1, T2> Graph<T1, *>.isomorphicMappingTo(graph: Graph<T2, *>): List<Pair<T1, T2>>? {
    if (nodes.size != graph.nodes.size) return null
    val allMappings = nodes.values.toList().combinations().map { it zip graph.nodes.values }
    return allMappings.find { mapping ->
        mapping.all {
            val mappedNeighbors = it.first.neighbors().map { node -> mapping.find{ it.first == node }!!.second }
            mappedNeighbors.toSet() == it.second.neighbors().toSet()
        }
    }?.map{
        Pair(it.first.value, it.second.value)
    }
}

private fun <T> List<T>.combinations(): List<List<T>> {
    if (size <= 1) return listOf(this)
    val head = first()
    return drop(1).combinations().flatMap{ subCombination ->
        (0..subCombination.size).map { i ->
            LinkedList(subCombination).apply{ add(i, head) }
        }
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
                Pair("a", "4"),
                Pair("b", "3"),
                Pair("c", "2"),
                Pair("d", "1")
        )))
        assertTrue("[a-b, b-c, c-d, d-a]".toGraph().isIsomorphicTo("[1-2, 2-3, 3-4, 4-1]".toGraph()))
        assertFalse("[a-b, b-c, c-d, d-a]".toGraph().isIsomorphicTo("[1-2, 2-3, 3-4, 4-2]".toGraph()))
    }

    @Test fun `combinations of collection`() {
        assertThat(emptyList<Int>().combinations(), equalTo(listOf(emptyList<Int>())))
        assertThat(listOf(1).combinations(), equalTo(listOf(listOf(1))))
        assertThat(listOf(1).combinations(), equalTo(listOf(listOf(1))))

        assertThat(listOf(1, 2).combinations(), containsAll(listOf(listOf(1, 2), listOf(2, 1))))
        assertThat(listOf(1, 2).combinations(), containsAll(listOf(listOf(1, 2), listOf(2, 1))))
        assertThat(listOf(1, 2, 3).combinations(), containsAll(listOf(
                listOf(1, 2, 3),
                listOf(1, 3, 2),
                listOf(2, 1, 3),
                listOf(2, 3, 1),
                listOf(3, 1, 2),
                listOf(3, 2, 1)
        )))
    }
}