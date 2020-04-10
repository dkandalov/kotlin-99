package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> lengthSort(listOfLists: List<List<T>>): List<List<T>> = listOfLists.sortedBy { it.size }

fun <T> lengthFreqSort(listOfLists: List<List<T>>): List<List<T>> {
    val groupedLists = listOfLists.groupBy { it.size }
    return listOfLists.sortedBy { groupedLists.getValue(it.size).size }
}


class P28Test {
    @Test fun `a) sort elements of the list according to their length`() {
        assertThat(
            lengthSort(listOf(
                "abc",
                "de",
                "fgh",
                "de",
                "ijkl",
                "mn",
                "o"
            ).map { it.toList() }),
            equalTo(listOf(
                "o",
                "de",
                "de",
                "mn",
                "abc",
                "fgh",
                "ijkl"
            ).map { it.toList() })
        )
    }

    @Test fun `b) sort elements according to their length frequency`() {
        assertThat(
            lengthFreqSort(listOf(
                "abc",
                "de",
                "fgh",
                "de",
                "ijkl",
                "mn",
                "o"
            ).map { it.toList() }),
            equalTo(listOf(
                "ijkl",
                "o",
                "abc",
                "fgh",
                "de",
                "de",
                "mn"
            ).map { it.toList() })
        )
    }
}
