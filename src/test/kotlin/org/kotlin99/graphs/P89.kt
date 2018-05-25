package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

/**
 * Based on https://en.wikipedia.org/wiki/Bipartite_graph#Testing_bipartiteness
 */
fun <V> Graph<V, *>.isBipartite() =
    components().all { graph ->
        graph.colorNodes().all { it.second == 1 || it.second == 2 }
    }

class P89Test {
    @Test fun `linear graph are bipartite`() {
        assertThat("[a]".toGraph().isBipartite(), equalTo(true))
        assertThat("[a-b]".toGraph().isBipartite(), equalTo(true))
        assertThat("[a-b, b-c]".toGraph().isBipartite(), equalTo(true))
        assertThat("[a-b, b-c, c-d]".toGraph().isBipartite(), equalTo(true))
        assertThat("[a-b, b-c, c-d, d-e]".toGraph().isBipartite(), equalTo(true))
    }

    @Test fun `graphs with cycles are bipartite if number of edges in cycle is even`() {
        assertThat("[a-b, b-c, c-a]".toGraph().isBipartite(), equalTo(false))

        assertThat("[a-b, b-c, c-d, d-a]".toGraph().isBipartite(), equalTo(true))
        assertThat("[a-b, b-c, c-d, d-a, c-a]".toGraph().isBipartite(), equalTo(false))

        assertThat("[a-b, b-c, c-d, d-e, e-a]".toGraph().isBipartite(), equalTo(false))
    }

    @Test fun `bipartiteness includes graph components`() {
        assertThat("[a-b, b-c, d-e]".toGraph().isBipartite(), equalTo(true))
        assertThat("[a-b, b-c, d, e-f, f-g, g-e, h]".toGraph().isBipartite(), equalTo(false))
    }

    @Test fun `directed graph`() {
        assertThat("[a>b, c>a, d>b]".toGraph().isBipartite(), equalTo(true))
    }
}