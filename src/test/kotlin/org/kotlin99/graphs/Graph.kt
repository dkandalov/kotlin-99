package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.graphs.Graph.*
import org.kotlin99.graphs.Graph.AdjacencyList.Entry
import org.kotlin99.graphs.Graph.AdjacencyList.Entry.Companion.links
import org.kotlin99.graphs.Graph.AdjacencyList.Link
import org.kotlin99.graphs.Graph.TermForm.Term
import java.util.*


/**
 * [V] - type of node value
 * [L] - type of edge label
 */
class Graph<V, L>(nodes: Collection<Node<V, L>> = emptyList(), edges: Collection<Edge<V, L>> = emptyList()) {
    val nodes: MutableMap<V, Node<V, L>> = nodes
        .map { Pair(it.value, it) }
        .toMap(LinkedHashMap()) // Use linked map to make operations on graph more deterministic.
    val edges: MutableList<Edge<V, L>> = edges.toMutableList()

    private fun addNode(value: V): Node<V, L> {
        val node = Node<V, L>(value)
        nodes[value] = node
        return node
    }

    private fun addUndirectedEdge(n1: V, n2: V, label: L?) {
        if (!nodes.contains(n1) || !nodes.contains(n2)) {
            throw IllegalStateException("Expected '$n1' and '$n2' nodes to exist in graph")
        }
        val edge = UndirectedEdge(nodes[n1]!!, nodes[n2]!!, label)
        if (edges.none { it.equivalentTo(edge) }) {
            edges.add(edge)
            nodes[n1]!!.edges.add(edge)
            nodes[n2]!!.edges.add(edge)
        }
    }

    private fun addDirectedEdge(source: V, dest: V, label: L?) {
        val edge = DirectedEdge(nodes[source]!!, nodes[dest]!!, label)
        if (!edges.contains(edge)) {
            edges.add(edge)
            nodes[source]!!.edges.add(edge)
        }
    }

    override fun toString(): String {
        val standaloneNodes = nodes.values.filter { node -> edges.all { it.n1 != node && it.n2 != node } }
        val s = (edges.map { it.toString() } + standaloneNodes.map { it.toString() }).joinToString()
        return "[$s]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Graph<*, *>
        return nodes == other.nodes && edges == other.edges
    }

    override fun hashCode() = 31 * nodes.hashCode() + edges.hashCode()

    fun equivalentTo(other: Graph<V, L>): Boolean {
        return nodes == other.nodes && edges.all { edge -> other.edges.any { it.equivalentTo(edge) } }
    }


    data class Node<V, L>(val value: V) {
        val edges: MutableList<Edge<V, L>> = ArrayList()
        fun neighbors(): List<Node<V, L>> = edges.map { edge -> edge.target(this)!! }
        override fun toString() = value.toString()
    }

    interface Edge<V, L> {
        val n1: Node<V, L>
        val n2: Node<V, L>
        val label: L?
        fun target(node: Node<V, L>): Node<V, L>?
        fun equivalentTo(other: Edge<V, L>) =
            (n1 == other.n1 && n2 == other.n2) || (n1 == other.n2 && n2 == other.n1)
    }

    data class UndirectedEdge<V, L>(override val n1: Node<V, L>, override val n2: Node<V, L>, override val label: L?): Edge<V, L> {
        override fun target(node: Node<V, L>) = if (n1 == node) n2 else if (n2 == node) n1 else null
        override fun toString() = "$n1-$n2${if (label == null) "" else "/$label"}"
    }

    data class DirectedEdge<V, L>(override val n1: Node<V, L>, override val n2: Node<V, L>, override val label: L?): Edge<V, L> {
        override fun target(node: Node<V, L>) = if (n1 == node) n2 else null
        override fun toString() = "$n1>$n2${if (label == null) "" else "/$label"}"
    }


    data class TermForm<out V, out L>(val nodes: Collection<V>, val edges: List<Term<V, L>>) {
        data class Term<out V, out L>(val n1: V, val n2: V, val label: L? = null) {
            override fun toString() = if (label == null) "Term($n1, $n2)" else "Term($n1, $n2, $label)"
        }
    }

    data class AdjacencyList<V, out L>(val entries: List<Entry<V, L>>) {
        constructor(vararg entries: Entry<V, L>): this(entries.asList())
        override fun toString() = "AdjacencyList(${entries.joinToString()})"

        data class Entry<out V, out L>(val node: V, val links: List<Link<V, L>> = emptyList<Nothing>()) {
            constructor(node: V, vararg links: Link<V, L>): this(node, links.asList())
            override fun toString() = "Entry($node, links[${links.joinToString()}])"
            companion object {
                fun <V> links(vararg linkValues: V): List<Link<V, Nothing>> = linkValues.map { Link(it, null) }
            }
        }

