package org.kotlin99.binarytrees

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Test
import org.kotlin99.binarytrees.Tree.End
import org.kotlin99.binarytrees.Tree.Node
import org.kotlin99.common.containsAll

fun <T> balancedTrees(treeSize: Int, value: T): List<Tree<T>> {
    return when (treeSize) {
        0    -> listOf(End)
        1    -> listOf(Node(value))
        else -> {
            val n = (treeSize - 1) / 2
            val subTrees1 = balancedTrees(n, value)
            val subTrees2 = balancedTrees((treeSize - 1) - n, value)
            subTrees1.flatMap { tree1 ->
                subTrees2.flatMap { tree2 ->
                    listOf(Node(value, tree1, tree2), Node(value, tree2, tree1))
                }
            }.distinct()
        }
    }
}

class P55Test {
    @Test fun `construct all balanced trees`() {
        assertThat(balancedTrees(1, "x"), containsAll(nodeList(Node("x"))))
        assertThat(balancedTrees(2, "x"), containsAll(nodeList(
            Node("x", End, Node("x")),
            Node("x", Node("x"), End)
        )))
        assertThat(balancedTrees(3, "x"), containsAll(nodeList(
            Node("x", Node("x"), Node("x"))
        )))
        assertThat(balancedTrees(4, "x"), containsAll(nodeList(
            Node("x", Node("x"), Node("x", End, Node("x"))),
            Node("x", Node("x", End, Node("x")), Node("x")),
            Node("x", Node("x"), Node("x", Node("x"), End)),
            Node("x", Node("x", Node("x"), End), Node("x"))
        )))
    }

    companion object {
        fun nodeList(vararg nodes: Node<String>): Iterable<Tree<String>> = nodes.asList()
    }
}