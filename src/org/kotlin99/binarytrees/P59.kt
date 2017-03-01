package org.kotlin99.binarytrees

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotlin99.binarytrees.P55Test.Companion.nodeList
import org.kotlin99.binarytrees.Tree.*
import org.kotlin99.common.*

fun <T> heightBalancedTrees(height: Int, value: T): List<Tree<T>> =
    if (height < 1) listOf(End)
    else if (height == 1) listOf(Node(value))
    else {
        val fullHeightTrees = heightBalancedTrees(height - 1, value)
        val shortHeightTrees = heightBalancedTrees(height - 2, value)

        val nodes1 = fullHeightTrees.flatMap { tree1 ->
            fullHeightTrees.map{ tree2 ->
                Node(value, tree1, tree2)
            }
        }
        val nodes2 = fullHeightTrees.flatMap { tree1 ->
            shortHeightTrees.flatMap { tree2 ->
                listOf(Node(value, tree1, tree2), Node(value, tree2, tree1))
            }
        }

        nodes1 + nodes2
    }

fun <T> Tree<T>.height(): Int =
        if (this == End) 0
        else if (this is Node<T>) 1 + Math.max(left.height(), right.height())
        else this.throwUnknownImplementation()

fun <T> Tree<T>.nodes(): List<Node<T>> = when {
    this is Node<T> -> left.nodes() + right.nodes() + this
    else -> emptyList()
}

class P59Test {
    @Test fun `construct all height-balanced binary trees`() {
        assertThat(heightBalancedTrees(1, "x"), containsAll(nodeList(Node("x"))))
        assertThat(heightBalancedTrees(2, "x"), containsAll(nodeList(
            Node("x", Node("x"), Node("x")),
            Node("x", End, Node("x")),
            Node("x", Node("x"), End)
        )))

        assertThat(heightBalancedTrees(3, "x"), containsElements<Tree<String>>(
            equalTo(
                Node("x",
                     Node("x", End, Node("x")),
                     Node("x", Node("x"), End))),
            equalTo(
                Node("x",
                     Node("x", Node("x"), End),
                     Node("x", End, Node("x"))))
        ))

        heightBalancedTrees(3, "x").flatMap{ it.nodes() }.forEach { node ->
            assertTrue(node.left.height() - node.right.height() <= 1)
        }
    }

    private fun <T> containsElements(vararg matchers: Matcher<T>) : Matcher<Iterable<T>> {
        return matchers.tail().fold(anyElement(matchers.first())){ result: Matcher<Iterable<T>>, matcher ->
            result.and(anyElement(matcher))
        }
    }
}