        data class Link<out V, out L>(val node: V, val label: L? = null) {
            override fun toString() = if (label == null) "$node" else "$node/$label"
        }
    }

    companion object {
        fun <V> terms(termForm: TermForm<V, Nothing>): Graph<V, Nothing> =
            createFromTerms(termForm) { graph, n1, n2, value -> graph.addUndirectedEdge(n1, n2, value) }

        fun <V> directedTerms(termForm: TermForm<V, Nothing>): Graph<V, Nothing> =
            createFromTerms(termForm) { graph, n1, n2, value -> graph.addDirectedEdge(n1, n2, value) }

        fun <V, L> labeledTerms(termForm: TermForm<V, L>): Graph<V, L> =
            createFromTerms(termForm) { graph, n1, n2, value -> graph.addUndirectedEdge(n1, n2, value) }

        fun <V, L> labeledDirectedTerms(termForm: TermForm<V, L>): Graph<V, L> =
            createFromTerms(termForm) { graph, n1, n2, value -> graph.addDirectedEdge(n1, n2, value) }

        fun <V> adjacent(adjacencyList: AdjacencyList<V, Nothing>): Graph<V, *> =
            fromAdjacencyList(adjacencyList) { graph, n1, n2, value ->
                graph.addUndirectedEdge(n1, n2, value)
            }

        fun <V> directedAdjacent(adjacencyList: AdjacencyList<V, Nothing>): Graph<V, *> =
            fromAdjacencyList(adjacencyList) { graph, n1, n2, value -> graph.addDirectedEdge(n1, n2, value) }

        fun <V, L> labeledAdjacent(adjacencyList: AdjacencyList<V, L>): Graph<V, L> =
            fromAdjacencyList(adjacencyList) { graph, n1, n2, value ->
                graph.addUndirectedEdge(n1, n2, value)
            }

        fun <V, L> labeledDirectedAdjacent(adjacencyList: AdjacencyList<V, L>): Graph<V, L> =
            fromAdjacencyList(adjacencyList) { graph, n1, n2, value ->
                graph.addDirectedEdge(n1, n2, value)
            }

        private fun <V, L> createFromTerms(termForm: TermForm<V, L>, addFunction: (Graph<V, L>, V, V, L?) -> Unit): Graph<V, L> {
            val graph = Graph<V, L>()
            termForm.nodes.forEach { graph.addNode(it) }
            termForm.edges.forEach { addFunction(graph, it.n1, it.n2, it.label) }
            return graph
        }

        private fun <V, L> fromAdjacencyList(adjacencyList: AdjacencyList<V, L>, addFunction: (Graph<V, L>, V, V, L?) -> Unit): Graph<V, L> {
            val graph = Graph<V, L>()
            adjacencyList.entries.forEach { graph.addNode(it.node) }
            adjacencyList.entries.forEach { (node, links) ->
                links.forEach { addFunction(graph, node, it.node, it.label) }
            }
            return graph
        }
    }
}


class GraphTest {
    @Test fun `create simple graph from list of nodes and edges`() {
        val graph = Graph.terms(TermForm(nodes = listOf("a", "b", "c"), edges = listOf(Term("a", "b"))))

        assertThat(graph.nodes.size, equalTo(3))
        assertThat(graph.edges.size, equalTo(1))
        assertThat(graph.toString(), equalTo("[a-b, c]"))

        assertThat(graph.nodes["a"]!!.neighbors(), equalTo(listOf(Node("b"))))
        assertThat(graph.nodes["b"]!!.neighbors(), equalTo(listOf(Node("a"))))
        assertThat(graph.nodes["c"]!!.neighbors(), equalTo(emptyList()))
    }

    @Test fun `create graph from list of nodes and edges`() {
        val graph = Graph.terms(TermForm(
            nodes = listOf("b", "c", "d", "f", "g", "h", "k"),
            edges = listOf(Term("b", "c"), Term("b", "f"), Term("c", "f"), Term("f", "k"), Term("g", "h"))
        ))
        graph.assertPropertiesOfUndirectedGraphExample()
    }

    @Test fun `create graph from adjacency list`() {
        val graph = Graph.adjacent(AdjacencyList(
            Entry("b", links("c", "f")),
            Entry("c", links("b", "f")),
            Entry("d"),
            Entry("f", links("b", "c", "k")),
            Entry("g", links("h")),
            Entry("h", links("g")),
            Entry("k", links("f"))
        ))
        graph.assertPropertiesOfUndirectedGraphExample()
    }

