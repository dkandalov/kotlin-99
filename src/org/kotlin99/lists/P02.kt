package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> penultimate(list: List<T>) = list[list.lastIndex - 1]

class P02Test {
    @Test fun `find the last but one element of a list`() {
        assertThat(penultimate(listOf(1, 1, 2, 3, 5, 8)), equalTo(5))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `penultimate element in list smaller than 2`() {
        penultimate(listOf(1))
    }
}
