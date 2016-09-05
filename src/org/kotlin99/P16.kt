package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> drop(n: Int, list: List<T>): List<T> = list
        .mapIndexed { i: Int, value: T -> Pair(i, value) }
        .filter { (it.first + 1) % n != 0 }
        .map { it.second }

class P16Test {
    @Test fun `drop every Nth element from a list`() {
        assertThat(drop(3, "abcdefghijk".toList()), equalTo("abdeghjk".toList()))
    }
}
