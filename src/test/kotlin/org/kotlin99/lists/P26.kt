package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.containsAll
import org.kotlin99.common.tail

fun <T> combinations(n: Int, list: List<T>): List<List<T>> =
    if (n == 0) listOf(emptyList())
    else list.flatMapTails { subList ->
        combinations(n - 1, subList.tail()).map { (it + subList.first()) }
    }

private fun <T> List<T>.flatMapTails(f: (List<T>) -> (List<List<T>>)): List<List<T>> =
    if (isEmpty()) emptyList()
    else f(this) + this.tail().flatMapTails(f)


class P26Test {
    @Test fun `generate the combinations of K distinct objects chosen from the N elements of a list`() {
        assertThat(combinations(0, "abc".toList()), equalTo(listOf(emptyList())))
        assertThat(combinations(1, "abc".toList()), containsAll(listOf(
            "a".toList(), "b".toList(), "c".toList()
        )))
        assertThat(combinations(2, "abc".toList()), containsAll(listOf(
            "cb".toList(), "ab".toList(), "ca".toList()
        )))
        assertThat(combinations(3, "abc".toList()), containsAll(listOf(
            "cba".toList()
        )))

        assertThat(combinations(3, "abcde".toList()), containsAll(listOf(
            "cba".toList(), "dba".toList(), "eba".toList(),
            "dca".toList(), "eca".toList(), "eda".toList(),
            "dcb".toList(), "ecb".toList(), "edb".toList(),
            "edc".toList()
        )))
        assertThat(combinations(3, "abcdefghijkl".toList()).size, equalTo(220))
    }
}
