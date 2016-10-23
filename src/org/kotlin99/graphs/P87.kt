package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.graphs.Graph.Node
import java.util.*

fun <T> Graph<T, *>.nodesByDepthFrom(nodeValue: T): List<T> {
    fun nodesByDepth(nodeValues: List<T>, visited: List<T>): List<T> {
        if (nodeValues.isEmpty()) return emptyList()
        val head = nodeValues.first()
        if (visited.contains(head)) return nodesByDepth(nodeValues.drop(1), visited)

        return listOf(head) +
                nodesByDepth(nodes[head]!!.neighbors().map{it.value} + nodeValues.drop(1), visited + head)
    }
    return nodesByDepth(listOf(nodeValue), emptyList())
}

fun <T> Graph<T, *>.nodesByBreadthFrom(nodeValue: T): List<T> {
    fun nodesByBreadth(nodeQueue: List<Node<T, *>>, visited: LinkedHashSet<Node<T, *>>): LinkedHashSet<Node<T, *>> {
        if (nodeQueue.isEmpty()) return visited
        visited.add(nodeQueue.first())
        val updatedNodeQueue = nodeQueue.drop(1) + nodeQueue.first().neighbors()
        return nodesByBreadth(updatedNodeQueue.filterNot{ visited.contains(it) }, visited)
    }
    return nodesByBreadth(listOf(nodes[nodeValue]!!), LinkedHashSet()).map { it.value }
}

class P87Test {
    @Test fun `depth-first undirected graph traversal`() {
        assertThat("[a]".toGraph().nodesByDepthFrom("a"), equalTo(listOf("a")))
        assertThat("[a-b]".toGraph().nodesByDepthFrom("a"), equalTo(listOf("a", "b")))

        "[a-b, b-c]".toGraph().let {
            assertThat(it.nodesByDepthFrom("a"), equalTo(listOf("a", "b", "c")))
            assertThat(it.nodesByDepthFrom("b"), equalTo(listOf("b", "a", "c")))
            assertThat(it.nodesByDepthFrom("c"), equalTo(listOf("c", "b", "a")))
        }

        assertThat("[a-b, b-c, c-d, d-e]".toGraph().nodesByDepthFrom("c"), equalTo(listOf("c", "b", "a", "d", "e")))

        "[a-b, b-c, e, a-c, a-d]".toGraph().let {
            assertThat(it.nodesByDepthFrom("a"), equalTo(listOf("a", "b", "c", "d")))
            assertThat(it.nodesByDepthFrom("b"), equalTo(listOf("b", "a", "c", "d")))
            assertThat(it.nodesByDepthFrom("c"), equalTo(listOf("c", "b", "a", "d")))
            assertThat(it.nodesByDepthFrom("d"), equalTo(listOf("d", "a", "b", "c")))
        }
    }

    @Test fun `breadth-first undirected graph traversal`() {
        assertThat("[a]".toGraph().nodesByBreadthFrom("a"), equalTo(listOf("a")))
        assertThat("[a-b]".toGraph().nodesByBreadthFrom("a"), equalTo(listOf("a", "b")))

        "[a-b, b-c]".toGraph().let {
            assertThat(it.nodesByBreadthFrom("a"), equalTo(listOf("a", "b", "c")))
            assertThat(it.nodesByBreadthFrom("b"), equalTo(listOf("b", "a", "c")))
            assertThat(it.nodesByBreadthFrom("c"), equalTo(listOf("c", "b", "a")))
        }

        assertThat("[a-b, b-c, c-d, d-e]".toGraph().nodesByBreadthFrom("c"), equalTo(listOf("c", "b", "d", "a", "e")))

        "[a-b, b-c, e, a-c, a-d]".toGraph().let {
            assertThat(it.nodesByBreadthFrom("a"), equalTo(listOf("a", "b", "c", "d")))
            assertThat(it.nodesByBreadthFrom("b"), equalTo(listOf("b", "a", "c", "d")))
            assertThat(it.nodesByBreadthFrom("c"), equalTo(listOf("c", "b", "a", "d")))
            assertThat(it.nodesByBreadthFrom("d"), equalTo(listOf("d", "a", "b", "c")))
        }
    }

    @Test fun `breadth-first directed graph traversal`() {
        "[a>b, b>c]".toGraph().let{
            assertThat(it.nodesByBreadthFrom("a"), equalTo(listOf("a", "b", "c")))
            assertThat(it.nodesByBreadthFrom("b"), equalTo(listOf("b", "c")))
            assertThat(it.nodesByBreadthFrom("c"), equalTo(listOf("c")))
        }
    }
}