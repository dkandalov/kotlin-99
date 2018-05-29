package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.containsAll
import java.util.*

fun <V> Graph.Node<V, *>.degree(): Int = this.edges.size

fun <V> Graph<V, *>.colorNodes(): List<Pair<V, Int>> {
    val colorByNode = LinkedHashMap<V, Int>()
    val nodeList = nodes.values.sortedBy { -it.degree() }.toMutableList()
    var color = 1

    while (nodeList.isNotEmpty()) {
        nodeList.forEach { node ->
            val hasSameColorNeighbour = node.neighbors().any { colorByNode[it.value] == color }
            if (!hasSameColorNeighbour) {
                colorByNode[node.value] = color
            }
        }
        nodeList.removeAll { colorByNode.containsKey(it.value) }
        color += 1
    }

    return colorByNode.entries.map { Pair(it.key, it.value) }
}


class P86Test {
    @Test fun `node degree in undirected graph`() {
        assertThat("[a]".toGraph().nodes["a"]!!.degree(), equalTo(0))

        "[a-b]".toGraph().let {
            assertThat(it.nodes["a"]!!.degree(), equalTo(1))
            assertThat(it.nodes["b"]!!.degree(), equalTo(1))
        }

        "[a-b, a-c]".toGraph().let {
            assertThat(it.nodes["a"]!!.degree(), equalTo(2))
            assertThat(it.nodes["b"]!!.degree(), equalTo(1))
            assertThat(it.nodes["c"]!!.degree(), equalTo(1))
        }

        "[a-b, b-c, a-c, a-d]".toGraph().let {
            assertThat(it.nodes["a"]!!.degree(), equalTo(3))
            assertThat(it.nodes["b"]!!.degree(), equalTo(2))
            assertThat(it.nodes["c"]!!.degree(), equalTo(2))
            assertThat(it.nodes["d"]!!.degree(), equalTo(1))
        }
    }

    @Test fun `color nodes of undirected graph (so that adjacent nodes have different color)`() {
        assertThat("[a]".toGraph().colorNodes(), containsAll(listOf(Pair("a", 1))))
        assertThat("[a-b]".toGraph().colorNodes(), containsAll(listOf(Pair("a", 1), Pair("b", 2))))
        assertThat("[a-b, a-c]".toGraph().colorNodes(), containsAll(listOf(Pair("a", 1), Pair("b", 2), Pair("c", 2))))
        assertThat("[a-b, b-c, c-d]".toGraph().colorNodes(), containsAll(listOf(Pair("a", 2), Pair("b", 1), Pair("c", 2), Pair("d", 1))))

        assertThat("[a-b, a-c, b-c]".toGraph().colorNodes(), containsAll(listOf(
            Pair("a", 1),
            Pair("b", 2),
            Pair("c", 3)
        )))

        assertThat("[a-b, b-c, a-c, a-d]".toGraph().colorNodes(), containsAll(listOf(
            Pair("a", 1),
            Pair("b", 2),
            Pair("c", 3),
            Pair("d", 2)
        )))
    }
}
