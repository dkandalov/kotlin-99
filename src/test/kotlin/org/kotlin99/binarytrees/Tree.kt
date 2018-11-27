package org.kotlin99.binarytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.binarytrees.Tree.End
import org.kotlin99.binarytrees.Tree.Node

@Suppress("unused") // suppress because IJ wrongly thinks type parameters is not necessary
sealed class Tree<out T> {

    // This class declared inside Tree interface to use Tree as a "namespace".
    data class Node<out T>(
        val value: T,
        val left: Tree<T> = End,
        val right: Tree<T> = End
    ): Tree<T>() {
        override fun toString(): String {
            val children = if (left == End && right == End) "" else " $left $right"
            return "T($value$children)"
        }
    }

    object End: Tree<Nothing>() {
        override fun toString() = "."
    }
}

class TreeTest {
    @Test fun `tree construction and string conversion`() {
        val node =
            Node('a',
                 Node('b',
                      Node('d'),
                      Node('e')),
                 Node('c', End,
                      Node('f', Node('g'),
                           End)))
        assertThat(node.toString(), equalTo("T(a T(b T(d) T(e)) T(c . T(f T(g) .)))"))
    }
}