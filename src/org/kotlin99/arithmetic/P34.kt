package org.kotlin99.arithmetic

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun Int.totient() = (1..this).filter { it.isCoprimeTo(this) }.size

class P34Test {
    @Test fun `calculate Euler's totient function`() {
        assertThat(10.totient(), equalTo(4))
        assertThat(10090.totient(), equalTo(4032))
    }
}
