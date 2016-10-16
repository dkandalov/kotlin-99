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


class Graph<T, U> {
    val nodes: MutableMap<T, Node<T, U>> = HashMap()
    val edges: MutableList<Edge<T, U>> = ArrayList()

    fun addNode(value: T): Node<T, U> {
        val node = Node<T, U>(value)
        nodes.put(value, node)
        return node
    }

    fun addUndirectedEdge(n1: T, n2: T, label: U?) {
        if (!nodes.contains(n1) || !nodes.contains(n2)) {
            throw IllegalStateException("Expected '$n1' and '$n2' nodes to exist in graph")
        }
        val edge = UndirectedEdge(nodes[n1]!!, nodes[n2]!!, label)
        if (edges.all{ !it.equivalentTo(edge) }) {
            edges.add(edge)
            nodes[n1]!!.edges.add(edge)
            nodes[n2]!!.edges.add(edge)
        }
    }

    fun addDirectedEdge(source: T, dest: T, label: U?) {
        val edge = DirectedEdge(nodes[source]!!, nodes[dest]!!, label)
        if (!edges.contains(edge)) {
            edges.add(edge)
            nodes[source]!!.edges.add(edge)
        }
    }

    override fun toString(): String {
        val standaloneNodes = nodes.values.filter{ node -> edges.all { it.n1 != node && it.n2 != node } }
        val s = (edges.map{ it.toString() } + standaloneNodes.map{ it.toString() }).joinToString()
        return "[$s]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other || other?.javaClass != javaClass) return false
        other as Graph<*, *>
        return nodes == other.nodes && edges == other.edges
    }

    override fun hashCode() = 31 * nodes.hashCode() + edges.hashCode()

    fun equivalentTo(other: Graph<T, U>): Boolean {
        return nodes == other.nodes && edges.all { edge -> other.edges.any{ it.equivalentTo(edge) } }
    }


    data class Node<T, U>(val value: T) {
        val edges: MutableList<Edge<T, U>> = ArrayList()
        fun neighbors(): List<Node<T, U>> = edges.map{ edge -> edge.target(this)!! }
        override fun toString() = value.toString()
    }

    interface Edge<T, U> {
        val n1: Node<T, U>
        val n2: Node<T, U>
        val label: U?
        fun target(node: Node<T, U>): Node<T, U>?
        fun equivalentTo(other: Edge<T, U>) =
                (n1 == other.n1 && n2 == other.n2) || (n1 == other.n2 && n2 == other.n1)
    }

    data class UndirectedEdge<T, U>(override val n1: Node<T, U>, override val n2: Node<T, U>, override val label: U?) : Edge<T, U> {
        override fun target(node: Node<T, U>) = if (n1 == node) n2 else if (n2 == node) n1 else null
        override fun toString() = n1.toString() + "-" + n2 + (if (label == null) "" else "/" + label.toString())
    }

    data class DirectedEdge<T, U>(override val n1: Node<T, U>, override val n2: Node<T, U>, override val label: U?) : Edge<T, U> {
        override fun target(node: Node<T, U>) = if (n1 == node) n2 else null
        override fun toString() = n1.toString() + ">" + n2 + (if (label == null) "" else "/" + label.toString())
    }


    data class TermForm<out T, out U>(val nodes: Collection<T>, val edges: List<Term<T, U>>) {
        data class Term<out T, out U>(val n1: T, val n2: T, val label: U? = null) {
            override fun toString() = if (label == null) "Term($n1, $n2)" else "Term($n1, $n2, $label)"
        }
    }

    data class AdjacencyList<T, out U>(val entries: List<Entry<T, U>>) {
        constructor(vararg entries: Entry<T, U>): this(entries.toList())
        override fun toString() = "AdjacencyList(${entries.joinToString()})"

        data class Entry<out T, out U>(val node: T, val links: List<Link<T, U>> = emptyList<Nothing>()) {
            constructor(node: T, vararg links: Link<T, U>): this(node, links.toList())
            override fun toString() = "Entry($node, links[${links.joinToString()}])"

            companion object {
                fun <T> links(vararg linkValues: T): List<Link<T, Nothing>> = linkValues.map { Link(it, null) }
            }
        }

        data class Link<out T, out U>(val node: T, val label: U? = null) {
            override fun toString() = if (label == null) "$node" else "$node/$label"
        }
    }

