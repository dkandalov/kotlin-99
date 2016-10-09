package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.*


class Graph<T, U> {
    val nodes: MutableMap<T, Graph.Node<T>> = HashMap()
    val edges: MutableList<Edge<T, U>> = ArrayList()

    fun addNode(value: T): Node<T> {
        val node = Graph.Node(value)
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
        val edge = Edge(nodes[source]!!, nodes[dest]!!, value)
        edges.add(edge)
        nodes[source]!!.adj.add(edge)
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


    data class Edge<T, U>(val n1: Node<T>, val n2: Node<T>, val value: U) {
        fun target(node: Node<T>): Node<T>? =
                if (n1 == node) n2 else if (n2 == node) n1 else null

        fun equivalentTo(other: Edge<T, U>) =
                (n1 == other.n1 && n2 == other.n2) || (n1 == other.n2 && n2 == other.n1)

        override fun toString() = n1.toString() + "-" + n2
    }

    data class DirectedEdge<T, U>(val from: Node<T>, val to: Node<T>, val value: U) {
        fun target(node: Node<T>): Node<T>? =
            if (from == node) to
            else null
    }

    data class Node<T>(val value: T) {
        val adj: MutableList<Edge<T, *>> = ArrayList()
        fun neighbors(): List<Node<T>> = adj.map{ edge -> edge.target(this)!! }
        override fun toString() = value.toString()
    }

    companion object {
        fun <T> terms(nodes: List<T>, edges: List<Pair<T, T>>): Graph<T, *> {
            val graph = Graph<T, Any?>()
            nodes.forEach { graph.addNode(it) }
            edges.forEach { graph.addEdge(it.first, it.second, null) }
            return graph
        }

        fun <T> adjacent(vararg nodesWithNeighbors: Pair<T, List<T>>): Graph<T, *> {
            return adjacent(nodesWithNeighbors.toList())
        }

        fun <T> adjacent(nodesWithNeighbors: List<Pair<T, List<T>>>): Graph<T, *> {
            val graph = Graph<T, Any?>()
            nodesWithNeighbors.forEach { graph.addNode(it.first) }
            nodesWithNeighbors.forEach{
                val (nodeValue, adjacentNodeValues) = it
                adjacentNodeValues.forEach{ graph.addEdge(nodeValue, it, null) }
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

        assertThat(graph.nodes["a"]!!.neighbors(), equalTo(listOf(Graph.Node("b"))))
        assertThat(graph.nodes["b"]!!.neighbors(), equalTo(listOf(Graph.Node("a"))))
        assertThat(graph.nodes["c"]!!.neighbors(), equalTo(emptyList()))
    }

    @Test fun `create graph from list of nodes and edges`() {
        val graph = Graph.terms(nodes = listOf("b", "c", "d", "f", "g", "h", "k"),
                                edges = listOf(Pair("b", "c"), Pair("b", "f"), Pair("c", "f"), Pair("f", "k"), Pair("g", "h")))

        assertThat(graph.nodes.size, equalTo(7))
        assertThat(graph.edges.size, equalTo(5))
        assertThat(graph.toString(), equalTo("[b-c, b-f, c-f, f-k, g-h, d]"))

        assertThat(graph.nodes["f"]!!.neighbors(), equalTo(listOf(Graph.Node("b"), Graph.Node("c"), Graph.Node("k"))))
        assertThat(graph.nodes["g"]!!.neighbors(), equalTo(listOf(Graph.Node("h"))))
        assertThat(graph.nodes["d"]!!.neighbors(), equalTo(emptyList()))
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

        assertThat(graph.nodes.size, equalTo(7))
        assertThat(graph.edges.size, equalTo(5))
        assertThat(graph.toString(), equalTo("[b-c, b-f, c-f, f-k, g-h, d]"))

        assertThat(graph.nodes["f"]!!.neighbors(), equalTo(listOf(Graph.Node("b"), Graph.Node("c"), Graph.Node("k"))))
        assertThat(graph.nodes["g"]!!.neighbors(), equalTo(listOf(Graph.Node("h"))))
        assertThat(graph.nodes["d"]!!.neighbors(), equalTo(emptyList()))
    }
}