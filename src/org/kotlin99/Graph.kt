package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.Graph.Node
import org.kotlin99.Graph.Term
import java.util.*
import java.util.regex.Pattern


class Graph<T, U> {
    val nodes: MutableMap<T, Node<T>> = HashMap()
    val edges: MutableList<Edge<T, U>> = ArrayList()

    fun addNode(value: T): Node<T> {
        val node = Node(value)
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
            nodes[n1]!!.adj.add(edge)
            nodes[n2]!!.adj.add(edge)
        }
    }

    fun addDirectedEdge(source: T, dest: T, label: U?) {
        val edge = DirectedEdge(nodes[source]!!, nodes[dest]!!, label)
        if (!edges.contains(edge)) {
            edges.add(edge)
            nodes[source]!!.adj.add(edge)
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
        val adj: MutableList<Edge<T, *>> = ArrayList()
        fun neighbors(): List<Node<T>> = adj.map{ edge -> edge.target(this)!! }
        override fun toString() = value.toString()
    }

    interface Edge<T, U> {
        val n1: Node<T>
        val n2: Node<T>
        val label: U?
        fun target(node: Node<T>): Node<T>?
        fun equivalentTo(other: Edge<T, U>) =
                (n1 == other.n1 && n2 == other.n2) || (n1 == other.n2 && n2 == other.n1)
    }

    data class UndirectedEdge<T, U>(override val n1: Node<T>, override val n2: Node<T>, override val label: U?) : Edge<T, U> {
        override fun target(node: Node<T>) = if (n1 == node) n2 else if (n2 == node) n1 else null
        override fun toString() = n1.toString() + "-" + n2 + (if (label == null) "" else "/" + label.toString())
    }

    data class DirectedEdge<T, U>(override val n1: Node<T>, override val n2: Node<T>, override val label: U?) : Edge<T, U> {
        override fun target(node: Node<T>) = if (n1 == node) n2 else null
        override fun toString() = n1.toString() + ">" + n2 + (if (label == null) "" else "/" + label.toString())
    }

    data class Term<T, U>(val n1: T, val n2: T, val label: U? = null)

    companion object {
        private val graphTokenSeparators = Pattern.compile("[->/]")

        fun fromString(s: String): Graph<String, *> {
            if (!s.startsWith('[') || !s.endsWith(']')) {
                throw IllegalArgumentException("Expected string starting '[' and ending with ']' but it was '$s'")
            }
            val tokens = s.substring(1, s.length - 1).split(", ").map { it.split(graphTokenSeparators) }
            val nodes = tokens.flatMap{ it }.toCollection(LinkedHashSet())
            val edges = tokens.filter{ it.size == 2 }.map{ Term<String, Nothing>(it[0], it[1]) }
            if (s.contains("-")) {
                return terms(nodes, edges)
            } else {
                return directedTerms(nodes, edges)
            }
        }

        fun fromStringLabel(s: String): Graph<String, Int> {
            if (!s.startsWith('[') || !s.endsWith(']')) {
                throw IllegalArgumentException("Expected string starting '[' and ending with ']' but it was '$s'")
            }
            val tokens = s.substring(1, s.length - 1).split(", ").map { it.split(graphTokenSeparators) }
            val nodes = tokens.flatMap{ it.take(2) }.toCollection(LinkedHashSet())
            val edges = tokens.filter{ it.size == 3 }.map{ Term(it[0], it[1], it[2].toInt()) }
            if (s.contains("-")) {
                return labeledTerms(nodes, edges)
            } else {
                return labeledDirectedTerms(nodes, edges)
            }
        }

        fun <T> terms(nodes: Collection<T>, edges: List<Term<T, Nothing>>): Graph<T, *> {
            return createFromTerms(nodes, edges) { graph, n1, n2, value -> graph.addUndirectedEdge(n1, n2, value) }
        }

        fun <T> directedTerms(nodes: Collection<T>, edges: List<Term<T, Nothing>>): Graph<T, *> {
            return createFromTerms(nodes, edges) { graph, n1, n2, value -> graph.addDirectedEdge(n1, n2, value) }
        }

        fun <T, U> labeledTerms(nodes: Collection<T>, edges: List<Term<T, U>>): Graph<T, U> {
            return createFromTerms(nodes, edges) { graph, n1, n2, value -> graph.addUndirectedEdge(n1, n2, value) }
        }

        fun <T, U> labeledDirectedTerms(nodes: Collection<T>, edges: List<Term<T, U>>): Graph<T, U> {
            return createFromTerms(nodes, edges) { graph, n1, n2, value -> graph.addDirectedEdge(n1, n2, value) }
        }

        fun <T> adjacent(nodesWithNeighbors: List<Pair<T, List<T>>>): Graph<T, *> {
            return fromAdjacencyList(nodesWithNeighbors.map{ Pair(it.first, it.second.toPairs())}) { graph, n1, n2, value ->
                graph.addUndirectedEdge(n1, n2, value)
            }
        }

        fun <T> directedAdjacent(nodesWithNeighbors: List<Pair<T, List<T>>>): Graph<T, *> {
            return fromAdjacencyList(nodesWithNeighbors.map{ Pair(it.first, it.second.toPairs())}) { graph, n1, n2, value -> graph.addDirectedEdge(n1, n2, value) }
        }

        fun <T, U> labeledAdjacent(nodesWithNeighbors: List<Pair<T, List<Pair<T, U>>>>): Graph<T, U> {
            return fromAdjacencyList(nodesWithNeighbors) { graph, n1, n2, value ->
                graph.addUndirectedEdge(n1, n2, value)
            }
        }

        fun <T, U> labeledDirectedAdjacent(nodesWithNeighbors: List<Pair<T, List<Pair<T, U>>>>): Graph<T, U> {
            return fromAdjacencyList(nodesWithNeighbors) { graph, n1, n2, value ->
                graph.addDirectedEdge(n1, n2, value)
            }
        }

        private fun <T, U> createFromTerms(nodes: Collection<T>, edges: List<Term<T, U>>,
                                        addFunction: (Graph<T, U>, T, T, U?) -> Unit): Graph<T, U> {
            val graph = Graph<T, U>()
            nodes.forEach { graph.addNode(it) }
            edges.forEach { addFunction(graph, it.n1, it.n2, it.label) }
            return graph
        }

        private fun <T, U> fromAdjacencyList(nodesWithNeighbors: List<Pair<T, List<Pair<T, U>>>>,
                                             addFunction: (Graph<T, U>, T, T, U) -> Unit): Graph<T, U> {
            val graph = Graph<T, U>()
            nodesWithNeighbors.forEach { graph.addNode(it.first) }
            nodesWithNeighbors.forEach{
                val (nodeValue, adjacentNodeValues) = it
                adjacentNodeValues.forEach{ addFunction(graph, nodeValue, it.first, it.second) }
            }
            return graph
        }

        private fun <T> List<T>.toPairs() = map{ Pair(it, null) }
    }
}


