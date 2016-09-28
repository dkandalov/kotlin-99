package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

data class Point(val x: Int, val y: Int)

data class Positioned<out T>(val value: T, val point: Point) {
    constructor (value: T, x: Int, y: Int) : this(value, Point(x, y))

    override fun toString(): String =
            "[" + point.x.toString() + "," + point.y.toString() + "] " + value.toString()
}


fun <T> Tree<T>.layout(xShift: Int = 0, y: Int = 1): Tree<Positioned<T>> =
        if (this == End) {
            End
        } else if (this is Node<T>) {
            val positionedLeft = left.layout(xShift, y + 1)
            val positionedRight = right.layout(left.nodeCount() + 1 + xShift, y + 1)
            val point = Point(left.nodeCount() + 1 + xShift, y)
            Node(Positioned(value, point), positionedLeft, positionedRight)
        } else {
            throw throw UnknownTreeImplementation(this)
        }


class P64Test {
    @Test fun `layout binary tree (1)`() {
        assertThat(Node("a").layout(), equalTo<Tree<Positioned<String>>>(Node(Positioned("a", 1, 1))))
        assertThat(Node("a", Node("b")).layout(), equalTo<Tree<Positioned<String>>>(
                Node(Positioned("a", 2, 1),
                     Node(Positioned("b", 1, 2)),
                     End)
        ))
        assertThat(Node("a", Node("b"), Node("c")).layout(), equalTo<Tree<Positioned<String>>>(
                Node(Positioned("a", 2, 1),
                     Node(Positioned("b", 1, 2)),
                     Node(Positioned("c", 3, 2)))
        ))
        assertThat(Node("a", Node("b", End, Node("c")), Node("d")).layout(), equalTo<Tree<Positioned<String>>>(
                Node(Positioned("a", 3, 1),
                     Node(Positioned("b", 1, 2), End, Node(Positioned("c", 2, 3))),
                     Node(Positioned("d", 4, 2)))
        ))
        assertThat(Node("a", Node("b", Node("b1"), Node("b2")), Node("c", Node("c1"), Node("c2"))).layout(), equalTo<Tree<Positioned<String>>>(
                Node(Positioned("a", 4, 1),
                     Node(Positioned("b", 2, 2),
                          Node(Positioned("b1", 1, 3)),
                          Node(Positioned("b2", 3, 3))),
                     Node(Positioned("c", 6, 2),
                          Node(Positioned("c1", 5, 3)),
                          Node(Positioned("c2", 7, 3))))
        ))
    }
}