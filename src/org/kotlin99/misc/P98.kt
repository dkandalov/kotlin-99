package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.misc.Nonogram.Companion.parse
import java.util.*

@Suppress("unused") // Because this class is a "namespace".
class Nonogram {

    companion object {
        fun String.parse(): Pair<List<List<Int>>, List<List<Int>>> {
            fun List<List<Int>>.transpose(): List<List<Int>> {
                val max = maxBy{ it.size }!!.size
                val result = ArrayList<List<Int>>()
                0.rangeTo(max - 1).forEach { i ->
                    result.add(mapNotNull { list ->
                        if (i < list.size) list[i] else null
                    })
                }
                return result
            }

            val lines = split("\n")

            val rowConstraints = lines
                .takeWhile{ it.startsWith("|") }
                .map { it.replace(Regex("[|_]"), "") }
                .map { it.trim().split(" ").map(String::toInt) }

            val columnConstraints = lines
                .dropWhile{ it.startsWith("|") }
                .map { it.trim().split(" ").map(String::toInt) }
                .transpose()

            return Pair(rowConstraints, columnConstraints)
        }
    }
}

class P98Test {

    @Test fun `parse string as nonogram problem statement`() {
        val nonogramProblem = """
            *|_|_|_|_|_|_|_|_| 3
            *|_|_|_|_|_|_|_|_| 2 1
            *|_|_|_|_|_|_|_|_| 3 2
            *|_|_|_|_|_|_|_|_| 2 2
            *|_|_|_|_|_|_|_|_| 6
            *|_|_|_|_|_|_|_|_| 1 5
            *|_|_|_|_|_|_|_|_| 6
            *|_|_|_|_|_|_|_|_| 1
            *|_|_|_|_|_|_|_|_| 2
            * 1 3 1 7 5 3 4 3
            * 2 1 5 1
        """.trimMargin("*").parse()

        assertThat(nonogramProblem, equalTo(Pair(
            listOf(
                listOf(3),
                listOf(2, 1),
                listOf(3, 2),
                listOf(2, 2),
                listOf(6),
                listOf(1, 5),
                listOf(6),
                listOf(1),
                listOf(2)
            ),
            listOf(
                listOf(1, 2),
                listOf(3, 1),
                listOf(1, 5),
                listOf(7, 1),
                listOf(5),
                listOf(3),
                listOf(4),
                listOf(3)
            )
        )))
    }
}