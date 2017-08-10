package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.*

fun <T> duplicateN(n: Int, list: List<T>): List<T> =
    list.flatMap { Collections.nCopies(n, it) }

class P15Test {
    @Test fun `duplicate the elements of a list a given number of times`() {
        assertThat(duplicateN(3, "abccd".toList()), equalTo("aaabbbccccccddd".toList()))
    }
}

