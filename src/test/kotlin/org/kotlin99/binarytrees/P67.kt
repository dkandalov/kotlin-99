package org.kotlin99.binarytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.binarytrees.P57Test.Companion.equalToTree
import org.kotlin99.binarytrees.Tree.End
import org.kotlin99.binarytrees.Tree.Node

fun Tree<*>.convertToString(): String =
    when (this) {
        End     -> ""
        is Node ->
            value.toString() + if (left != End || right != End) {
                "(" + left.convertToString() + "," + right.convertToString() + ")"
            } else {
                ""
            }
    }

fun String.convertToTree(): Tree<String> {
    fun String.drop(prefix: String): String =
        if (!startsWith(prefix)) throw IllegalStateException("Expected '$this' to start with '$prefix'")
        else drop(prefix.length)

    fun String.parse(): Pair<Tree<String>, Int> {
        val value = takeWhile { it != '(' && it != ',' && it != ')' }
        var rest = substring(value.length)
        return if (value.isEmpty()) {
            Pair(End, 0)
        } else if (!rest.startsWith("(")) {
            Pair(Node(value), value.length)
        } else {
            rest = rest.drop("(")
            val (left, leftLength) = rest.parse()
            rest = rest.drop(leftLength).drop(",")
            val (right, rightLength) = rest.parse()
            rest.drop(rightLength).drop(")")
            Pair(Node(value, left, right), value.length + leftLength + rightLength + 3)
        }
    }

    return parse().first
}


class P67Test {
    @Test fun `conversion to string`() {
        assertThat(End.convertToString(), equalTo(""))
        assertThat(Node("a").convertToString(), equalTo("a"))
        assertThat(Node("a", Node("b"), Node("c")).convertToString(), equalTo("a(b,c)"))
        assertThat(Node("a", Node("b", Node("d"), Node("e")), Node("c", End, Node("f", Node("g"), End))).convertToString(), equalTo(
            "a(b(d,e),c(,f(g,)))"
        ))
    }

    @Test fun `conversion from string`() {
        assertThat("".convertToTree(), equalToTree(End))
        assertThat("a".convertToTree(), equalToTree(Node("a")))
        assertThat("a(b,c)".convertToTree(), equalToTree(Node("a", Node("b"), Node("c"))))
        assertThat("a(b(d,e),c(,f(g,)))".convertToTree(), equalToTree(
            Node("a", Node("b", Node("d"), Node("e")), Node("c", End, Node("f", Node("g"), End)))
        ))
    }
}