package org.kotlin99.arithmetic

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun Int.primeFactorMultiplicity(): List<Pair<Int, Int>> =
    this.primeFactors()
        .groupBy { it }
        .map { Pair(it.key, it.value.size) }

class P36Test {
    @Test fun `determine the prime factors of a given positive integer (2)`() {
        assertThat(315.primeFactorMultiplicity(), equalTo(listOf(Pair(3, 2), Pair(5, 1), Pair(7, 1))))
    }
}