class GraphTest {
    @Test fun `create simple graph from list of nodes and edges`() {
        val graph = Graph.terms(nodes = listOf("a", "b", "c"), edges = listOf(Term("a", "b")))

        assertThat(graph.nodes.size, equalTo(3))
        assertThat(graph.edges.size, equalTo(1))
        assertThat(graph.toString(), equalTo("[a-b, c]"))

        assertThat(graph.nodes["a"]!!.neighbors(), equalTo(listOf(Node("b"))))
        assertThat(graph.nodes["b"]!!.neighbors(), equalTo(listOf(Node("a"))))
        assertThat(graph.nodes["c"]!!.neighbors(), equalTo(emptyList()))
    }

    @Test fun `create graph from list of nodes and edges`() {
        Graph.terms(nodes = listOf("b", "c", "d", "f", "g", "h", "k"),
                    edges = listOf(Term("b", "c"), Term("b", "f"), Term("c", "f"), Term("f", "k"), Term("g", "h")))
             .assertPropertiesOfUndirectedGraphExample()
    }

    @Test fun `create graph from adjacency list`() {
        Graph.adjacent(listOf(
                Pair("b", listOf("c", "f")),
                Pair("c", listOf("b", "f")),
                Pair("d", emptyList()),
                Pair("f", listOf("b", "c", "k")),
                Pair("g", listOf("h")),
                Pair("h", listOf("g")),
                Pair("k", listOf("f"))))
             .assertPropertiesOfUndirectedGraphExample()
    }

    @Test fun `create directed graph from list of nodes and edges`() {
        Graph.directedTerms(listOf("r", "s", "t", "u", "v"),
                            listOf(Term("s", "r"), Term("s", "u"), Term("u", "r"), Term("u", "s"), Term("v", "u")))
             .assertPropertiesOfDirectedGraphExample()
    }

    @Test fun `create directed graph from adjacency list`() {
        Graph.directedAdjacent(listOf(
                Pair("r", emptyList()),
                Pair("s", listOf("r", "u")),
                Pair("t", emptyList()),
                Pair("u", listOf("r", "s")),
                Pair("v", listOf("u"))))
             .assertPropertiesOfDirectedGraphExample()
    }

