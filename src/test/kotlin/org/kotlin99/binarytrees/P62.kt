package org.kotlin99.binarytrees

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Test
import org.kotlin99.binarytrees.Tree.End
import org.kotlin99.binarytrees.Tree.Node
import org.kotlin99.common.containsAll


fun <T> Tree<T>.valuesAtLevel(n: Int): List<T> =
    when (this) {
        End        -> emptyList()
        is Node<T> ->
            if (n == 1) listOf(this.value)
            else left.valuesAtLevel(n - 1) + right.valuesAtLevel(n - 1)
    }


class P62Test {
    @Test fun `collect node values at particular level`() {
        val tree = Node("a", Node("b"), Node("c", Node("d"), Node("e")))
        assertThat(tree.valuesAtLevel(0), containsAll(emptyList()))
        assertThat(tree.valuesAtLevel(1), containsAll("a"))
        assertThat(tree.valuesAtLevel(2), containsAll("b", "c"))
        assertThat(tree.valuesAtLevel(3), containsAll("d", "e"))
        assertThat(tree.valuesAtLevel(4), containsAll(emptyList()))
    }
}