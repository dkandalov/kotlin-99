package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.tail


fun <T> Graph<T, *>.nodesByDepthFrom(nodeValue: T): List<T> {
    fun nodesByDepth(nodeValues: List<T>, visited: List<T>): List<T> =
        if (nodeValues.isEmpty()) emptyList()
        else {
            val head = nodeValues.first()
            val tail = nodeValues.tail()
            if (visited.contains(head)) nodesByDepth(tail, visited)
            else listOf(head) + nodesByDepth(neighbourValues(head) + tail, visited + head)
        }
    return nodesByDepth(listOf(nodeValue), emptyList())
}

fun <T> Graph<T, *>.nodesByBreadthFrom(nodeValue: T): List<T> {
    fun nodesByBreadth(nodeValues: List<T>, visited: List<T>): List<T> =
        if (nodeValues.isEmpty()) emptyList()
        else {
            val head = nodeValues.first()
            val tail = nodeValues.tail()
            if (visited.contains(head)) nodesByBreadth(tail, visited)
            else listOf(head) + nodesByBreadth(tail + neighbourValues(head), visited + head)
        }
    return nodesByBreadth(listOf(nodeValue), emptyList())
}

private fun <T> Graph<T, *>.neighbourValues(head: T) = nodes[head]!!.neighbors().map { it.value }


class P87Test {
    @Test fun `basic examples`() {
        assertThat("[a]".toGraph().nodesByDepthFrom("a"), equalTo(listOf("a")))
        assertThat("[a-b]".toGraph().nodesByDepthFrom("a"), equalTo(listOf("a", "b")))

        assertThat("[a]".toGraph().nodesByBreadthFrom("a"), equalTo(listOf("a")))
        assertThat("[a-b]".toGraph().nodesByBreadthFrom("a"), equalTo(listOf("a", "b")))
    }

    @Test fun `nodes connected in one line`() {
        "[a-b, b-c]".toGraph().let {
            assertThat(it.nodesByDepthFrom("a"), equalTo(nodeList("abc")))
            assertThat(it.nodesByDepthFrom("b"), equalTo(nodeList("bac")))
            assertThat(it.nodesByDepthFrom("c"), equalTo(nodeList("cba")))
        }
        assertThat("[a-b, b-c, c-d, d-e]".toGraph().nodesByDepthFrom("c"), equalTo(nodeList("cbade")))

        "[a-b, b-c]".toGraph().let {
            assertThat(it.nodesByBreadthFrom("a"), equalTo(nodeList("abc")))
            assertThat(it.nodesByBreadthFrom("b"), equalTo(nodeList("bac")))
            assertThat(it.nodesByBreadthFrom("c"), equalTo(nodeList("cba")))
        }
        assertThat("[a-b, b-c, c-d, d-e]".toGraph().nodesByBreadthFrom("c"), equalTo(nodeList("cbdae")))
    }

    @Test fun `graph with loop and disconnected node`() {
        "[a-b, b-c, b-e, a-c, a-d, f]".toGraph().let {
            assertThat(it.nodesByDepthFrom("c"), equalTo(nodeList("cbade")))
            assertThat(it.nodesByDepthFrom("d"), equalTo(nodeList("dabce")))
        }

        "[a-b, b-c, b-e, a-c, a-d, f]".toGraph().let {
            assertThat(it.nodesByBreadthFrom("c"), equalTo(nodeList("cbaed")))
            assertThat(it.nodesByBreadthFrom("d"), equalTo(nodeList("dabce")))
        }
    }

    @Test fun `directed graph traversal`() {
        "[a>b, b>c]".toGraph().let {
            assertThat(it.nodesByBreadthFrom("a"), equalTo(nodeList("abc")))
            assertThat(it.nodesByBreadthFrom("b"), equalTo(nodeList("bc")))
            assertThat(it.nodesByBreadthFrom("c"), equalTo(nodeList("c")))
        }
    }

    private fun nodeList(s: String) = s.toList().map(Char::toString)
}