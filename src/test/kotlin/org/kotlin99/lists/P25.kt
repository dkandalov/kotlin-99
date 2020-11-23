package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import kotlin.random.Random

fun <T> randomPermute(list: List<T>, random: Random = Random): List<T> =
    randomSelect(list.size, list, random)

class P25Test {
    @Test fun `generate a random permutation of the elements of a list`() {
        assertThat(randomPermute("abcdef".toList(), Random(seed = 123)), equalTo("edbfac".toList()))
    }
}
