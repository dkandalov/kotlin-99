package org.kotlin99.binarytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.binarytrees.P57Test.Companion.equalToTree
import org.kotlin99.binarytrees.Tree.End
import org.kotlin99.binarytrees.Tree.Node

fun <T> Tree<T>.preorder(): List<T> =
    when (this) {
        End        -> emptyList()
        is Node<T> -> listOf(value) + left.preorder() + right.preorder()
    }

fun <T> Tree<T>.inorder(): List<T> =
    when (this) {
        End        -> emptyList()
        is Node<T> -> left.inorder() + listOf(value) + right.inorder()
    }

fun <T> createTree(preorder: List<T>, inorder: List<T>): Tree<T> =
    if (preorder.isEmpty()) End
    else {
        val (leftInorder, rightInorder) = inorder.spanAt { it == preorder.first() }
        Node(
            preorder.first(),
            createTree(preorder.drop(1).take(leftInorder.size), leftInorder),
            createTree(preorder.drop(leftInorder.size + 1), rightInorder)
        )
    }

private fun <T> List<T>.spanAt(p: (T) -> Boolean): Pair<List<T>, List<T>> {
    if (isEmpty()) return Pair(this, this)
    var i = 0
    while (i < size && !p(this[i])) i++

    return if (i >= size) Pair(this, emptyList())
    else Pair(subList(0, i), subList(i + 1, size))
}


class P68Test {
    @Test fun `pre-order list of tree values`() {
        assertThat("a".convertToTree().preorder(), equalTo(listOf("a")))
        assertThat("a(b,c)".convertToTree().preorder(), equalTo(listOf("a", "b", "c")))
        assertThat("a(b(d,e),c(,f(g,)))".convertToTree().preorder(), equalTo(listOf("a", "b", "d", "e", "c", "f", "g")))
    }

    @Test fun `in-order list of tree values`() {
        assertThat("a".convertToTree().inorder(), equalTo(listOf("a")))
        assertThat("a(b,c)".convertToTree().inorder(), equalTo(listOf("b", "a", "c")))
        assertThat("a(b(d,e),c(,f(g,)))".convertToTree().inorder(), equalTo(listOf("d", "b", "e", "a", "c", "g", "f")))
    }

    @Test fun `convert pre-order and in-order lists into a tree`() {
        assertThat(createTree(preorder = listOf("a"), inorder = listOf("a")),
                   equalToTree("a".convertToTree()))

        assertThat(createTree(preorder = listOf("a", "b", "c"), inorder = listOf("b", "a", "c")),
                   equalToTree("a(b,c)".convertToTree()))

        assertThat(createTree(preorder = listOf("a", "b", "d", "e", "c", "f", "g"), inorder = listOf("d", "b", "e", "a", "c", "g", "f")),
                   equalToTree("a(b(d,e),c(,f(g,)))".convertToTree()))
    }

    @Test fun `convert pre-order and in-order lists with duplicate values into a tree`() {
        assertThat(createTree(preorder = listOf("a", "b", "a"), inorder = listOf("b", "a", "a")),
                   equalToTree("a(b,a)".convertToTree()))

        assertThat(createTree(preorder = listOf("a", "a", "a"), inorder = listOf("a", "a", "a")),
                   equalToTree("a(,a(,a))".convertToTree()))
    }

    @Test fun `span list at element matching predicate (and drop matched element)`() {
        assertThat(emptyList<Int>().spanAt { it > 0 }, equalTo(Pair(emptyList(), emptyList())))
        assertThat(listOf(0, 1, 2).spanAt { it <= 0 }, equalTo(Pair(listOf(), listOf(1, 2))))
        assertThat(listOf(0, 1, 2).spanAt { it == 1 }, equalTo(Pair(listOf(0), listOf(2))))
        assertThat(listOf(0, 1, 2).spanAt { it >= 2 }, equalTo(Pair(listOf(0, 1), listOf())))
        assertThat(listOf(0, 1, 2).spanAt { it == 100 }, equalTo(Pair(listOf(0, 1, 2), listOf())))
    }
}