package org.kotlin99.arithmetic

import org.junit.Assert.assertTrue
import org.junit.Test

fun Int.isCoprimeTo(n: Int) = gcd(this, n) == 1

class P33Test {
    @Test fun `determine whether two positive integer numbers are coprime`() {
        assertTrue(35.isCoprimeTo(64))
    }
}
