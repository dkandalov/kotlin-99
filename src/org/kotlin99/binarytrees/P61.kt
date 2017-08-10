package org.kotlin99.binarytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.binarytrees.Tree.End
import org.kotlin99.binarytrees.Tree.Node
import org.kotlin99.common.containsAll


fun Tree<*>.leafCount(): Int = leafValues().size

fun <T> Tree<T>.leafValues(): List<T> = nodes()
    .filter { it.left == End && it.right == End }
    .map { it.value }

fun <T> Tree<T>.internalValues(): List<T> = nodes()
    .filter { it.left != End || it.right != End }
    .map { it.value }


class P61Test {
    @Test fun `count tree leafs`() {
        assertThat(Node("x").leafCount(), equalTo(1))
        assertThat(Node("x", Node("x"), End).leafCount(), equalTo(1))
        assertThat(Node("x", Node("x"), Node("x")).leafCount(), equalTo(2))
    }

    @Test fun `collect leaf values into a list`() {
        assertThat(Node("a").leafValues(), equalTo(listOf("a")))
        assertThat(Node("a", Node("b"), End).leafValues(), equalTo(listOf("b")))
        assertThat(Node("a", Node("b"), Node("c")).leafValues(), containsAll("b", "c"))
        assertThat(Node("a", Node("b"), Node("c", Node("d"), Node("e"))).leafValues(), containsAll("b", "d", "e"))
    }

    @Test fun `collect internal values into a list`() {
        assertThat(Node("a").internalValues(), equalTo(emptyList()))
        assertThat(Node("a", Node("b"), End).internalValues(), equalTo(listOf("a")))
        assertThat(Node("a", Node("b"), Node("c")).internalValues(), equalTo(listOf("a")))
        assertThat(Node("a", Node("b"), Node("c", Node("d"), Node("e"))).internalValues(), containsAll("a", "c"))
    }
}