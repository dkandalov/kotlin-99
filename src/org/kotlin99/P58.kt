package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Test
import org.kotlin99.P55Test.Companion.nodeList
import org.kotlin99.Tree.End
import org.kotlin99.Tree.Node

fun <T> symmetricBalancedTrees(treeSize: Int, value: T): List<Tree<T>> =
        balancedTrees(treeSize, value).filter { it.isSymmetric() }

class P58Test {
    @Test fun `construct all balanced and symmetric trees`() {
        assertThat(symmetricBalancedTrees(5, "x"), hasSameElementsAs(nodeList(
                Node("x",
                    Node("x", End, Node("x")),
                    Node("x", Node("x"), End)),
                Node("x",
                    Node("x", Node("x"), End),
                    Node("x", End, Node("x")))
        )))
    }
}