    companion object {
        fun <T> terms(termForm: TermForm<T, Nothing>): Graph<T, Nothing> {
            return createFromTerms(termForm) { graph, n1, n2, value -> graph.addUndirectedEdge(n1, n2, value) }
        }

        fun <T> directedTerms(termForm: TermForm<T, Nothing>): Graph<T, Nothing> {
            return createFromTerms(termForm) { graph, n1, n2, value -> graph.addDirectedEdge(n1, n2, value) }
        }

        fun <T, U> labeledTerms(termForm: TermForm<T, U>): Graph<T, U> {
            return createFromTerms(termForm) { graph, n1, n2, value -> graph.addUndirectedEdge(n1, n2, value) }
        }

        fun <T, U> labeledDirectedTerms(termForm: TermForm<T, U>): Graph<T, U> {
            return createFromTerms(termForm) { graph, n1, n2, value -> graph.addDirectedEdge(n1, n2, value) }
        }

        fun <T> adjacent(adjacencyList: AdjacencyList<T, Nothing>): Graph<T, *> {
            return fromAdjacencyList(adjacencyList) { graph, n1, n2, value ->
                graph.addUndirectedEdge(n1, n2, value)
            }
        }

        fun <T> directedAdjacent(adjacencyList: AdjacencyList<T, Nothing>): Graph<T, *> {
            return fromAdjacencyList(adjacencyList) { graph, n1, n2, value -> graph.addDirectedEdge(n1, n2, value) }
        }

        fun <T, U> labeledAdjacent(adjacencyList: AdjacencyList<T, U>): Graph<T, U> {
            return fromAdjacencyList(adjacencyList) { graph, n1, n2, value ->
                graph.addUndirectedEdge(n1, n2, value)
            }
        }

        fun <T, U> labeledDirectedAdjacent(adjacencyList: AdjacencyList<T, U>): Graph<T, U> {
            return fromAdjacencyList(adjacencyList) { graph, n1, n2, value ->
                graph.addDirectedEdge(n1, n2, value)
            }
        }

        private fun <T, U> createFromTerms(termForm: TermForm<T, U>, addFunction: (Graph<T, U>, T, T, U?) -> Unit): Graph<T, U> {
            val graph = Graph<T, U>()
            termForm.nodes.forEach { graph.addNode(it) }
            termForm.edges.forEach { addFunction(graph, it.n1, it.n2, it.label) }
            return graph
        }

        private fun <T, U> fromAdjacencyList(adjacencyList: AdjacencyList<T, U>,
                                             addFunction: (Graph<T, U>, T, T, U?) -> Unit): Graph<T, U> {
            val graph = Graph<T, U>()
            adjacencyList.entries.forEach { graph.addNode(it.node) }
            adjacencyList.entries.forEach{
                val (node, links) = it
                links.forEach{ addFunction(graph, node, it.node, it.label) }
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
                edges = listOf(Term("b", "c"), Term("b", "f"), Term("c", "f"), Term("f", "k"), Term("g", "h"))))
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
                Entry("k", links("f"))))
        graph.assertPropertiesOfUndirectedGraphExample()
    }

    @Test fun `create directed graph from list of nodes and edges`() {
        val graph = Graph.directedTerms(TermForm(
                            listOf("r", "s", "t", "u", "v"),
                            listOf(Term("s", "r"), Term("s", "u"), Term("u", "r"), Term("u", "s"), Term("v", "u"))))
        graph.assertPropertiesOfDirectedGraphExample()
    }

    @Test fun `create directed graph from adjacency list`() {
        val graph = Graph.directedAdjacent(AdjacencyList(
                Entry("r"),
                Entry("s", links("r", "u")),
                Entry("t"),
                Entry("u", links("r", "s")),
                Entry("v", links("u"))))
        graph.assertPropertiesOfDirectedGraphExample()
    }

    @Test fun `create labeled undirected graph`() {
        val graph = Graph.labeledTerms(TermForm(
                listOf("k", "m", "p", "q"),
                listOf(Term("m", "q", 7), Term("p", "m", 5), Term("p", "q", 9))))
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
                Entry("q")))
        graph.assertPropertiesOfUndirectedLabeledGraphExample()
    }

    @Test fun `create labeled directed graph from adjacency list`() {
        val graph = Graph.labeledDirectedAdjacent(AdjacencyList(
                Entry("k"),
                Entry("m", Link("q", 7)),
                Entry("p", Link("m", 5), Link("q", 9)),
                Entry("q")))
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