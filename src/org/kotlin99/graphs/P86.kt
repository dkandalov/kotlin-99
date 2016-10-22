package org.kotlin99.graphs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> Graph.Node<T, *>.degree(): Int = this.edges.size

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

    @Test fun `node degree in directed graph`() {
        // TODO create github issue
    }
}
