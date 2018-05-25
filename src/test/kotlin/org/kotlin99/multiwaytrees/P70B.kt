package org.kotlin99.multiwaytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.*

/**
 * <tree> ::= <value> { <tree> } "^"
 */
fun String.convertToMTree(): MTree<Char> = convertToMTree(0).first

private fun String.convertToMTree(position: Int): Pair<MTree<Char>, Int> {
    val value = this[position]
    val children = ArrayList<MTree<Char>>()
    var i = position + 1
    while (this[i] != '^') {
        val it = convertToMTree(i)
        children.add(it.first)
        i = it.second
    }
    return Pair(MTree(value, children), i + 1)
}

fun MTree<Char>.convertToString(): String =
    value.toString() + children.joinToString("") { it.convertToString() } + "^"


class P70BTest {
    @Test fun `construct multiway tree from string`() {
        assertThat("a^".convertToMTree(), equalTo(MTree('a')))
        assertThat("ab^c^^".convertToMTree(), equalTo(MTree('a', MTree('b'), MTree('c'))))
        assertThat("afg^^c^bd^e^^^".convertToMTree(), equalTo(
            MTree('a',
                  MTree('f', MTree('g')),
                  MTree('c'),
                  MTree('b', MTree('d'), MTree('e'))
            )
        ))
    }

    @Test fun `convert multiway tree to string`() {
        assertThat(MTree('a').convertToString(), equalTo("a^"))
        assertThat(MTree('a', MTree('b'), MTree('c')).convertToString(), equalTo("ab^c^^"))
        assertThat(
            MTree('a',
                  MTree('f', MTree('g')),
                  MTree('c'),
                  MTree('b', MTree('d'), MTree('e'))
            ).convertToString(),
            equalTo("afg^^c^bd^e^^^")
        )
    }
}