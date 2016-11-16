package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert.assertTrue
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
        h.joinDown(h)
        val headers = 0.rangeTo(width - 1).map{ Node("$it") }
        headers.forEach { it.joinDown(it) }
        headers.pairs().forEach { it.first.joinRight(it.second) }
        headers.last().joinRight(h).joinRight(headers.first())

        0.rangeTo(height - 1).forEach { row ->
            var firstNode: Node? = null
            var prevNode: Node? = null
            0.rangeTo(width - 1).forEach { column ->
                if (matrix[row][column] == 1) {
                    val node = Node("$column,$row")
                    val headerNode = headers[column]

                    node.header = headerNode
                    headerNode.up.joinDown(node).joinDown(headerNode)
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

    fun search(k: Int = 0, answer: ArrayList<Node> = ArrayList()): List<Node> {
        if (h.right == h) return answer

        var column = chooseColumn()
        column.coverColumn()

        var row = column.down
        while (row != column) {
            answer.add(row)
            row.eachRight { it.header.coverColumn() }

            search(k + 1, answer)

            row = answer.removeAt(answer.size - 1)
            column = row.header
            row.eachLeft { it.header.uncoverColumn() }

            row = row.down
        }

        column.uncoverColumn()
        
        return answer
    }

    private fun chooseColumn() = h.right

    override fun toString(): String {
        fun Node.toList(direction: (Node) -> (Node?)): List<Node> {
            val result = ArrayList<Node>()
            result.add(this)
            each(direction){ result.add(it) }
            return result
        }
        fun Node.toListRight(): List<Node> = toList(Node::right)
        fun Node.toListDown(): List<Node> = toList(Node::down)
        fun Node.distanceToHeader(): Int {
            return header.toListDown().indexOf(this) - 1
        }

        val lines = ArrayList<String>()

        val headers = h.toListRight().tail()
        lines.add(headers.joinToString(""){ it.label.toString() })

        var nodeStacks = headers.map{ it.toListDown().tail() }
        while (!nodeStacks.all{ it.isEmpty() }) {
            val node = nodeStacks
                .filter{ it.isNotEmpty() }
                .minBy { it.first().toListRight().sumBy(Node::distanceToHeader) }!!.first()

            val nodesInRow = node.toListRight()
            val line = nodeStacks.map { stack ->
                    if (stack.any{ nodesInRow.contains(it) }) "1" else "0"
                }.joinToString("")
            lines.add(line)

            nodeStacks = nodeStacks.map{ stack ->
                stack.filter{ !nodesInRow.contains(it) }
            }
        }
        return lines.joinToString("\n")
    }

    private fun <T> List<T>.pairs(): List<Pair<T, T>> {
        return if (size <= 1) emptyList()
        else listOf(Pair(this[0], this[1])) + tail().pairs()
    }
}

class Node(val label: String? = null) {
    var left: Node = none
    var right: Node = none
    var up: Node = none
    var down: Node = none
    var header: Node = none

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

    fun eachUp(f: (Node) -> Unit) = each(Node::up, f)
    fun eachDown(f: (Node) -> Unit) = each(Node::down, f)
    fun eachLeft(f: (Node) -> Unit) = each(Node::left, f)
    fun eachRight(f: (Node) -> Unit) = each(Node::right, f)

    fun each(direction: (Node) -> (Node?), f: (Node) -> Unit) {
        var next = direction(this)
        while (next != this && next != null) {
            f(next)
            next = direction(next)
        }
    }

    fun coverColumn() {
        right.left = left
        left.right = right
        eachDown { i ->
            i.eachRight { j ->
                j.down.up = j.up
                j.up.down = j.down
            }
        }
    }

    fun uncoverColumn() {
        eachUp { i ->
            i.eachLeft { j ->
                j.down.up = j
                j.up.down = j
            }
        }
        right.left = this
        left.right = this
    }

    override fun toString(): String {
        return label ?: "*"
    }

    companion object {
        val none = Node()
    }
}


class DancingLinksTest {
    @Test fun `creating new dancing links matrix`() {
        val matrix = DLMatrix(listOf(
            listOf(0, 0, 1, 0, 1, 1, 0),
            listOf(1, 0, 0, 1, 0, 0, 1),
            listOf(0, 1, 1, 0, 0, 1, 0),
            listOf(1, 0, 0, 1, 0, 0, 0),
            listOf(0, 1, 0, 0, 0, 0, 1),
            listOf(0, 0, 0, 1, 1, 0, 1)
        ))

        assertLinkedInAllDirections(matrix.h)

        assertThat(matrix.h.right.label!!, equalTo("0"))
        assertThat(matrix.h.left.label!!, equalTo("6"))

        assertThat(matrix.h.right.down.label!!, equalTo("0,1"))
        assertThat(matrix.h.left.down.label!!, equalTo("6,1"))
    }

    @Test fun `dancing links matrix conversion to string`() {
        val matrix = DLMatrix(listOf(
            listOf(0, 0, 1, 0, 1, 1, 0),
            listOf(1, 0, 0, 1, 0, 0, 1),
            listOf(0, 1, 1, 0, 0, 1, 0),
            listOf(1, 0, 0, 1, 0, 0, 0),
            listOf(0, 1, 0, 0, 0, 0, 1),
            listOf(0, 0, 0, 1, 1, 0, 1)
        ))

        assertThat(matrix.toString(), equalTo("""
            |0123456
            |1001001
            |0010110
            |1001000
            |0110010
            |0100001
            |0001101
        """.trimMargin()))
    }

    @Test fun `cover first column in matrix`() {
        val matrix = DLMatrix(listOf(
                listOf(0, 0, 1, 0, 1, 1, 0),
                listOf(1, 0, 0, 1, 0, 0, 1),
                listOf(0, 1, 1, 0, 0, 1, 0),
                listOf(1, 0, 0, 1, 0, 0, 0),
                listOf(0, 1, 0, 0, 0, 0, 1),
                listOf(0, 0, 0, 1, 1, 0, 1)
        ))

        matrix.h.right.coverColumn()

        assertLinkedInAllDirections(matrix.h)
        assertThat(matrix.toString(), equalTo("""
            |123456
            |010110
            |100001
            |110010
            |001101
        """.trimMargin()))
    }

    @Ignore // TODO failing
    @Test fun `find solution for cover problem from dancing links paper`() {
        val matrix = DLMatrix(listOf(
                listOf(0, 0, 1, 0, 1, 1, 0),
                listOf(1, 0, 0, 1, 0, 0, 1),
                listOf(0, 1, 1, 0, 0, 1, 0),
                listOf(1, 0, 0, 1, 0, 0, 0),
                listOf(0, 1, 0, 0, 0, 0, 1),
                listOf(0, 0, 0, 1, 1, 0, 1)
        ))
        println(matrix.search())
        assertThat(matrix.search().size, equalTo(1))
    }

    private fun assertLinkedInAllDirections(node: Node, visited: HashSet<Node> = HashSet()) {
        assertTrue(node != Node.none)

        if (visited.contains(node)) return
        visited.add(node)

        assertLinkedInAllDirections(node.up, visited)
        assertLinkedInAllDirections(node.down, visited)
        assertLinkedInAllDirections(node.left, visited)
        assertLinkedInAllDirections(node.right, visited)
    }
}