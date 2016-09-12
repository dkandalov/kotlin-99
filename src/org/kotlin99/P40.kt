package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun Int.goldbach(): Pair<Int, Int> {
    val prime = listPrimesInRange(2..this - 1).find { (this - it).isPrime() } ?: throw IllegalStateException()
    return Pair(prime, this - prime)
}

class P39Test {
    @Test fun `Goldbach's conjecture`() {
        assertThat(4.goldbach(), equalTo(Pair(2, 2)))
        assertThat(28.goldbach(), equalTo(Pair(5, 23)))
    }
}
