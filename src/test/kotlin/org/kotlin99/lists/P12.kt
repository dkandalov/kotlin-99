package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> decode(list: List<Pair<Int, T>>): List<T> = list.flatMap { (count, value) -> List(count) { value } }

class P12Test {
    @Test fun `decode a run-length encoded list`() {
        assertThat(decode(listOf(
            Pair(4, 'a'), Pair(1, 'b'), Pair(2, 'c'), Pair(2, 'a'), Pair(1, 'd'), Pair(4, 'e')
        )), equalTo("aaaabccaadeeee".toList()))
    }
}