    @Test fun `create directed graph from list of nodes and edges`() {
        val graph = Graph.directedTerms(TermForm(
            listOf("r", "s", "t", "u", "v"),
            listOf(Term("s", "r"), Term("s", "u"), Term("u", "r"), Term("u", "s"), Term("v", "u"))
        ))
        graph.assertPropertiesOfDirectedGraphExample()
    }

    @Test fun `create directed graph from adjacency list`() {
        val graph = Graph.directedAdjacent(AdjacencyList(
            Entry("r"),
            Entry("s", links("r", "u")),
            Entry("t"),
            Entry("u", links("r", "s")),
            Entry("v", links("u"))
        ))
        graph.assertPropertiesOfDirectedGraphExample()
    }

    @Test fun `create labeled undirected graph`() {
        val graph = Graph.labeledTerms(TermForm(
            listOf("k", "m", "p", "q"),
            listOf(Term("m", "q", 7), Term("p", "m", 5), Term("p", "q", 9))
        ))
        graph.assertPropertiesOfUndirectedLabeledGraphExample()
    }

    @Test fun `create labeled directed graph`() {
        val graph = Graph.labeledDirectedTerms(TermForm(listOf("k", "m", "p", "q"), listOf(Term("m", "q", 7), Term("p", "m", 5), Term("p", "q", 9))))
        graph.assertPropertiesOfDirectedLabeledGraphExample()
    }

    @Test fun `create labeled undirected graph from adjacency list`() {
        val graph = Graph.labeledAdjacent(AdjacencyList(
            Entry("k"),
            Entry("m", Link("q", 7)),
            Entry("p", Link("m", 5), Link("q", 9)),
            Entry("q")
        ))
        graph.assertPropertiesOfUndirectedLabeledGraphExample()
    }

    @Test fun `create labeled directed graph from adjacency list`() {
        val graph = Graph.labeledDirectedAdjacent(AdjacencyList(
            Entry("k"),
            Entry("m", Link("q", 7)),
            Entry("p", Link("m", 5), Link("q", 9)),
            Entry("q")
        ))
        graph.assertPropertiesOfDirectedLabeledGraphExample()
    }

    companion object {
        fun Graph<String, *>.assertPropertiesOfUndirectedGraphExample() {
            assertThat(nodes.size, equalTo(7))
            assertThat(edges.size, equalTo(5))
            assertThat(toString(), equalTo("[b-c, b-f, c-f, f-k, g-h, d]"))

            assertThat(nodes["f"]!!.neighbors(), equalTo(listOf(Node("b"), Node("c"), Node("k"))))
            assertThat(nodes["g"]!!.neighbors(), equalTo(listOf(Node("h"))))
            assertThat(nodes["d"]!!.neighbors(), equalTo(emptyList()))
        }

        fun Graph<String, *>.assertPropertiesOfDirectedGraphExample() {
            assertThat(nodes.size, equalTo(5))
            assertThat(edges.size, equalTo(5))
            assertThat(toString(), equalTo("[s>r, s>u, u>r, u>s, v>u, t]"))

            assertThat(nodes["s"]!!.neighbors(), equalTo(listOf(Node("r"), Node("u"))))
            assertThat(nodes["v"]!!.neighbors(), equalTo(listOf(Node("u"))))
            assertThat(nodes["r"]!!.neighbors(), equalTo(emptyList()))
        }

        fun Graph<String, Int>.assertPropertiesOfUndirectedLabeledGraphExample() {
            assertThat(nodes.size, equalTo(4))
            assertThat(edges.size, equalTo(3))
            assertThat(toString(), equalTo("[m-q/7, p-m/5, p-q/9, k]"))

            assertThat(nodes["p"]!!.neighbors(), equalTo(listOf(Node("m"), Node("q"))))
            assertThat(nodes["m"]!!.neighbors(), equalTo(listOf(Node("q"), Node("p"))))
            assertThat(nodes["k"]!!.neighbors(), equalTo(emptyList()))
        }

        fun Graph<String, Int>.assertPropertiesOfDirectedLabeledGraphExample() {
            assertThat(nodes.size, equalTo(4))
            assertThat(edges.size, equalTo(3))
            assertThat(toString(), equalTo("[m>q/7, p>m/5, p>q/9, k]"))

            assertThat(nodes["p"]!!.neighbors(), equalTo(listOf(Node("m"), Node("q"))))
            assertThat(nodes["m"]!!.neighbors(), equalTo(listOf(Node("q"))))
            assertThat(nodes["k"]!!.neighbors(), equalTo(emptyList()))
        }
    }
}