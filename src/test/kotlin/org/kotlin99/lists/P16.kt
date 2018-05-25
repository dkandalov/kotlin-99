package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> drop(n: Int, list: List<T>): List<T> =
        list.filterIndexed { i, _ -> (i + 1) % n != 0 }

class P16Test {
    @Test fun `drop every Nth element from a list`() {
        assertThat(drop(3, "abcdefghijk".toList()), equalTo("abdeghjk".toList()))
    }
}
