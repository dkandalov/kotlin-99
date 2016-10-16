package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.graphs.Graph.Edge
import org.kotlin99.graphs.Graph.Node
import org.kotlin99.graphs.Graph.TermForm.Term
import org.kotlin99.graphs.P80Test.Companion.equivalentTo


fun <T, U: Comparable<U>> Graph<T, U>.minSpanningTree(): Graph<T, U> {
    fun Edge<T, U>.contains(node: Node<T, U>) = n1 == node || n2 == node
    fun Edge<T, U>.connectsTo(nodes: List<Node<T, U>>) = nodes.contains(n1) != nodes.contains(n2)

    fun spanningTrees(graphEdges: List<Edge<T, U>>, graphNodes: List<Node<T, U>>, treeEdges: List<Edge<T, U>>): Graph<T, U> {
        if (graphNodes.isEmpty()) {
            val nodeValues = nodes.keys.toList()
            val terms = treeEdges.map{ Term(it.n1.value, it.n2.value, it.label) }
            return Graph.labeledTerms(Graph.TermForm(nodeValues, terms))
        } else {
            val edge = graphEdges.filter { it.connectsTo(graphNodes) }.minBy{ it.label!! }!!
            return spanningTrees(
                graphEdges.filterNot{ it == edge },
                graphNodes.filterNot{ edge.contains(it) },
                treeEdges + edge
            )
        }
    }

    return spanningTrees(edges, nodes.values.drop(1), emptyList())
}

class P84Test {
    @Test fun `minimum spanning tree`() {
        assertThat("[a-b/1]".toLabeledGraph().minSpanningTree(), equivalentTo("[a-b/1]".toLabeledGraph()))
        assertThat("[a-b/1, b-c/2]".toLabeledGraph().minSpanningTree(), equivalentTo("[a-b/1, b-c/2]".toLabeledGraph()))
        assertThat("[a-b/1, b-c/2, a-c/3]".toLabeledGraph().minSpanningTree(), equivalentTo("[a-b/1, b-c/2]".toLabeledGraph()))
    }

    @Test fun `minimum spanning tree for graph from illustration`() {
        val graph = "[a-b/5, a-d/3, b-c/2, b-e/4, c-e/6, d-e/7, d-f/4, d-g/3, e-h/5, f-g/4, g-h/1]".toLabeledGraph()
        assertThat(graph.minSpanningTree(), equivalentTo("[d-f/4, a-d/3, d-g/3, g-h/1, a-b/5, b-c/2, b-e/4]".toLabeledGraph()))
        assertThat(graph.minSpanningTree().edges.sumBy { it.label!! }, equalTo(22))
    }
}