package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

data class MTree<out T>(val value: T, val children: List<MTree<T>> = emptyList()) {
    override fun toString(): String =
        if (children.isEmpty()) value.toString()
        else value.toString() + " {" + children.joinToString(", "){ it.toString() } + "}"
}

class P70Test {
    @Test fun `tree construction and string conversion`() {
        val tree =
                MTree("a", listOf(
                    MTree("f", listOf(
                        MTree("g"))),
                    MTree("c"),
                    MTree("b", listOf(
                        MTree("d"), MTree("e")))
            ))
        assertThat(tree.toString(), equalTo("a {f {g}, c, b {d, e}}"))
    }
}