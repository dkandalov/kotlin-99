package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotlin99.common.IsIterableContainingInAnyOrder.Companion.containsInAnyOrder
import org.kotlin99.graphs.Graph.*
import org.kotlin99.graphs.Graph.TermForm.Term
import org.kotlin99.graphs.P80Test.Companion.equivalentTo
import java.util.*

fun <T, U> Graph<T, U>.spanningTrees(): List<Graph<T, U>> {
    fun Edge<T, U>.contains(node: Node<T, U>) = n1 == node || n2 == node
    fun Edge<T, U>.connectsTo(nodes: List<Node<T, U>>) = nodes.contains(n1) != nodes.contains(n2)
    fun List<Graph<T, U>>.removeEquivalentGraphs(): List<Graph<T, U>> {
        val result = ArrayList<Graph<T, U>>()
        forEach { graph -> if (result.none{ it.equivalentTo(graph) }) result.add(graph) }
        return result
    }

    fun spanningTrees(graphEdges: List<Edge<T, U>>, graphNodes: List<Node<T, U>>, treeEdges: List<Edge<T, U>>): List<Graph<T, U>> =
        if (graphNodes.isEmpty()) {
            val nodeValues = nodes.keys.toList()
            val terms = treeEdges.map{ Term(it.n1.value, it.n2.value, it.label) }
            listOf(Graph.labeledTerms(TermForm(nodeValues, terms)))
        }
        else graphEdges.filter{ it.connectsTo(graphNodes) }.flatMap { edge ->
            spanningTrees(
                graphEdges.filterNot{ it == edge },
                graphNodes.filterNot{ edge.contains(it) },
                treeEdges + edge
            )
        }

    return spanningTrees(edges.toList(), nodes.values.toList().drop(1), emptyList()).removeEquivalentGraphs()
}

fun Graph<*, *>.isTree(): Boolean = spanningTrees().size == 1

fun Graph<*, *>.isConnected(): Boolean = spanningTrees().size > 0


class P83Test {
    @Test fun `find all spanning trees`() {
        assertThat("[a]".toGraph().spanningTrees(), containsInAnyOrder(listOf("[a]".toGraph())))
        assertThat("[a-b]".toGraph().spanningTrees(), containsInAnyOrder(listOf("[a-b]".toGraph())))

        assertThat("[a-b, b-c, c-a]".toGraph().spanningTrees(), containsInAnyOrder(listOf(
                "[a-b, b-c]".toGraph(),
                "[a-b, c-a]".toGraph(),
                "[b-c, c-a]".toGraph()
        )){ equivalentTo(it) })

        "[a-b, b-c, b-d, b-e, a-f]".toGraph().let {
            assertThat(it.spanningTrees(), containsInAnyOrder(listOf(it)){ equivalentTo(it) })
            assertThat(it.isTree(), equalTo(true))
        }
    }

    @Test fun `no spanning trees for disjoint graph`() {
        "[a-b, c-d]".toGraph().let {
            assertThat(it.spanningTrees(), equalTo(emptyList()))
            assertThat(it.isConnected(), equalTo(false))
        }
    }

    @Test fun `all spanning of graph from task example`() {
        val graph = "[a-b, a-d, b-c, b-e, c-e, d-e, d-f, d-g, e-h, f-g, g-h]".toGraph()
        val spanningTrees = graph.spanningTrees()
        println(spanningTrees)

        assertTrue(spanningTrees.any{ it.equivalentTo("[d-f, a-d, a-b, b-c, b-e, d-g, e-h]".toGraph()) })
        assertThat(spanningTrees.size, equalTo(112))
    }
}