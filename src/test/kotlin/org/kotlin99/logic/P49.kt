package org.kotlin99.logic

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun grayCodes(bits: Int): List<String> =
    if (bits == 0) listOf("")
    else {
        val codes = grayCodes(bits - 1)
        codes.map { "0$it" } + codes.asReversed().map { "1$it" }
    }

class P49Test {
    @Test fun `generate Gray code values`() {
        assertThat(grayCodes(bits = 1), equalTo(listOf("0", "1")))
        assertThat(grayCodes(bits = 2), equalTo(listOf("00", "01", "11", "10")))
        assertThat(grayCodes(bits = 3), equalTo(listOf("000", "001", "011", "010", "110", "111", "101", "100")))
    }
}
