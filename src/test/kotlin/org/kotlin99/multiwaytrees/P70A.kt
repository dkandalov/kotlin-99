package org.kotlin99.multiwaytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun MTree<*>.nodeCount(): Int = 1 + children.sumBy { it.nodeCount() }

class P70ATest {
    @Test fun `count nodes`() {
        assertThat(MTree("a").nodeCount(), equalTo(1))
        assertThat(MTree("a", MTree("f")).nodeCount(), equalTo(2))
        assertThat(
            MTree("a",
                  MTree("f", MTree("g")),
                  MTree("c"),
                  MTree("b", MTree("d"), MTree("e"))
            ).nodeCount(), equalTo(7))
    }
}