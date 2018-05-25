package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Test
import org.kotlin99.common.containsAll

fun <V, L> Graph<V, L>.components(): List<Graph<V, L>> {
    return nodes.keys.fold(emptyList()) { result, nodeValue ->
        if (result.any { it.nodes.contains(nodeValue) }) {
            result
        } else {
            val nodeValues = nodesByDepthFrom(nodeValue)
            val newGraph = Graph(
                nodes.values.filter { nodeValues.contains(it.value) },
                edges.filter { edge -> nodeValues.any { edge.target(nodes[it]!!) != null } }
            )
            result + newGraph
        }
    }
}

class P88Test {
    @Test fun `find connected components of a graph`() {
        assertThat("[a-b]".toGraph().components(), containsAll(listOf(
            "[a-b]".toGraph()
        )))
        assertThat("[a-b, c-d]".toGraph().components(), containsAll(listOf(
            "[a-b]".toGraph(),
            "[c-d]".toGraph()
        )))
    }
}