    @Test fun `create labeled undirected graph`() {
        Graph.labeledTerms(
                listOf("k", "m", "p", "q"),
                listOf(Term("m", "q", 7), Term("p", "m", 5), Term("p", "q", 9)))
             .assertPropertiesOfUndirectedLabeledGraphExample()
    }

    @Test fun `create labeled directed graph`() {
        Graph.labeledDirectedTerms(listOf("k", "m", "p", "q"), listOf(Term("m", "q", 7), Term("p", "m", 5), Term("p", "q", 9)))
             .assertPropertiesOfDirectedLabeledGraphExample()
    }

    @Test fun `create labeled undirected graph from adjacency list`() {
        Graph.labeledAdjacent(listOf(
                Pair("k", emptyList()),
                Pair("m", listOf(Pair("q", 7))),
                Pair("p", listOf(Pair("m", 5), Pair("q", 9))),
                Pair("q", emptyList())))
             .assertPropertiesOfUndirectedLabeledGraphExample()
    }

    @Test fun `create labeled directed graph from adjacency list`() {
        Graph.labeledDirectedAdjacent(listOf(
                Pair("k", emptyList()),
                Pair("m", listOf(Pair("q", 7))),
                Pair("p", listOf(Pair("m", 5), Pair("q", 9))),
                Pair("q", emptyList())))
             .assertPropertiesOfDirectedLabeledGraphExample()
    }

    @Test fun `graph conversion from and to string`() {
        Graph.fromString("[b-c, b-f, c-f, f-k, g-h, d]").assertPropertiesOfUndirectedGraphExample()
        Graph.fromString("[s>r, s>u, u>r, u>s, v>u, t]").assertPropertiesOfDirectedGraphExample()

        Graph.fromStringLabel("[m-q/7, p-m/5, p-q/9, k]").assertPropertiesOfUndirectedLabeledGraphExample()
        Graph.fromStringLabel("[m>q/7, p>m/5, p>q/9, k]").assertPropertiesOfDirectedLabeledGraphExample()
    }

    private fun Graph<String, *>.assertPropertiesOfUndirectedGraphExample() {
        assertThat(nodes.size, equalTo(7))
        assertThat(edges.size, equalTo(5))
        assertThat(toString(), equalTo("[b-c, b-f, c-f, f-k, g-h, d]"))

        assertThat(nodes["f"]!!.neighbors(), equalTo(listOf(Node("b"), Node("c"), Node("k"))))
        assertThat(nodes["g"]!!.neighbors(), equalTo(listOf(Node("h"))))
        assertThat(nodes["d"]!!.neighbors(), equalTo(emptyList()))
    }

    private fun Graph<String, *>.assertPropertiesOfDirectedGraphExample() {
        assertThat(nodes.size, equalTo(5))
        assertThat(edges.size, equalTo(5))
        assertThat(toString(), equalTo("[s>r, s>u, u>r, u>s, v>u, t]"))

        assertThat(nodes["s"]!!.neighbors(), equalTo(listOf(Node("r"), Node("u"))))
        assertThat(nodes["v"]!!.neighbors(), equalTo(listOf(Node("u"))))
        assertThat(nodes["r"]!!.neighbors(), equalTo(emptyList()))
    }

    private fun Graph<String, Int>.assertPropertiesOfUndirectedLabeledGraphExample() {
        assertThat(nodes.size, equalTo(4))
        assertThat(edges.size, equalTo(3))
        assertThat(toString(), equalTo("[m-q/7, p-m/5, p-q/9, k]"))

        assertThat(nodes["p"]!!.neighbors(), equalTo(listOf(Node("m"), Node("q"))))
        assertThat(nodes["m"]!!.neighbors(), equalTo(listOf(Node("q"), Node("p"))))
        assertThat(nodes["k"]!!.neighbors(), equalTo(emptyList()))
    }

    private fun Graph<String, Int>.assertPropertiesOfDirectedLabeledGraphExample() {
        assertThat(nodes.size, equalTo(4))
        assertThat(edges.size, equalTo(3))
        assertThat(toString(), equalTo("[m>q/7, p>m/5, p>q/9, k]"))

        assertThat(nodes["p"]!!.neighbors(), equalTo(listOf(Node("m"), Node("q"))))
        assertThat(nodes["m"]!!.neighbors(), equalTo(listOf(Node("q"))))
        assertThat(nodes["k"]!!.neighbors(), equalTo(emptyList()))
    }
}