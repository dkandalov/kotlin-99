package org.kotlin99.lists

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.*

fun lotto(n: Int, k: Int, random: Random = Random()): List<Int> =
    randomSelect(n, (1..k).toList(), random)

class P24Test {
    @Test fun `draw N different random numbers from the set 1 to M`() {
        assertThat(lotto(3, 49, Random(123)), equalTo(listOf(32, 28, 8)))
    }
}
