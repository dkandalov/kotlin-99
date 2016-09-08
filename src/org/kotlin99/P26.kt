package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> combinations(n: Int, list: List<T>): List<List<T>> =
        if (n == 0) listOf(emptyList())
        else list.flatMapTails { subList ->
            combinations(n - 1, subList.drop(1)).map{ (it + subList.first()) }
        }

private fun <T> List<T>.flatMapTails(f: (List<T>) -> (List<List<T>>)): List<List<T>> =
        if (isEmpty()) emptyList()
        else f(this) + this.drop(1).flatMapTails(f)


class P26Test {
    @Test fun `generate the combinations of K distinct objects chosen from the N elements of a list`() {
        assertThat(combinations(0, "abc".toList()), equalTo(listOf(emptyList())))
        assertThat(combinations(1, "abc".toList()), equalTo(listOf(
                "a".toList(), "b".toList(), "c".toList()
        )))
        assertThat(combinations(2, "abc".toList()), equalTo(listOf(
                "ba".toList(), "ca".toList(), "cb".toList()
        )))
        assertThat(combinations(3, "abc".toList()), equalTo(listOf(
                "cba".toList()
        )))

        assertThat(combinations(3, "abcde".toList()), equalTo(listOf(
                "cba".toList(), "dba".toList(), "eba".toList(),
                "dca".toList(), "eca".toList(), "eda".toList(),
                "dcb".toList(), "ecb".toList(), "edb".toList(),
                "edc".toList()
        )))
        assertThat(combinations(3, "abcdefghijkl".toList()).size, equalTo(220))
    }
}
