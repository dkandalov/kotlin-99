package org.kotlin99.arithmetic

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun Int.primeFactors(): List<Int> {
    if (this.isPrime()) return listOf(this)

    val primeFactor = (2..(this / 2))
        .filter { it.isPrime() }
        .find { this % it == 0 }

    return if (primeFactor == null) emptyList()
    else listOf(primeFactor) + (this / primeFactor).primeFactors()
}

class P35Test {
    @Test fun `determine prime factors of a given positive integer`() {
        assertThat((2..10).map { Pair(it, it.primeFactors()) }, equalTo(listOf(
            Pair(2, listOf(2)),
            Pair(3, listOf(3)),
            Pair(4, listOf(2, 2)),
            Pair(5, listOf(5)),
            Pair(6, listOf(2, 3)),
            Pair(7, listOf(7)),
            Pair(8, listOf(2, 2, 2)),
            Pair(9, listOf(3, 3)),
            Pair(10, listOf(2, 5))
        )))
        assertThat(315.primeFactors(), equalTo(listOf(3, 3, 5, 7)))
    }
}
