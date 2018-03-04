package org.kotlin99.arithmetic

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun goldbachList(range: IntRange) =
    range.filter { it % 2 == 0 }.map { it.goldbach() }

fun printGoldbachList(range: IntRange) =
    goldbachList(range).forEach { println(it.toStringSum()) }

fun goldbachListLimited(range: IntRange, minPrime: Int) =
    goldbachList(range).filter { it.first > minPrime && it.second > minPrime }

fun printGoldbachListLimited(range: IntRange, minPrime: Int) =
    goldbachListLimited(range, minPrime).forEach { println(it.toStringSum()) }

private fun Pair<Int, Int>.toStringSum() = "${first + second} = $first + $second"


class P41Test {
    @Test fun `list of Goldbach compositions`() {
        printGoldbachList(9..20)
        assertThat(goldbachList(9..20), equalTo(listOf(
            Pair(3, 7),
            Pair(5, 7),
            Pair(3, 11),
            Pair(3, 13),
            Pair(5, 13),
            Pair(3, 17)
        )))
    }

    @Test fun `limited list of Goldbach compositions`() {
        printGoldbachListLimited(2..3000, 50)
        assertThat(goldbachListLimited(2..3000, 50), equalTo(listOf(
            Pair(73, 919),
            Pair(61, 1321),
            Pair(67, 1789),
            Pair(61, 1867),
            Pair(61, 2017),
            Pair(61, 2377),
            Pair(53, 2459),
            Pair(53, 2477),
            Pair(61, 2557),
            Pair(103, 2539)
        )))
    }
}
