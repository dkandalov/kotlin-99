package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> last(list: List<T>): T = list[list.lastIndex]

class P01Test {
    @Test fun `find the last element of a list`() {
        assertThat(last(listOf(1, 1, 2, 3, 5, 8)), equalTo(8))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `last element in empty list`() {
        last(listOf<Int>())
    }
}