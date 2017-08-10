package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> encodeDirect(list: List<T>): List<Pair<Int, T>> =
    list.fold(emptyList()) { result, value ->
        if (result.isEmpty()) listOf(Pair(1, value))
        else {
            val last = result.last()
            if (last.second == value) result.dropLast(1) + Pair(last.first + 1, value)
            else result + Pair(1, value)
        }
    }

class P13Test {
    @Test fun `run-length encoding of a list (direct solution)`() {
        assertThat(encodeDirect("aaaabccaadeeee".toList()), equalTo(listOf(
            Pair(4, 'a'), Pair(1, 'b'), Pair(2, 'c'), Pair(2, 'a'), Pair(1, 'd'), Pair(4, 'e')
        )))
    }
}
