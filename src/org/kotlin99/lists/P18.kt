package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> slice(from: Int, to: Int, list: List<T>): List<T> =
        if (from > 0) slice(from - 1, to - 1, list.drop(1))
        else if (to > 0) listOf(list.first()) + slice(from, to - 1, list.drop(1))
        else emptyList()

fun <T> slice_(from: Int, to: Int, list: List<T>) = list.subList(from, to)

class P18Test {
    @Test fun `extract a slice from a list`() {
        assertThat(slice(3, 7, "abcdefghijk".toList()), equalTo("defg".toList()))
    }
}
