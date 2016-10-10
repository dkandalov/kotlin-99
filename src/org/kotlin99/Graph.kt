package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.Graph.Node
import java.util.*


class Graph<T, U> {
    val nodes: MutableMap<T, Node<T>> = HashMap()
    val edges: MutableList<Link<T, U>> = ArrayList()

    fun addNode(value: T): Node<T> {
        val node = Node(value)
        nodes.put(value, node)
        return node
    }

    fun addEdge(n1: T, n2: T, value: U) {
        if (!nodes.contains(n1) || !nodes.contains(n2)) {
            throw IllegalStateException("Expected '$n1' and '$n2' nodes to exist in graph")
        }
        val edge = Edge(nodes[n1]!!, nodes[n2]!!, value)
        if (edges.all{ !it.equivalentTo(edge) }) {
            edges.add(edge)
            nodes[n1]!!.adj.add(edge)
            nodes[n2]!!.adj.add(edge)
        }
    }

    fun addArc(source: T, dest: T, value: U) {
        val arc = Arc(nodes[source]!!, nodes[dest]!!, value)
        if (!edges.contains(arc)) {
            edges.add(arc)
            nodes[source]!!.adj.add(arc)
        }
    }

    override fun toString(): String {
        val standaloneNodes = nodes.values.filter{ node -> edges.all { it.n1 != node && it.n2 != node } }
        val s = (edges.map{ it.toString() } + standaloneNodes.map{ it.toString() }).joinToString()
        return "[$s]"
    }

    override fun equals(other: Any?): Boolean{
        if (this === other || other?.javaClass != javaClass) return false
        other as Graph<*, *>
        return nodes == other.nodes && edges == other.edges
    }

    override fun hashCode() = 31 * nodes.hashCode() + edges.hashCode()


    data class Node<T>(val value: T) {
        val adj: MutableList<Link<T, *>> = ArrayList()
        fun neighbors(): List<Node<T>> = adj.map{ edge -> edge.target(this)!! }
        override fun toString() = value.toString()
    }

    interface Link<T, U> {
        val n1: Node<T>
        val n2: Node<T>
        fun target(node: Node<T>): Node<T>?
        fun equivalentTo(other: Edge<T, U>) =
                (n1 == other.n1 && n2 == other.n2) || (n1 == other.n2 && n2 == other.n1)
    }

    data class Edge<T, U>(override val n1: Node<T>, override val n2: Node<T>, val value: U) : Link<T, U> {
        override fun target(node: Node<T>) = if (n1 == node) n2 else if (n2 == node) n1 else null
        override fun toString() = n1.toString() + "-" + n2
    }

    data class Arc<T, U>(override val n1: Node<T>, override val n2: Node<T>, val value: U) : Link<T, U> {
        override fun target(node: Node<T>) = if (n1 == node) n2 else null
        override fun toString() = n1.toString() + ">" + n2
    }


    companion object {
        fun <T> terms(nodes: List<T>, edges: List<Pair<T, T>>): Graph<T, *> {
            return createFromTerms(nodes, edges) { graph, n1, n2, value ->
                graph.addEdge(n1, n2, value)
            }
        }

        fun <T> arcTerms(nodes: List<T>, arcs: List<Pair<T, T>>): Graph<T, *> {
            return createFromTerms(nodes, arcs) { graph, n1, n2, value ->
                graph.addArc(n1, n2, value)
            }
        }

        fun <T> adjacent(vararg nodesWithNeighbors: Pair<T, List<T>>): Graph<T, *> {
            return adjacent(nodesWithNeighbors.toList())
        }

        fun <T> adjacent(nodesWithNeighbors: List<Pair<T, List<T>>>): Graph<T, *> {
            return fromAdjacencyList(nodesWithNeighbors) { graph, n1, n2, value ->
                graph.addEdge(n1, n2, value)
            }
        }

        fun <T> arcAdjacent(vararg nodesWithNeighbors: Pair<T, List<T>>): Graph<T, *> {
            return arcAdjacent(nodesWithNeighbors.toList())
        }

        fun <T> arcAdjacent(nodesWithNeighbors: List<Pair<T, List<T>>>): Graph<T, *> {
            return fromAdjacencyList(nodesWithNeighbors) { graph, n1, n2, value ->
                graph.addArc(n1, n2, value)
            }
        }

        private fun <T> createFromTerms(nodes: List<T>, edges: List<Pair<T, T>>,
                                        addFunction: (Graph<T, Any?>, T, T, Any?) -> Unit): Graph<T, *> {
            val graph = Graph<T, Any?>()
            nodes.forEach { graph.addNode(it) }
            edges.forEach { addFunction(graph, it.first, it.second, null) }
            return graph
        }

        private fun <T> fromAdjacencyList(nodesWithNeighbors: List<Pair<T, List<T>>>,
                                          addFunction: (Graph<T, Any?>, T, T, Any?) -> Unit): Graph<T, *> {
            val graph = Graph<T, Any?>()
            nodesWithNeighbors.forEach { graph.addNode(it.first) }
            nodesWithNeighbors.forEach{
                val (nodeValue, adjacentNodeValues) = it
                adjacentNodeValues.forEach{ addFunction(graph, nodeValue, it, null) }
            }
            return graph
        }
    }
}


