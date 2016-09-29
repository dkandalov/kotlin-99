package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.P64Test.Companion.toPrettyString

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
        assertThat(
                Node("a").layout2().toPrettyString(),
                equalTo("""
                | 012
                |0···
                |1·a·
                |2···
            """.trimMargin()))

        assertThat(
                Node("a", Node("b")).layout2().toPrettyString(),
                equalTo("""
                | 0123
                |0····
                |1··a·
                |2·b··
                |3····
            """.trimMargin()))

        assertThat(
                Node("a", Node("b"), Node("c")).layout2().toPrettyString(),
                equalTo("""
                | 01234
                |0·····
                |1··a··
                |2·b·c·
                |3·····
            """.trimMargin()))

        assertThat(
                Node("a", Node("b", Node("c"))).layout2().toPrettyString(),
                equalTo("""
                | 012345
                |0······
                |1····a·
                |2··b···
                |3·c····
                |4······
                """.trimMargin()))

        assertThat(
                Node("a", End, Node("b", End, Node("c"))).layout2().toPrettyString(),
                equalTo("""
                | 012345
                |0······
                |1·a····
                |2···b··
                |3····c·
                |4······
                """.trimMargin()))

        assertThat(
                Node("a", Node("b", Node("d"), Node("e")), Node("c", Node("f"), Node("g"))).layout2().toPrettyString(),
                equalTo("""
                | 012345678
                |0·········
                |1····a····
                |2··b···c··
                |3·d·e·f·g·
                |4·········
                """.trimMargin()))
    }

    @Test fun `P65 illustration example`() {
        assertThat(
                "nkmcaedgupq".toList().toTree().layout2().toPrettyString(),
                equalTo("""
                | 0123456789012345678901234
                |0·························
                |1···············n·········
                |2·······k···············u·
                |3···c·······m·······p·····
                |4·a···e···············q···
                |5····d·g··················
                |6·························
                """.trimMargin()))
    }

}