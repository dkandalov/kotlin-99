package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun range(from: Int, to: Int): List<Int> = (from..to).toList()

class P22Test {
    @Test fun `create a list containing all integers within a given range`() {
        assertThat(range(4, 9), equalTo(listOf(4, 5, 6, 7, 8, 9)))
    }
}
