package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

@Suppress("UNCHECKED_CAST")
fun flatten(list: List<Any>): List<Any> =
    list.flatMap {
        if (it is List<*>) flatten(it as List<Any>) else listOf(it)
    }

class P07Test {
    @Test fun `flatten a nested list structure`() {
        assertThat(flatten(listOf(1)), equalTo(listOf<Any>(1)))
        assertThat(flatten(listOf(1, listOf(2))), equalTo(listOf<Any>(1, 2)))
        assertThat(
            flatten(listOf(listOf(1, 1), 2, listOf(3, listOf(5, 8)))),
            equalTo(listOf<Any>(1, 1, 2, 3, 5, 8))
        )
    }
}