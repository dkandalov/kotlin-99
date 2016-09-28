package org.kotlin99

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.describe
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotlin99.P55Test.Companion.nodeList

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
        else throw UnknownTreeImplementation(this)

fun <T> Tree<T>.nodes(): List<Node<T>> = when {
    this is Node<T> -> left.nodes() + right.nodes() + this
    else -> emptyList()
}

class P59Test {
    @Test fun `construct all height-balanced binary trees`() {
        assertThat(heightBalancedTrees(1, "x"), hasSameElementsAs(nodeList(Node("x"))))
        assertThat(heightBalancedTrees(2, "x"), hasSameElementsAs(nodeList(
                Node("x", Node("x"), Node("x")),
                Node("x", End, Node("x")),
                Node("x", Node("x"), End)
        )))
        assertThat(heightBalancedTrees(3, "x"), containsAll(nodeList(
                Node("x",
                    Node("x", End, Node("x")),
                    Node("x", Node("x"), End)),
                Node("x",
                    Node("x", Node("x"), End),
                    Node("x", End, Node("x")))
        )))

        heightBalancedTrees(3, "x").flatMap{ it.nodes() }.forEach { node ->
            assertTrue(node.left.height() - node.right.height() <= 1)
        }
    }
}

fun <T> containsAll(expected: Iterable<T>) : Matcher<Iterable<T>> {
    return object : Matcher.Primitive<Iterable<T>>() {
        override fun invoke(actual: Iterable<T>): MatchResult {
            val actualList = actual.toList()
            val expectedList = expected.toList()
            val isMatch = actualList.containsAll(expectedList)
            return if (isMatch) MatchResult.Match else MatchResult.Mismatch("was ${describe(actual)}")
        }
        override val description: String get() = "contains all elements ${describe(expected)}"
        override val negatedDescription : String get() = "doesn't contain all elements ${describe(expected)}"
    }
}
