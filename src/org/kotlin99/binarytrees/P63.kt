package org.kotlin99.binarytrees

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Test
import org.kotlin99.binarytrees.P57Test.Companion.equalToTree
import org.kotlin99.binarytrees.Tree.End
import org.kotlin99.binarytrees.Tree.Node

fun <T> completeBinaryTree(nodeAmount: Int, value: T): Tree<T> {
    fun generate(nodeAddress: Int): Tree<T> =
        if (nodeAddress > nodeAmount) End
        else Node(value, generate(nodeAddress * 2), generate(nodeAddress * 2 + 1))

    return generate(1)
}

class P63Test {
    @Test fun `construct complete binary tree`() {
        assertThat(completeBinaryTree(1, "x"), equalToTree(
            Node("x")
        ))
        assertThat(completeBinaryTree(2, "x"), equalToTree(
            Node("x", Node("x"))
        ))
        assertThat(completeBinaryTree(3, "x"), equalToTree(
            Node("x", Node("x"), Node("x"))
        ))
        assertThat(completeBinaryTree(6, "x"), equalToTree(
            Node("x",
                 Node("x", Node("x"), Node("x")),
                 Node("x", Node("x"), End))
        ))
    }
}