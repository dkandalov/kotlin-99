package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> Graph<T, *>.findPaths(from: T, to: T, path: List<T> = emptyList()): List<List<T>> {
    if (from == to) return listOf(path + to)
    return nodes[from]!!.neighbors()
            .filter{ !path.contains(it.value) }
            .flatMap{ findPaths(it.value, to, path + from) }
}

class P81Test {
    @Test fun `find paths in undirected graphs`() {
        assertThat("[a]".toGraph().findPaths("a", "a"), equalTo(listOf(listOf("a"))))
        assertThat("[a, b]".toGraph().findPaths("a", "b"), equalTo(emptyList()))

        assertThat("[a-b]".toGraph().findPaths("a", "b"), equalTo(listOf(listOf("a", "b"))))
        assertThat("[a-b]".toGraph().findPaths("b", "a"), equalTo(listOf(listOf("b", "a"))))

        assertThat("[a-b, b-c]".toGraph().findPaths("a", "c"), equalTo(listOf(listOf("a", "b", "c"))))
        assertThat("[a-b, b-c, a-c]".toGraph().findPaths("a", "c"), equalTo(listOf(listOf("a", "b", "c"), listOf("a", "c"))))
        assertThat("[a-b, b-c, a-c]".toGraph().findPaths("c", "a"), equalTo(listOf(listOf("c", "b", "a"), listOf("c", "a"))))
    }

    @Test fun `find paths in directed graphs`() {
        assertThat("[a>b]".toGraph().findPaths("a", "b"), equalTo(listOf(listOf("a", "b"))))
        assertThat("[a>b, b>c, c>a]".toGraph().findPaths("a", "c"), equalTo(listOf(listOf("a", "b", "c"))))

        assertThat("[p>q/9, m>q/7, k, p>m/5]".toLabeledGraph().findPaths("p", "q"), equalTo(listOf(listOf("p", "q"), listOf("p", "m", "q"))))
        assertThat("[p>q/9, m>q/7, k, p>m/5]".toLabeledGraph().findPaths("p", "k"), equalTo(listOf()))
    }
}