package org.kotlin99.binarytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.binarytrees.Tree.End
import org.kotlin99.binarytrees.Tree.Node

fun Tree<*>.isSymmetric(): Boolean = this == End || (this is Node<*> && left.isMirrorOf(right))

fun Tree<*>.isMirrorOf(that: Tree<*>): Boolean =
    when {
        this is Node<*> && that is Node<*> -> left.isMirrorOf(that.right) && right.isMirrorOf(that.left)
        this == End                        -> that == End
        else                               -> false
    }

class P56Test {
    @Test fun `tree is mirror of another tree`() {
        assertThat(Node("x").isMirrorOf(Node("x")), equalTo(true))
        assertThat(Node("x").isMirrorOf(Node("x", Node("x"))), equalTo(false))
        assertThat(Node("x", End, Node("x")).isMirrorOf(Node("x", End, Node("x"))), equalTo(false))
        assertThat(Node("x", End, Node("x")).isMirrorOf(Node("x", Node("x"))), equalTo(true))
    }

    @Test fun `tree is symmetric`() {
        assertThat(Node("x").isSymmetric(), equalTo(true))
        assertThat(Node("x", Node("x")).isSymmetric(), equalTo(false))
        assertThat(Node("x", End, Node("x")).isSymmetric(), equalTo(false))
        assertThat(Node("x", Node("x"), Node("x")).isSymmetric(), equalTo(true))
        assertThat(Node("x", Node("x", End, Node("x")), Node("x", Node("x"))).isSymmetric(), equalTo(true))
    }
}