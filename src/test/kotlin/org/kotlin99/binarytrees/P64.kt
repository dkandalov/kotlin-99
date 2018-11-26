package org.kotlin99.binarytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.binarytrees.Tree.End
import org.kotlin99.binarytrees.Tree.Node


data class Point(val x: Int, val y: Int)

data class Positioned<out T>(val value: T, val point: Point) {
    constructor (value: T, x: Int, y: Int): this(value, Point(x, y))

    override fun toString(): String =
        "[" + point.x.toString() + "," + point.y.toString() + "] " + value.toString()
}


/**
 * Assigned coordinates to tree nodes where:
 * - X is determined by the position of parent node and the amount of current node left children
 * - Y is depth of current node (root node has depth 1)
 */
fun <T> Tree<T>.layout(xShift: Int = 0, y: Int = 1): Tree<Positioned<T>> =
    when (this) {
        End        -> End
        is Node<T> -> {
            val x = xShift + left.nodeCount() + 1
            Node(value = Positioned(value, Point(x, y)),
                 left = left.layout(xShift, y + 1),
                 right = right.layout(x, y + 1))
        }
    }


class P64Test {

    @Test fun `positioned nodes pretty print`() {
        assertThat(
            Node(Positioned("a", 1, 1)).toPrettyString(),
            equalTo("""
                | 012
                |0···
                |1·a·
                |2···
            """.trimMargin()))

        assertThat(
            Node(Positioned("a", 2, 1),
                 Node(Positioned("b", 1, 2)),
                 Node(Positioned("c", 3, 2))).toPrettyString(),
            equalTo("""
                | 01234
                |0·····
                |1··a··
                |2·b·c·
                |3·····
            """.trimMargin()))
    }

    @Test fun `layout binary tree (1)`() {
        assertThat(
            Node("a").layout().toPrettyString(),
            equalTo("""
                | 012
                |0···
                |1·a·
                |2···
            """.trimMargin()))

        assertThat(
            Node("a", Node("b")).layout().toPrettyString(),
            equalTo("""
                | 0123
                |0····
                |1··a·
                |2·b··
                |3····
            """.trimMargin()))

        assertThat(
            Node("a", Node("b", Node("c"))).layout().toPrettyString(),
            equalTo("""
                | 01234
                |0·····
                |1···a·
                |2··b··
                |3·c···
                |4·····
                """.trimMargin()))

        assertThat(
            Node("a", Node("b"), Node("c")).layout().toPrettyString(),
            equalTo("""
                | 01234
                |0·····
                |1··a··
                |2·b·c·
                |3·····
            """.trimMargin()))

        assertThat(
            Node("a", Node("b", End, Node("c")), Node("d")).layout().toPrettyString(),
            equalTo("""
                | 012345
                |0······
                |1···a··
                |2·b··d·
                |3··c···
                |4······
            """.trimMargin()))

        assertThat(
            Node("a", Node("b", Node("d"), Node("e")), Node("c", Node("f"), Node("g"))).layout().toPrettyString(),
            equalTo("""
                | 012345678
                |0·········
                |1····a····
                |2··b···c··
                |3·d·e·f·g·
                |4·········
            """.trimMargin()))
    }

    @Test fun `P64 illustration example`() {
        assertThat(
            "nkmcahgeupsq".toList().toTree().layout().toPrettyString(),
            equalTo("""
                | 01234567890123
                |0··············
                |1········n·····
                |2······k·····u·
                |3··c····m·p····
                |4·a···h·····s··
                |5····g·····q···
                |6···e··········
                |7··············
                """.trimMargin()))
    }

    companion object {
        fun Tree<Positioned<*>>.toPrettyString(xPadding: Int = 1, yPadding: Int = 1): String {
            return when (this) {
                End     -> ""
                is Node -> {
                    val nodes = nodes()
                    val xs = nodes.map { it.value.point.x }
                    val ys = nodes.map { it.value.point.y }
                    val xRange = (xs.min()!! - xPadding)..(xs.max()!! + xPadding)
                    val yRange = (ys.min()!! - yPadding)..(ys.max()!! + yPadding)
                    val nodeByPoint = nodes.groupBy { it.value.point }

                    val xHeader = " " + xRange.map { it.toString().last() }.joinToString("") + "\n"
                    val body = yRange.map { y ->
                        val line = xRange.map { x -> nodeByPoint[Point(x, y)]?.first()?.value?.value ?: "·" }
                        y.toString().last() + line.joinToString("")
                    }
                    xHeader + body.joinToString("\n")
                }
            }
        }
    }
}