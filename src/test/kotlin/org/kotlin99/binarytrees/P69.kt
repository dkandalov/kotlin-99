package org.kotlin99.binarytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.binarytrees.P57Test.Companion.equalToTree
import org.kotlin99.binarytrees.Tree.End
import org.kotlin99.binarytrees.Tree.Node


fun Tree<*>.toDotString(): String =
    when (this) {
        End     -> "."
        is Node -> value.toString() + left.toDotString() + right.toDotString()
    }

/**
 * BNF grammar:
 * ```
 * <dot-string> ::= <value><dot-string><dot-string> | "."
 * ```
 */
fun String.fromDotString(): Tree<String> = fromDotString(0).first

private fun String.fromDotString(position: Int): Pair<Tree<String>, Int> =
    if (get(position) == '.') Pair(End, position + 1)
    else {
        val value = get(position).toString()
        val (left, position2) = fromDotString(position + 1)
        val (right, position3) = fromDotString(position2)
        Pair(Node(value, left, right), position3)
    }


class P69Test {
    @Test fun `conversion to dot-string`() {
        assertThat("".convertToTree().toDotString(), equalTo("."))
        assertThat("a".convertToTree().toDotString(), equalTo("a.."))
        assertThat("a(b(d,e),c(,f(g,)))".convertToTree().toDotString(), equalTo(
            "abd..e..c.fg..."
        ))
    }

    @Test fun `conversion from dot-string`() {
        assertThat(".".fromDotString(), equalToTree("".convertToTree()))
        assertThat("a..".fromDotString(), equalToTree("a".convertToTree()))
        assertThat("abd..e..c.fg...".fromDotString(), equalToTree(
            "a(b(d,e),c(,f(g,)))".convertToTree()
        ))
    }
}