class GraphTest {
    @Test fun `create simple graph from list of nodes and edges`() {
        val graph = Graph.terms(nodes = listOf("a", "b", "c"), edges = listOf(Pair("a", "b")))

        assertThat(graph.nodes.size, equalTo(3))
        assertThat(graph.edges.size, equalTo(1))
        assertThat(graph.toString(), equalTo("[a-b, c]"))

        assertThat(graph.nodes["a"]!!.neighbors(), equalTo(listOf(Node("b"))))
        assertThat(graph.nodes["b"]!!.neighbors(), equalTo(listOf(Node("a"))))
        assertThat(graph.nodes["c"]!!.neighbors(), equalTo(emptyList()))
    }

    @Test fun `create graph from list of nodes and edges`() {
        val graph = Graph.terms(nodes = listOf("b", "c", "d", "f", "g", "h", "k"),
                                edges = listOf(Pair("b", "c"), Pair("b", "f"), Pair("c", "f"), Pair("f", "k"), Pair("g", "h")))
        assertPropertiesOfGraphFromIllustration(graph)
    }

    @Test fun `create graph from adjacency list`() {
        val graph = Graph.adjacent(
                Pair("b", listOf("c", "f")),
                Pair("c", listOf("b", "f")),
                Pair("d", emptyList()),
                Pair("f", listOf("b", "c", "k")),
                Pair("g", listOf("h")),
                Pair("h", listOf("g")),
                Pair("k", listOf("f")))

        assertPropertiesOfGraphFromIllustration(graph)
    }

    @Test fun `create directed graph from list of nodes and edges`() {
        val graph = Graph.arcTerms(listOf("r", "s", "t", "u", "v"),
                                   listOf(Pair("s", "r"), Pair("s", "u"), Pair("u", "r"), Pair("u", "s"), Pair("v", "u")))
        assertPropertiesOfDirectedGraphFromIllustration(graph)
    }

    @Test fun `create directed graph from adjacency list`() {
        val graph = Graph.arcAdjacent(
                Pair("r", emptyList()),
                Pair("s", listOf("r", "u")),
                Pair("t", emptyList()),
                Pair("u", listOf("r", "s")),
                Pair("v", listOf("u")))
        assertPropertiesOfDirectedGraphFromIllustration(graph)
    }

    private fun assertPropertiesOfGraphFromIllustration(graph: Graph<String, *>) {
        assertThat(graph.nodes.size, equalTo(7))
        assertThat(graph.edges.size, equalTo(5))
        assertThat(graph.toString(), equalTo("[b-c, b-f, c-f, f-k, g-h, d]"))

        assertThat(graph.nodes["f"]!!.neighbors(), equalTo(listOf(Node("b"), Node("c"), Node("k"))))
        assertThat(graph.nodes["g"]!!.neighbors(), equalTo(listOf(Node("h"))))
        assertThat(graph.nodes["d"]!!.neighbors(), equalTo(emptyList()))
    }

    private fun assertPropertiesOfDirectedGraphFromIllustration(graph: Graph<String, *>) {
        assertThat(graph.nodes.size, equalTo(5))
        assertThat(graph.edges.size, equalTo(5))
        assertThat(graph.toString(), equalTo("[s>r, s>u, u>r, u>s, v>u, t]"))

        assertThat(graph.nodes["s"]!!.neighbors(), equalTo(listOf(Node("r"), Node("u"))))
        assertThat(graph.nodes["v"]!!.neighbors(), equalTo(listOf(Node("u"))))
        assertThat(graph.nodes["r"]!!.neighbors(), equalTo(emptyList()))
    }
}