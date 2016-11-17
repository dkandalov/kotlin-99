package org.kotlin99.misc.dancinglinks

import java.util.*

class Node(val label: String? = null) {
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

    fun toListRight(): List<Node> = toList(Node::right)
    fun toListDown(): List<Node> = toList(Node::down)
    fun toList(direction: (Node) -> Node): List<Node> {
        val result = ArrayList<Node>()
        result.add(this)
        each(direction){ result.add(it) }
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

    override fun toString(): String {
        return label ?: "*"
    }

    companion object {
        val none = Node()
    }
}