package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> reverse(list: List<T>): List<T> {
    if (list.size <= 1) return list
    else return reverse(list.drop(1)) + list.first()
}

class P05Test {
    @Test fun `reverse a list`() {
        assertThat(reverse(listOf<Int>()), equalTo(emptyList()))
        assertThat(reverse(listOf(1)), equalTo(listOf(1)))
        assertThat(reverse(listOf(1, 1, 2, 3, 5, 8)), equalTo(listOf(8, 5, 3, 2, 1, 1)))
    }
}