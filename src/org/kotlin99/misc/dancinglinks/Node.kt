package org.kotlin99.misc.dancinglinks

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.*

open class Node(val label: String? = null) {
    var left: Node = none
    var right: Node = none
    var up: Node = none
    var down: Node = none
    var header: Node = none

    fun linkRight(other: Node): Node {
        right = other
        other.left = this
        return other
    }

    fun linkDown(other: Node): Node {
        down = other
        other.up = this
        return other
    }

    fun toListUp() = toList(Node::up)
    fun toListDown() = toList(Node::down)
    fun toListLeft() = toList(Node::left)
    fun toListRight() = toList(Node::right)
    fun toList(direction: (Node) -> Node): List<Node> {
        val result = ArrayList<Node>()
        result.add(this)
        each(direction) { result.add(it) }
        return result
    }

    fun eachUp(f: (Node) -> Unit) = each(Node::up, f)
    fun eachDown(f: (Node) -> Unit) = each(Node::down, f)
    fun eachLeft(f: (Node) -> Unit) = each(Node::left, f)
    fun eachRight(f: (Node) -> Unit) = each(Node::right, f)
    fun each(direction: (Node) -> Node, f: (Node) -> Unit) {
        var next = direction(this)
        while (next != this && next != none) {
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

    fun sizeDown(): Int {
        var size = 1
        eachDown { size++ }
        return size
    }

    override fun toString(): String {
        return label ?: "*"
    }

    companion object {
        val none = Node()
    }
}

class NodeTest {
    val a = Node("a")
    val b = Node("b")
    val c = Node("c")

    init {
        a.linkDown(b).linkDown(c).linkDown(a)
    }

    @Test fun `node conversion to list`() {
        assertThat(a.toListDown().map(Node::label), equalTo(listOf<String?>("a", "b", "c")))
        assertThat(a.toListUp().map(Node::label), equalTo(listOf<String?>("a", "c", "b")))

        assertThat(b.toListDown().map(Node::label), equalTo(listOf<String?>("b", "c", "a")))
        assertThat(b.toListUp().map(Node::label), equalTo(listOf<String?>("b", "a", "c")))
    }

    @Test fun `node iteration doesn't include node itself`() {
        ArrayList<String>().apply {
            a.eachDown { this.add(it.label!!) }
            assertThat(this, equalTo(listOf("b", "c")))
        }
        ArrayList<String>().apply {
            a.eachUp { this.add(it.label!!) }
            assertThat(this, equalTo(listOf("c", "b")))
        }
    }

    @Test fun `node links size`() {
        assertThat(a.sizeDown(), equalTo(3))
    }
}