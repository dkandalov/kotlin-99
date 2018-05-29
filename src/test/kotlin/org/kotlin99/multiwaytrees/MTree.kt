package org.kotlin99.multiwaytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

data class MTree<out T>(val value: T, val children: List<MTree<T>> = emptyList()) {

    constructor(value: T, vararg children: MTree<T>): this(value, children.asList())

    override fun toString(): String =
        if (children.isEmpty()) value.toString()
        else value.toString() + " {" + children.joinToString(", ") { it.toString() } + "}"
}

class P70Test {
    @Test fun `tree construction and string conversion`() {
        val tree =
            MTree("a",
                  MTree("f",
                        MTree("g")),
                  MTree("c"),
                  MTree("b",
                        MTree("d"), MTree("e")))
        assertThat(tree.toString(), equalTo("a {f {g}, c, b {d, e}}"))
    }
}