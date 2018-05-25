package org.kotlin99.multiwaytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> MTree<T>.postorder(): List<T> =
    children.flatMap { it.postorder() } + value


class P72Test {
    @Test fun `tree values in post-order`() {
        assertThat(MTree('a').postorderString(), equalTo("a"))
        assertThat(MTree('a', MTree('b'), MTree('c')).postorderString(), equalTo("bca"))
        assertThat(MTree('a', MTree('b'), MTree('c', MTree('d'))).postorderString(), equalTo("bdca"))
        assertThat("afg^^c^bd^e^^^".convertToMTree().postorderString(), equalTo("gfcdeba"))
    }

    private fun MTree<Char>.postorderString() = postorder().joinToString("")
}