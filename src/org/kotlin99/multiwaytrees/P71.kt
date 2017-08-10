package org.kotlin99.multiwaytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun MTree<*>.internalPathLength(): Int =
    children.sumBy { it.nodeCount() + it.internalPathLength() }


class P71Test {
    @Test fun `internal path of tree`() {
        assertThat(MTree('a').internalPathLength(), equalTo(0))
        assertThat(MTree('a', MTree('b'), MTree('c')).internalPathLength(), equalTo(2))
        assertThat(MTree('a', MTree('b'), MTree('c', MTree('d'))).internalPathLength(), equalTo(4))
        assertThat("afg^^c^bd^e^^^".convertToMTree().internalPathLength(), equalTo(9))
    }
}