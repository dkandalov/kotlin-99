package org.kotlin99.arithmetic

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun gcd(a: Int, b: Int): Int =
    when {
        a == 0 -> b
        a > b  -> gcd(b, a)
        else   -> gcd(b % a, a)
    }

class P32Test {
    @Test fun `greatest common divisor of two positive integer numbers`() {
        assertThat(gcd(1, 2), equalTo(1))
        assertThat(gcd(4, 2), equalTo(2))
        assertThat(gcd(5, 13), equalTo(1))
        assertThat(gcd(36, 63), equalTo(9))
    }
}
