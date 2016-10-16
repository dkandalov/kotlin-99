package org.kotlin99.graphs

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.describe
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.hasSameElementsAs
import org.kotlin99.graphs.Graph.AdjacencyList
import org.kotlin99.graphs.Graph.AdjacencyList.Entry
import org.kotlin99.graphs.Graph.AdjacencyList.Entry.Companion.links
import org.kotlin99.graphs.Graph.AdjacencyList.Link
import org.kotlin99.graphs.Graph.TermForm
import org.kotlin99.graphs.Graph.TermForm.Term
import org.kotlin99.graphs.GraphTest.Companion.assertPropertiesOfDirectedGraphExample
import org.kotlin99.graphs.GraphTest.Companion.assertPropertiesOfDirectedLabeledGraphExample
import org.kotlin99.graphs.GraphTest.Companion.assertPropertiesOfUndirectedGraphExample
import org.kotlin99.graphs.GraphTest.Companion.assertPropertiesOfUndirectedLabeledGraphExample
import java.util.*
import java.util.regex.Pattern

private val graphTokenSeparators = Pattern.compile("[->/]")

fun String.toGraph(): Graph<String, Nothing> {
    if (!startsWith('[') || !endsWith(']')) {
        throw IllegalArgumentException("Expected string starting '[' and ending with ']' but it was '$this'")
    }
    val tokens = substring(1, length - 1).split(", ").map { it.split(graphTokenSeparators) }
    val nodes = tokens.flatMap{ it }.toCollection(LinkedHashSet())
    val edges = tokens.filter{ it.size == 2 }.map{ Term<String, Nothing>(it[0], it[1]) }
    if (contains("-")) {
        return Graph.terms(TermForm(nodes, edges))
    } else {
        return Graph.directedTerms(TermForm(nodes, edges))
    }
}

fun String.toLabeledGraph(): Graph<String, Int> {
    if (!startsWith('[') || !endsWith(']')) {
        throw IllegalArgumentException("Expected string starting '[' and ending with ']' but it was '$")
    }
    val tokens = substring(1, length - 1).split(", ").map { it.split(graphTokenSeparators) }
    val nodes = tokens.flatMap{ it.take(2) }.toCollection(LinkedHashSet())
    val edges = tokens.filter{ it.size == 3 }.map{ Term(it[0], it[1], it[2].toInt()) }
    if (contains("-")) {
        return Graph.labeledTerms(TermForm(nodes, edges))
    } else {
        return Graph.labeledDirectedTerms(TermForm(nodes, edges))
    }
}

fun <T, U> Graph<T, U>.toTermForm(): TermForm<T, U> {
    val nodeValues = nodes.values.map { it.value }
    val terms = edges.map { Term(it.n1.value, it.n2.value, it.label) }
    return TermForm(nodeValues, terms)
}

fun <T, U> Graph<T, U>.toAdjacencyList(): AdjacencyList<T, U> {
    val entries = nodes.values.map { node ->
        val links = node.edges.map { Link(it.target(node)!!.value, it.label) }
        Entry(node = node.value, links = links)
    }
    return AdjacencyList(entries)
}

class P80Test {
    @Test fun `graph conversion from and to string`() {
        "[b-c, b-f, c-f, f-k, g-h, d]".toGraph().assertPropertiesOfUndirectedGraphExample()
        "[s>r, s>u, u>r, u>s, v>u, t]".toGraph().assertPropertiesOfDirectedGraphExample()

        "[m-q/7, p-m/5, p-q/9, k]".toLabeledGraph().assertPropertiesOfUndirectedLabeledGraphExample()
        "[m>q/7, p>m/5, p>q/9, k]".toLabeledGraph().assertPropertiesOfDirectedLabeledGraphExample()
    }

    @Test fun `graph equality and equivalence`() {
        assertThat("[a]".toGraph(), equalTo("[a]".toGraph()))
        assertThat("[a]".toGraph(), !equalTo("[b]".toGraph()))

        assertThat("[a-b]".toGraph(), equalTo("[a-b]".toGraph()))
        assertThat("[a-b]".toGraph(), !equalTo("[b-a]".toGraph()))

        assertThat("[a-b]".toGraph(), equivalentTo("[a-b]".toGraph()))
        assertThat("[a-b]".toGraph(), equivalentTo("[b-a]".toGraph()))
        assertThat("[a-b, b-c]".toGraph(), equivalentTo("[c-b, b-a]".toGraph()))
    }

    @Test fun `convert graph to term form`() {
        assertThat("[b-c, b-f, c-f, f-k, g-h, d]".toGraph().toTermForm(), equalTo(TermForm(
                listOf("f", "g", "d", "b", "c", "k", "h"),
                listOf(Term("b", "c"), Term("b", "f"), Term("c", "f"), Term("f", "k"), Term("g", "h"))
        )))
    }

    @Test fun `convert graph to adjacency list`() {
        assertThat("[a-b, a-c]".toGraph().toAdjacencyList(), equalTo(AdjacencyList(
                Entry("a", links("b", "c")),
                Entry("b", links("a")),
                Entry("c", links("a"))
        )))
        assertThat("[b-c, b-f, c-f, f-k, g-h, d]".toGraph().toAdjacencyList(), equalTo(AdjacencyList(
                Entry("b", links("c", "f")),
                Entry("c", links("b", "f")),
                Entry("d"),
                Entry("f", links("b", "c", "k")),
                Entry("g", links("h")),
                Entry("h", links("g")),
                Entry("k", links("f"))
        )))
        assertThat("[m-q/7, p-m/5, p-q/9, k]".toLabeledGraph().toAdjacencyList(), equalTo(AdjacencyList(
                Entry("q", listOf(Link("m", 7), Link("p", 9))),
                Entry("p", listOf(Link("m", 5), Link("q", 9))),
                Entry("m", listOf(Link("q", 7), Link("p", 5))),
                Entry("k")
        )))
    }

    fun <T, U> equivalentTo(expected: Graph<T, U>): Matcher<Graph<T, U>> =
        object : Matcher<Graph<T, U>> {
            override fun invoke(actual: Graph<T, U>): MatchResult =
                    if (actual.equivalentTo(expected)) MatchResult.Match else MatchResult.Mismatch("was ${describe(actual)}")
            override val description: String get() = "is equivalent to ${describe(expected)}"
            override val negatedDescription: String get() = "is not equivalent to ${describe(expected)}"
        }

    private fun <T, U> equalTo(expected: AdjacencyList<T, U>) : Matcher<AdjacencyList<T, U>> {
        return object : Matcher.Primitive<AdjacencyList<T, U>>() {
            override fun invoke(actual: AdjacencyList<T, U>) = hasSameElementsAs(expected.entries).invoke(actual.entries)
            override val description: String get() = "has the same elements as ${describe(expected)}"
            override val negatedDescription : String get() = "element are not the same as in ${describe(expected)}"
        }
    }
}