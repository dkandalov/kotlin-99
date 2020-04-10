package org.kotlin99.arithmetic

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.tail
import kotlin.math.pow

fun Int.totient2(): Int {
    fun totient(primeFactors: List<Pair<Int, Int>>): Double =
        if (primeFactors.isEmpty()) 1.0
        else {
            val (p, m) = primeFactors.first()
            (p - 1) * p.toDouble().pow(m.toDouble() - 1) * totient(primeFactors.tail())
        }
    return totient(this.primeFactorMultiplicity()).toInt()
}

class P37Test {
    @Test fun `calculate Euler's totient function (improved)`() {
        assertThat(10.totient2(), equalTo(4))
        assertThat(10090.totient2(), equalTo(4032))
    }
}
