package org.kotlin99.arithmetic

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun Int.goldbach(): Pair<Int, Int> {
    if (this == 2) return Pair(1, 1)
    if (this == 3) return Pair(1, 2)
    val prime = listPrimesInRange(2 until this).find { (this - it).isPrime() } ?: throw IllegalStateException()
    return Pair(prime, this - prime)
}

class P40Test {
    @Test fun `Goldbach's conjecture`() {
        assertThat(4.goldbach(), equalTo(Pair(2, 2)))
        assertThat(28.goldbach(), equalTo(Pair(5, 23)))
    }
}
