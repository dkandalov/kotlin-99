package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.hasSameElementsAs
import org.kotlin99.graphs.Graph.*
import org.kotlin99.graphs.Graph.TermForm.Term
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


class P83 {
    @Test fun `find all spanning trees`() {
        assertThat("[a]".toGraph().spanningTrees(), hasSameElementsAs(listOf("[a]".toGraph())))
        assertThat("[a-b]".toGraph().spanningTrees(), hasSameElementsAs(listOf("[a-b]".toGraph())))
        assertThat("[a-b, b-c, c-a]".toGraph().spanningTrees(), hasSameElementsAs(listOf(
                "[a-b, b-c]".toGraph(),
                "[a-b, c-a]".toGraph(),
                "[b-c, c-a]".toGraph()
        )))
        "[a-b, b-c, b-d, b-e, a-f]".toGraph().let {
            assertThat(it.spanningTrees(), hasSameElementsAs(listOf(it)))
            assertThat(it.isTree(), equalTo(true))
        }
    }

    @Test fun `no spanning trees for disjoint graph`() {
        "[a-b, c-d]".toGraph().let {
            assertThat(it.spanningTrees(), equalTo(emptyList()))
            assertThat(it.isConnected(), equalTo(false))
        }
    }

    @Test fun `all spanning for example graph`() {
        TODO()
    }
}