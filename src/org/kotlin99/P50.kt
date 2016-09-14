package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun huffman(charAndFreq: List<Pair<Char, Int>>): List<Pair<Char, String>> {
    val sortedNodes = charAndFreq.map{ Node(it.second, it.first) }.sortedBy{ it.weight }
    val tree = buildTree(sortedNodes)
    return charAndFreq.map{ Pair(it.first, tree.findPath(it.first)!!.reversed()) }
}

private fun buildTree(nodes: List<Node>): Node =
    if (nodes.size == 1) nodes.first()
    else {
        val node = Node(nodes[0].weight + nodes[1].weight, null, nodes[0], nodes[1])
        buildTree((nodes.drop(2) + node).sortedBy{ it.weight })
    }

private data class Node(val weight: Int, val char: Char?, val left: Node? = null, val right: Node? = null) {
    fun findPath(char: Char): String? =
        if (char == this.char) ""
        else left?.findPath(char)?.plus("0") ?: right?.findPath(char)?.plus("1")

    override fun toString(): String {
        var s = "Node($weight"
        if (char != null) s += ", '$char'"
        if (left != null && right != null) s += ", $left, $right"
        return s + ")"
    }
}


class P50Test {
    @Test fun `Huffman encoding`() {
        assertThat(
            huffman(listOf(Pair('a', 45), Pair('b', 13), Pair('c', 12), Pair('d', 16), Pair('e', 9), Pair('f', 5))),
            equalTo(listOf(Pair('a', "0"), Pair('b', "101"), Pair('c', "100"), Pair('d', "111"), Pair('e', "1101"), Pair('f', "1100")))
        )
    }
}