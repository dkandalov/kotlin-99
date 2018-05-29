package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> removeAt(n: Int, list: List<T>): Pair<List<T>, T> =
    Pair(list.filterIndexed { i, _ -> i != n }, list[n])

class P20Test {
    @Test fun `remove the Kth element from a list`() {
        assertThat(removeAt(1, "abcd".toList()), equalTo(Pair(listOf('a', 'c', 'd'), 'b')))
    }
}
