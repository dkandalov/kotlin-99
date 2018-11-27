package org.kotlin99.binarytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.binarytrees.P55Test.Companion.nodeList
import org.kotlin99.binarytrees.Tree.End
import org.kotlin99.binarytrees.Tree.Node
import org.kotlin99.common.containsAll

fun maxNodeAmountInHBTree(height: Int): Int =
    Math.pow(2.0, height.toDouble()).toInt() - 1

fun minNodeAmountInHBTree(height: Int): Int =
    when {
        height <= 0 -> 0
        height == 1 -> 1
        else        -> 1 + minNodeAmountInHBTree(height - 1) + minNodeAmountInHBTree(height - 2)
    }

fun maxHeightOfHBTree(nodeAmount: Int): Int =
    (1..Int.MAX_VALUE).first { minNodeAmountInHBTree(it) > nodeAmount } - 1

fun minHeightOfHBTree(nodeAmount: Int): Int =
    if (nodeAmount == 0) 0
    else minHeightOfHBTree(nodeAmount / 2) + 1

fun Tree<*>.nodeCount(): Int =
    when (this) {
        End     -> 0
        is Node -> 1 + left.nodeCount() + right.nodeCount()
    }

fun <T> allHBTreesWithNodeAmount(nodeAmount: Int, value: T): List<Tree<T>> {
    val heightRange = minHeightOfHBTree(nodeAmount)..maxHeightOfHBTree(nodeAmount)
    return heightRange
        .flatMap { heightBalancedTrees(it, value) }
        .filter { it.nodeCount() == nodeAmount }
}


class P60Test {
    @Test fun `maximum amount of nodes in height-balanced tree (with specified height)`() {
        assertThat((0..5).map { Pair(it, maxNodeAmountInHBTree(it)) }, equalTo(listOf(
            Pair(0, 0),
            Pair(1, 1),
            Pair(2, 3),
            Pair(3, 7),
            Pair(4, 15),
            Pair(5, 31)
        )))
    }

    @Test fun `minimal amount of nodes in height-balanced tree (with specified height)`() {
        assertThat((0..5).map { Pair(it, minNodeAmountInHBTree(it)) }, equalTo(listOf(
            Pair(0, 0),
            Pair(1, 1),
            Pair(2, 2),
            Pair(3, 4),
            Pair(4, 7),
            Pair(5, 12)
        )))
    }

    @Test fun `maximum height of height-balanced tree (with specified amount of nodes)`() {
        assertThat((0..5).map { it * 5 }.map { Pair(it, maxHeightOfHBTree(it)) }, equalTo(listOf(
            Pair(0, 0),
            Pair(5, 3),
            Pair(10, 4),
            Pair(15, 5),
            Pair(20, 6),
            Pair(25, 6)
        )))
    }

    @Test fun `minimum height of height-balanced tree (with specified amount of nodes)`() {
        assertThat((0..5).map { it * 5 }.map { Pair(it, minHeightOfHBTree(it)) }, equalTo(listOf(
            Pair(0, 0),
            Pair(5, 3),
            Pair(10, 4),
            Pair(15, 4),
            Pair(20, 5),
            Pair(25, 5)
        )))
    }

    @Test fun `amount of nodes in a tree`() {
        assertThat(End.nodeCount(), equalTo(0))
        assertThat(Node("x").nodeCount(), equalTo(1))
        assertThat(Node("x", Node("x", Node("x"))).nodeCount(), equalTo(3))
    }

    @Test fun `all height-balanced trees (with specified amount of nodes)`() {
        assertThat(allHBTreesWithNodeAmount(4, "x"), containsAll(nodeList(
            Node("x",
                 Node("x", Node("x"), End),
                 Node("x")),
            Node("x",
                 Node("x"),
                 Node("x", Node("x"), End)),
            Node("x",
                 Node("x", End, Node("x")),
                 Node("x")),
            Node("x",
                 Node("x"),
                 Node("x", End, Node("x")))
        )))

        assertThat(allHBTreesWithNodeAmount(15, "x").size, equalTo(1553))
    }
}