package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> Tree<T>.preorderValues(): List<T> =
        if (this == End) emptyList<T>()
        else if (this is Node<T>) {
            listOf(value) + left.preorderValues() + right.preorderValues()
        } else {
            throw UnknownTreeImplementation(this)
        }

fun <T> Tree<T>.inorderValues(): List<T> =
        if (this == End) emptyList<T>()
        else if (this is Node<T>) {
            left.inorderValues() + listOf(value) + right.inorderValues()
        } else {
            throw UnknownTreeImplementation(this)
        }

//fun <T> createTree(preoreder: List<T>, inorder: List<T>): Tree<T> {
//
//}


class P68Test {
    @Test fun `pre-order list of tree values`() {
        assertThat(Node("a").preorderValues(), equalTo(listOf("a")))
        assertThat(Node("a", Node("b"), Node("c")).preorderValues().joinToString(""), equalTo(
                "abc"
        ))
        assertThat("a(b(d,e),c(,f(g,)))".convertToTree().preorderValues().joinToString(""), equalTo(
                "abdecfg"
        ))
    }

    @Test fun `in-order list of tree values`() {
        assertThat(Node("a").inorderValues(), equalTo(listOf("a")))
        assertThat(Node("a", Node("b"), Node("c")).inorderValues().joinToString(""), equalTo(
                "bac"
        ))
        assertThat("a(b(d,e),c(,f(g,)))".convertToTree().inorderValues().joinToString(""), equalTo(
                "dbeacgf"
        ))
    }
}