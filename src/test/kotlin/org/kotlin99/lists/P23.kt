package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.*

fun <T> randomSelect(n: Int, list: List<T>, random: Random = Random()): List<T> =
    if (n == 0) emptyList()
    else {
        val value = list[random.nextInt(list.size)]
        randomSelect(n - 1, list.filter { it != value }, random) + value
    }

class P23Test {
    @Test fun `extract a given number of randomly selected elements from a list`() {
        assertThat(randomSelect(3, "abcdefgh".toList(), Random(123)), equalTo("chf".toList()))
    }
}

