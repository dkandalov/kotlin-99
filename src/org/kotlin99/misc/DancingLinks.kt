package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Ignore
import org.junit.Test
import org.kotlin99.common.tail
import java.util.*

class DLMatrix(matrix: List<List<Int>>) {
    val h: Node

    init {
        val width = matrix.first().size
        val height = matrix.size

        h = Node("h")
        val headers = 0.rangeTo(width - 1).map{ Node("$it") }
        headers.forEach { it.joinDown(it) }
        headers.pairs().forEach { it.first.joinRight(it.second) }
        headers.last().joinRight(h).joinRight(headers.first())

        0.rangeTo(height - 1).forEach { row ->
            var firstNode: Node? = null
            var prevNode: Node? = null
            0.rangeTo(width - 1).forEach { column ->
                if (matrix[row][column] == 1) {
                    val node = Node("$row,$column")
                    val headerNode = headers[column]

                    node.header = headerNode
                    headerNode.up!!.joinDown(node).joinDown(headerNode)
                    if (prevNode == null) {
                        firstNode = node
                        prevNode = node
                    }
                    prevNode!!.joinRight(node).joinRight(firstNode!!)

                    prevNode = node
                }
            }
        }
    }

    fun search(k: Int = 0) {
        if (h.right == h) return

        val column = chooseColumn()
        coverColumn(column)
        column.each(Node::down) { row ->
            // ok = row
            row.each(Node::right) { j ->
                coverColumn(j)
            }
            search(k + 1)
            // row = ok
            // column = row.header
            row.each(Node::left) { j ->
                uncoverColumn(j)
            }
        }
        uncoverColumn(column)
    }

    private fun chooseColumn() = h.right!!

    private fun coverColumn(c: Node) {
        c.right!!.left = c.left
        c.left!!.right = c.right
        c.each(Node::down) { i ->
            i.each(Node::right) { j ->
                j.down!!.up = j.up
                j.up!!.down = j.down
            }
        }
    }

    private fun uncoverColumn(c: Node) {
        c.each(Node::up) { i ->
            i.each(Node::left) { j ->
                j.down!!.up = j
                j.up!!.down = j
            }
        }
        c.right!!.left = c
        c.left!!.right = c
    }

    override fun toString(): String {
        fun Node.toList(): List<Node> {
            val result = ArrayList<Node>()
            each(Node::down){ result.add(it) }
            return result
        }

        val headerStacks = h.map(Node::right, Node::toList).tail()
        return headerStacks.toString()
    }

    private fun <T> List<T>.pairs(): List<Pair<T, T>> {
        return if (size <= 1) emptyList()
        else listOf(Pair(this[0], this[1])) + tail().pairs()
    }
}

class Node(val label: String? = null) {
    var left: Node? = null
    var right: Node? = null
    var up: Node? = null
    var down: Node? = null
    var header: Node? = null

    fun joinRight(other: Node): Node {
        right = other
        other.left = this
        return other
    }

    fun joinDown(other: Node): Node {
        down = other
        other.up = this
        return other
    }

    fun <T> map(direction: (Node) -> (Node?), f: (Node) -> T): List<T> {
        val result = ArrayList<T>()
        each(direction) { result.add(f(it)) }
        return result
    }

    fun each(direction: (Node) -> (Node?), f: (Node) -> Unit) {
        var next: Node? = this
        do {
            next = direction(next!!)
            f(next!!)
        } while (next != this)
    }
}


class DancingLinksTest {
    @Ignore
    @Test fun `create dancing links matrix`() {
        val dlMatrix = DLMatrix(listOf(
            listOf(0, 0, 1, 0, 1, 1, 0),
            listOf(1, 0, 0, 1, 0, 0, 1),
            listOf(0, 1, 1, 0, 0, 1, 0),
            listOf(1, 0, 0, 1, 0, 0, 0),
            listOf(0, 1, 0, 0, 0, 0, 1),
            listOf(0, 0, 0, 1, 1, 0, 1)
        ))
        assertThat(dlMatrix.toString(), equalTo("""
            |0123456h
            |0010110
            |1001001
            |0110010
            |1001000
            |0100001
            |0001101
        """.trimMargin()))
    }
}