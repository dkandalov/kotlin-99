package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

fun <T : Comparable<T>> List<T>.toTree(): Tree<T> =
    fold(End as Tree<T>) { tree, value ->
        tree.add(value)
    }

fun <T : Comparable<T>> Tree<T>.add(value: T): Tree<T> =
        if (this == End) {
            Node(value)
        } else if (this is Node<T>) {
            if (this.value < value) copy(right = right.add(value))
            else copy(left = left.add(value))
        } else {
            throw UnknownTreeImplementation(this)
        }


class P57Test {
    @Test fun `add element to tree`() {
        assertThat(End.add(2), equalTo<Tree<Int>>(Node(2)))
        assertThat(Node(2).add(3), equalTo<Tree<Int>>(Node(2, End, Node(3))))
        assertThat(Node(2, End, Node(3)).add(0), equalTo<Tree<Int>>(Node(2, Node(0), Node(3))))
    }

    @Test fun `convert list to tree`() {
        assertThat(listOf(3, 2, 5, 7, 1).toTree(), equalTo<Tree<Int>>(
            Node(3,
                Node(2, Node(1), End),
                Node(5, End, Node(7)))
        ))
    }

    @Test fun `conversion to tree creates symmetric trees`() {
        assertTrue(listOf(5, 3, 18, 1, 4, 12, 21).toTree().isSymmetric())
        assertFalse(listOf(3, 2, 5, 7, 4).toTree().isSymmetric())
    }
}