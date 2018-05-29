package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> encode(list: List<T>): List<Pair<Int, T>> =
    pack(list).map { Pair(it.size, it.first()) }

class P10Test {
    @Test fun `run-length encoding of a list`() {
        assertThat(encode("aaaabccaadeeee".toList()), equalTo(listOf(
            Pair(4, 'a'), Pair(1, 'b'), Pair(2, 'c'), Pair(2, 'a'), Pair(1, 'd'), Pair(4, 'e')
        )))
    }
}