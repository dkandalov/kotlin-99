package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.graphs.Graph.AdjacencyList
import org.kotlin99.graphs.Graph.AdjacencyList.Entry
import org.kotlin99.graphs.Graph.AdjacencyList.Link

fun <V> Graph<V, *>.findAllPaths(from: V, to: V, path: List<V> = emptyList()): List<List<V>> {
    if (from == to) return listOf(path + to)
    return nodes[from]!!.neighbors()
        .filter { !path.contains(it.value) }
        .flatMap { findAllPaths(it.value, to, path + from) }
}

fun <V> Graph<V, Int>.findShortestPath(from: V, to: V): List<V> {
    fun distanceBetween(n1: V, n2: V): Int =
        nodes[n1]!!.edges.find { it.n1.value == n2 || it.n2.value == n2 }!!.label!!

    fun pathAsList(path: MutableMap<V, V>, current: V): List<V> =
        if (!path.containsKey(current)) listOf(current)
        else pathAsList(path, path[current]!!) + current

    fun neighborsOf(nodeValue: V): List<V> = nodes[nodeValue]!!.neighbors().map { it.value }

    val visited = mutableSetOf<V>()
    val queue = mutableSetOf(from)
    val path = mutableMapOf<V, V>()
    val scoreByNode = mutableMapOf(from to 0)

    while (queue.isNotEmpty()) {
        val node = queue.minBy { scoreByNode[it]!! }!!
        if (node == to) {
            return pathAsList(path, node)
        }
        queue.remove(node)
        visited.add(node)

        neighborsOf(node).filterNot { visited.contains(it) }.forEach { neighbor ->
            val score = scoreByNode[node]!! + distanceBetween(node, neighbor)
            val isNew = queue.add(neighbor)
            if (isNew || score < scoreByNode[neighbor]!!) {
                path[neighbor] = node
                scoreByNode[neighbor] = score
            }
        }
    }
    return emptyList()
}

fun <V, L> Graph<V, L>.findShortestPath(from: V, to: V, labelToInt: (L?) -> Int): List<V> {
    fun Graph<V, L>.toIntGraph(): Graph<V, Int> {
        return Graph.labeledAdjacent(AdjacencyList(this.toAdjacencyList().entries.map { (node, links) ->
            Entry(node, links.map { Link(it.node, labelToInt(it.label)) })
        }))
    }
    return toIntGraph().findShortestPath(from, to)
}

@JvmName("findShortestPathWithNothingLabels")
fun <V> Graph<V, Nothing>.findShortestPath(from: V, to: V): List<V> {
    fun Graph<V, Nothing>.toIntGraph(): Graph<V, Int> {
        return Graph.labeledAdjacent(AdjacencyList(this.toAdjacencyList().entries.map { (node, links) ->
            Entry(node, links.map { Link(it.node, 1) })
        }))
    }
    return toIntGraph().findShortestPath(from, to)
}


class P81Test {
    @Test fun `find paths in undirected graphs`() {
        assertThat("[a]".toGraph().findAllPaths("a", "a"), equalTo(listOf(listOf("a"))))
        assertThat("[a, b]".toGraph().findAllPaths("a", "b"), equalTo(emptyList()))

        assertThat("[a-b]".toGraph().findAllPaths("a", "b"), equalTo(listOf(listOf("a", "b"))))
        assertThat("[a-b]".toGraph().findAllPaths("b", "a"), equalTo(listOf(listOf("b", "a"))))

        assertThat("[a-b, b-c]".toGraph().findAllPaths("a", "c"), equalTo(listOf(listOf("a", "b", "c"))))
        assertThat("[a-b, b-c, a-c]".toGraph().findAllPaths("a", "c"), equalTo(listOf(listOf("a", "b", "c"), listOf("a", "c"))))
        assertThat("[a-b, b-c, a-c]".toGraph().findAllPaths("c", "a"), equalTo(listOf(listOf("c", "b", "a"), listOf("c", "a"))))
    }

    @Test fun `find paths in directed graphs`() {
        assertThat("[a>b]".toGraph().findAllPaths("a", "b"), equalTo(listOf(listOf("a", "b"))))
        assertThat("[a>b, b>c, c>a]".toGraph().findAllPaths("a", "c"), equalTo(listOf(listOf("a", "b", "c"))))

        assertThat("[p>q/9, m>q/7, k, p>m/5]".toLabeledGraph().findAllPaths("p", "q"), equalTo(listOf(listOf("p", "q"), listOf("p", "m", "q"))))
        assertThat("[p>q/9, m>q/7, k, p>m/5]".toLabeledGraph().findAllPaths("p", "k"), equalTo(listOf()))
    }

    @Test fun `find shortest path between nodes`() {
        assertThat("[a]".toGraph().findShortestPath("a", "a"), equalTo(listOf("a")))
        assertThat("[a, b]".toGraph().findShortestPath("a", "b"), equalTo(emptyList()))

        "[a-b/1, b-c/1, a-c/1]".toLabeledGraph().let {
            assertThat(it.findShortestPath("a", "a"), equalTo(listOf("a")))
            assertThat(it.findShortestPath("a", "b"), equalTo(listOf("a", "b")))
            assertThat(it.findShortestPath("a", "c"), equalTo(listOf("a", "c")))
        }
        "[a-b/1, b-c/1, a-c/3]".toLabeledGraph().let {
            assertThat(it.findShortestPath("a", "b"), equalTo(listOf("a", "b")))
            assertThat(it.findShortestPath("a", "c"), equalTo(listOf("a", "b", "c")))
        }

        "[a-b/1, a-c/1, b-e/1, c-d/1, d-e/1]".toLabeledGraph().let {
            assertThat(it.findShortestPath("a", "d"), equalTo(listOf("a", "c", "d")))
            assertThat(it.findShortestPath("a", "e"), equalTo(listOf("a", "b", "e")))
        }
        "[a-b/1, a-c/5, b-e/10, c-d/1, d-e/1]".toLabeledGraph().let {
            assertThat(it.findShortestPath("a", "d"), equalTo(listOf("a", "c", "d")))
            assertThat(it.findShortestPath("a", "e"), equalTo(listOf("a", "c", "d", "e")))
        }
    }
}