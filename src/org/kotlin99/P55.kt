package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Test

fun <T> balancedTrees(size: Int, value: T): List<Tree<T>> {
    if (size == 0) return listOf(End)
    else if (size == 1) return listOf(Node(value))
    else {
        val n = (size - 1) / 2
        val subTrees1 = balancedTrees(n, value)
        val subTrees2 = balancedTrees((size - 1) - n, value)
        return subTrees1.flatMap { tree1 ->
            subTrees2.flatMap { tree2 ->
                listOf(Node(value, tree1, tree2), Node(value, tree2, tree1))
            }
        }.distinct()
    }
}

class P55Test {
    @Test fun `construct all balanced trees`() {
        assertThat(balancedTrees(1, "x"), hasSameElementsAs(nodeList(Node("x"))))
        assertThat(balancedTrees(2, "x"), hasSameElementsAs(nodeList(
                Node("x", End, Node("x")),
                Node("x", Node("x"), End)
        )))
        assertThat(balancedTrees(3, "x"), hasSameElementsAs(nodeList(
                Node("x", Node("x"), Node("x"))
        )))
        assertThat(balancedTrees(4, "x"), hasSameElementsAs(nodeList(
                Node("x", Node("x"), Node("x", End, Node("x"))),
                Node("x", Node("x", End, Node("x")), Node("x")),
                Node("x", Node("x"), Node("x", Node("x"), End)),
                Node("x", Node("x", Node("x"), End), Node("x"))
        )))
    }

    private fun nodeList(vararg nodes: Node<String>): Iterable<Tree<String>> = nodes.toList()
}