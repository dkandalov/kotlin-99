package org.kotlin99.logic

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.tail


fun String.createEncoding(): HuffmanEncoding =
    createEncoding(charAndFrequency())

fun String.encode(encoding: HuffmanEncoding) =
    map { char -> encoding.codeByChar[char] }.joinToString("")

fun String.decode(encoding: HuffmanEncoding): String {
    var result = ""
    var chars = this.toList()
    var node = encoding.tree
    while (chars.isNotEmpty()) {
        node = node.followCode(chars.first())!!
        chars = chars.tail()
        if (node.char != null) {
            result += node.char
            node = encoding.tree
        }
    }
    return result
}

fun String.charAndFrequency(): Map<Char, Int> =
    toCharArray().groupBy { it }.map { Pair(it.key, it.value.size) }.toMap()

fun createEncoding(frequencyByChar: Map<Char, Int>): HuffmanEncoding {
    val tree = buildTree(frequencyByChar.map { Node(it.value, it.key) })
    val codeByChar = tree.leavesWithPath().toMap()
    return HuffmanEncoding(tree, codeByChar)
}

fun buildTree(nodes: List<Node>): Node {
    return if (nodes.size == 1) {
        nodes.first()
    } else {
        val sortedNodes = nodes.sortedBy { it.weight }
        val node = Node(sortedNodes[0].weight + sortedNodes[1].weight, null, sortedNodes[0], sortedNodes[1])
        buildTree(sortedNodes.drop(2) + node)
    }
}


data class HuffmanEncoding(val tree: Node, val codeByChar: Map<Char, String>)


data class Node(val weight: Int, val char: Char?, val left: Node? = null, val right: Node? = null) {
    fun leavesWithPath(): List<Pair<Char, String>> =
        if (left == null && right == null) listOf(Pair(char!!, ""))
        else (left?.leavesWithPath()?.map { Pair(it.first, "0" + it.second) } ?: emptyList()) +
            (right?.leavesWithPath()?.map { Pair(it.first, "1" + it.second) } ?: emptyList())

    fun followCode(code: Char): Node? =
        when (code) {
            '0'  -> left
            '1'  -> right
            else -> throw IllegalStateException("Unexpected code '$code'")
        }

    override fun toString(): String {
        var s = "Node($weight"
        if (char != null) s += ", '$char'"
        if (left != null && right != null) s += ", $left, $right"
        return "$s)"
    }
}


class P50Test {
    @Test fun `letter to code mapping`() {
        assertThat(
            createEncoding(linkedMapOf(
                Pair('a', 25), Pair('b', 21), Pair('c', 18), Pair('d', 14), Pair('e', 9), Pair('f', 7), Pair('g', 6)
            )).codeByChar,
            equalTo(linkedMapOf(
                Pair('a', "10"), Pair('b', "00"), Pair('c', "111"), Pair('d', "110"), Pair('e', "010"), Pair('f', "0111"), Pair('g', "0110")
            ))
        )
    }

    @Test fun `encoding and decoding a string`() {
        val string = "this is a sentence"
        val encoding = string.createEncoding()

        val encodedString = string.encode(encoding)
        assertThat(encodedString, equalTo("00110000101011100101011101001110101111011001111011000111"))

        val decodedString = encodedString.decode(encoding)
        assertThat(decodedString, equalTo("this is a sentence"))
    }
}