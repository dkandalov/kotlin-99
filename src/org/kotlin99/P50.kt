package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.P50.huffmanCodes


// Use object as namespace to avoid conflicting names.
object P50 {

    fun huffmanCodes(charAndFreq: List<Pair<Char, Int>>): List<Pair<Char, String>> {
        val nodes = charAndFreq.map{ Node(it.second, it.first) }
        val tree = buildTree(nodes)
        return charAndFreq.map{ Pair(it.first, tree.findPath(it.first)!!.reversed()) }
    }

    private fun buildTree(nodes: List<Node>): Node =
            if (nodes.size == 1) {
                nodes.first()
            } else {
                val sortedNodes = nodes.sortedBy{ it.weight }
                val node = Node(sortedNodes[0].weight + sortedNodes[1].weight, null, sortedNodes[0], sortedNodes[1])
                buildTree(sortedNodes.drop(2) + node)
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

}


class P50Test {
    @Test fun `Huffman encoding`() {
        assertThat(
            huffmanCodes(listOf(Pair('a', 45), Pair('b', 13), Pair('c', 12), Pair('d', 16), Pair('e', 9), Pair('f', 5))),
            equalTo(listOf(Pair('a', "0"), Pair('b', "101"), Pair('c', "100"), Pair('d', "111"), Pair('e', "1101"), Pair('f', "1100")))
        )
    }
}