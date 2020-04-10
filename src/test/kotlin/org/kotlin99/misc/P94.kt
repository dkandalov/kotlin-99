package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotlin99.common.tail
import org.kotlin99.graphs.Graph
import org.kotlin99.graphs.Graph.TermForm
import org.kotlin99.graphs.Graph.TermForm.Term
import org.kotlin99.graphs.isIsomorphicTo
import org.kotlin99.graphs.toGraph
import org.kotlin99.lists.combinations


fun findAllRegularGraphs(degree: Int, nodeAmount: Int): List<Graph<Int, Nothing>> {
    if (degree >= nodeAmount) return emptyList()
    val nodes = 0.until(nodeAmount).toList()
    return edgeCombinations(nodes, degree)
        .map { edges ->
            edges.toGraph()
        }
        .fold(emptyList()) { list, graph ->
            if (list.none { it.isIsomorphicTo(graph) }) list + graph else list
        }
}

private fun edgeCombinations(nodes: List<Int>, degree: Int, result: List<Pair<Int, Int>> = emptyList()): List<List<Pair<Int, Int>>> {
    fun List<Pair<Int, Int>>.degreeOf(node: Int) = count { it.first == node || it.second == node }

    if (nodes.isEmpty() || degree == 0) return emptyList()
    if (nodes.size == 1) {
        return if (result.degreeOf(nodes.first()) == degree) listOf(result) else emptyList()
    }

    val remainingDegree = degree - result.degreeOf(nodes.first())

    return combinations(remainingDegree, nodes.tail())
        .map { combination ->
            combination.map { Pair(nodes.first(), it) }
        }.flatMap { edges ->
        edgeCombinations(nodes.tail(), degree, edges + result)
    }
}

private fun <V> List<Pair<V, V>>.toGraph(): Graph<V, Nothing> {
    val nodes = flatMap { it.toList() }
    val edges = map { Term<V, Nothing>(it.first, it.second) }
    return Graph.terms(TermForm(nodes, edges))
}

private fun <V1, V2, L> Graph<V1, L>.mapValue(f: (V1) -> V2): Graph<V2, L> {
    val nodes = nodes.keys.map { f(it) }
    val edges = edges.map { Term(f(it.n1.value), f(it.n2.value), it.label) }
    return Graph.labeledTerms(TermForm(nodes, edges))
}

private fun Graph<*, *>.isRegular(degree: Int) = nodes.values.all { it.edges.size == degree }


class P94Test {
    @Test fun `regular graphs for 2 nodes`() {
        assertThat(findAllRegularGraphs(degree = 1, nodeAmount = 2), equalTo(intGraphs("[0-1]")))
        assertThat(findAllRegularGraphs(degree = 2, nodeAmount = 2), equalTo(emptyList()))
    }

    @Test fun `regular graphs for 3 nodes`() {
        assertThat(findAllRegularGraphs(degree = 1, nodeAmount = 3), equalTo(emptyList()))
        assertThat(findAllRegularGraphs(degree = 2, nodeAmount = 3), equalTo(intGraphs("[1-2, 0-2, 0-1]")))
    }

    @Test fun `regular graphs for 4 nodes`() {
        assertThat(findAllRegularGraphs(degree = 2, nodeAmount = 4), equalTo(intGraphs(
            "[2-3, 1-3, 0-2, 0-1]"
        )))
        assertThat(findAllRegularGraphs(degree = 3, nodeAmount = 4), equalTo(intGraphs(
            "[2-3, 1-3, 1-2, 0-3, 0-2, 0-1]"
        )))
    }

    @Test fun `non-isomorphic 3-regular graphs with 6 nodes`() {
        assertThat(findAllRegularGraphs(degree = 3, nodeAmount = 6), equalTo(intGraphs(
            "[4-5, 3-5, 3-4, 2-5, 1-4, 1-2, 0-3, 0-2, 0-1]",
            "[3-5, 3-4, 2-5, 2-4, 1-5, 1-4, 0-3, 0-2, 0-1]"
        )))
    }

    @Test fun `produced graphs are regular`() {
        findAllRegularGraphs(degree = 3, nodeAmount = 6).forEach {
            assertTrue(it.isRegular(degree = 3))
        }
    }

    private fun intGraphs(vararg graphs: String): List<Graph<Int, Nothing>> =
        graphs.map { it.toGraph().mapValue(String::toInt) }
}