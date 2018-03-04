package org.kotlin99.arithmetic

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun listPrimesInRange(range: IntRange): List<Int> = range.filter { it.isPrime() }

class P39Test {
    @Test fun `list of prime numbers`() {
        assertThat(listPrimesInRange(7..31), equalTo(listOf(7, 11, 13, 17, 19, 23, 29, 31)))
    }
}
