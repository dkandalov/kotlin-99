package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotlin99.common.containsAll
import org.kotlin99.common.tail
import org.kotlin99.graphs.Graph.*
import org.kotlin99.graphs.Graph.TermForm.Term
import org.kotlin99.graphs.P80Test.Companion.equivalentTo
import java.util.*

fun <V, L> Graph<V, L>.spanningTrees(): List<Graph<V, L>> {
    fun Edge<V, L>.contains(node: Node<V, L>) = n1 == node || n2 == node
    fun Edge<V, L>.connectsTo(nodes: List<Node<V, L>>) = nodes.contains(n1) != nodes.contains(n2)
    fun Edge<V, L>.toTerm() = Term(n1.value, n2.value, label)
    fun List<Graph<V, L>>.removeEquivalentGraphs(): List<Graph<V, L>> =
        fold(ArrayList()) { result, graph ->
            if (result.none { it.equivalentTo(graph) }) result.add(graph)
            result
        }

    fun spanningTrees(graphEdges: List<Edge<V, L>>, graphNodes: List<Node<V, L>>): List<Graph<V, L>> =
        if (graphNodes.isEmpty()) {
            listOf(Graph.labeledTerms(TermForm(nodes.keys, (edges - graphEdges).map { it.toTerm() })))
        } else graphEdges.filter { it.connectsTo(graphNodes) }.flatMap { edge ->
            spanningTrees(
                graphEdges.filterNot { it == edge },
                graphNodes.filterNot { edge.contains(it) }
            )
        }

    return spanningTrees(edges, nodes.values.tail()).removeEquivalentGraphs()
}

fun Graph<*, *>.isTree(): Boolean = spanningTrees().size == 1

fun Graph<*, *>.isConnected(): Boolean = spanningTrees().isNotEmpty()


class P83Test {
    @Test fun `find all spanning trees`() {
        assertThat("[a]".toGraph().spanningTrees(), containsAll(listOf("[a]".toGraph())))
        assertThat("[a-b]".toGraph().spanningTrees(), containsAll(listOf("[a-b]".toGraph())))

        assertThat("[a-b, b-c, c-a]".toGraph().spanningTrees(), containsAll(listOf(
            "[a-b, b-c]".toGraph(),
            "[a-b, c-a]".toGraph(),
            "[b-c, c-a]".toGraph()
        )) { equivalentTo(it) })

        "[a-b, b-c, b-d, b-e, a-f]".toGraph().let {
            assertThat(it.spanningTrees(), containsAll(listOf(it)) { equivalentTo(it) })
            assertThat(it.isTree(), equalTo(true))
        }
    }

    @Test fun `no spanning trees for disjoint graph`() {
        "[a-b, c-d]".toGraph().let {
            assertThat(it.spanningTrees(), equalTo(emptyList()))
            assertThat(it.isConnected(), equalTo(false))
        }
    }

    @Test fun `all spanning trees of graph from illustration`() {
        val graph = "[a-b, a-d, b-c, b-e, c-e, d-e, d-f, d-g, e-h, f-g, g-h]".toGraph()
        val spanningTrees = graph.spanningTrees()
        println(spanningTrees)

        assertTrue(spanningTrees.any { it.equivalentTo("[d-f, a-d, a-b, b-c, b-e, d-g, e-h]".toGraph()) })
        assertThat(spanningTrees.size, equalTo(112))
    }
}