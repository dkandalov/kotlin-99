package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Test
import org.kotlin99.P57Test.Companion.equalTo

fun <T> Tree<T>.layout2(parentX: Int = 0, y: Int = 1, totalHeight: Int = height()): Tree<Positioned<T>> =
    if (this == End) End
    else if (this is Node<T>) {

        val positionedLeft = left.layout2(parentX, y + 1, totalHeight)
        val newX =
            if (positionedLeft is Node<Positioned<T>>) {
                val shiftFromChildren = Math.pow(2.0, (totalHeight - y - 1).toDouble()).toInt()
                positionedLeft.value.point.x + shiftFromChildren
            } else {
                val shiftFromParent = Math.pow(2.0, (totalHeight - y).toDouble()).toInt()
                if (parentX == 0) 1 else parentX + shiftFromParent
            }

        val positionedRight = right.layout2(newX, y + 1, totalHeight)
        Node(Positioned(value, newX, y), positionedLeft, positionedRight)

    } else {
        throw UnknownTreeImplementation(this)
    }


class P65Test {
    @Test fun `layout binary tree (2)`() {
        assertThat(Node("a").layout2(), equalTo(
                Node(Positioned("a", 1, 1))
        ))
        assertThat(Node("a", Node("b")).layout2(), equalTo(
                Node(Positioned("a", 2, 1),
                     Node(Positioned("b", 1, 2)),
                     End)
        ))
        assertThat(Node("a", Node("b"), Node("c")).layout2(), equalTo(
                Node(Positioned("a", 2, 1),
                     Node(Positioned("b", 1, 2)),
                     Node(Positioned("c", 3, 2)))
        ))
        assertThat(Node("a", Node("b", Node("c"))).layout2(), equalTo(
                Node(Positioned("a", 4, 1),
                     Node(Positioned("b", 2, 2),
                          Node(Positioned("c", 1, 3))))
        ))
        assertThat(Node("a", End, Node("b", End, Node("c"))).layout2(), equalTo(
                Node(Positioned("a", 1, 1),
                     End,
                     Node(Positioned("b", 3, 2),
                          End,
                          Node(Positioned("c", 4, 3))))
        ))
        assertThat(Node("a", Node("b", Node("b1"), Node("b2")), Node("c", Node("c1"), Node("c2"))).layout2(), equalTo(
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