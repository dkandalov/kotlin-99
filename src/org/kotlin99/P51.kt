package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

interface Tree<out T>

data class Node<out T>(val value: T, val left: Tree<T> = End, val right: Tree<T> = End) : Tree<T> {
    override fun toString(): String {
        val children = if (left == End && right == End) "" else " $left $right"
        return "T($value$children)"
    }
}

val End = object : Tree<Nothing>{
    override fun toString() = "."
}


class P51Test {
    @Test fun `tree construction and string conversion`() {
        val node =
            Node('a',
                Node('b',
                    Node('d'),
                    Node('e')),
                Node('c', End,
                    Node('f', Node('g'),
                    End)))
        assertThat(node.toString(), equalTo("T(a T(b T(d) T(e)) T(c . T(f T(g) .)))"))
    }
}