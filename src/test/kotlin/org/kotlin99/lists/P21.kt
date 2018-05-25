package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> insertAt(value: T, n: Int, list: List<T>): List<T> =
    list.take(n) + listOf(value) + list.drop(n)

class P21Test {
    @Test fun `insert an element at a given position into a list`() {
        assertThat(insertAt('X', 1, "abcd".toList()), equalTo("aXbcd".toList()))
    }
}
