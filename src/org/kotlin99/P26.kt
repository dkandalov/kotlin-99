package org.kotlin99

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.describe
import com.natpryce.hamkrest.equalTo
import org.junit.Test

fun <T> combinations(n: Int, list: List<T>): List<List<T>> =
        if (n == 0) listOf(emptyList())
        else list.flatMapTails { subList ->
            combinations(n - 1, subList.drop(1)).map{ (it + subList.first()) }
        }

private fun <T> List<T>.flatMapTails(f: (List<T>) -> (List<List<T>>)): List<List<T>> =
        if (isEmpty()) emptyList()
        else f(this) + this.drop(1).flatMapTails(f)


class P26Test {
    @Test fun `generate the combinations of K distinct objects chosen from the N elements of a list`() {
        assertThat(combinations(0, "abc".toList()), equalTo(listOf(emptyList())))
        assertThat(combinations(1, "abc".toList()), hasSameElementsAs(listOf(
                "a".toList(), "b".toList(), "c".toList()
        )))
        assertThat(combinations(2, "abc".toList()), hasSameElementsAs(listOf(
                "cb".toList(), "ab".toList(), "ca".toList()
        )))
        assertThat(combinations(3, "abc".toList()), hasSameElementsAs(listOf(
                "cba".toList()
        )))

        assertThat(combinations(3, "abcde".toList()), hasSameElementsAs(listOf(
                "cba".toList(), "dba".toList(), "eba".toList(),
                "dca".toList(), "eca".toList(), "eda".toList(),
                "dcb".toList(), "ecb".toList(), "edb".toList(),
                "edc".toList()
        )))
        assertThat(combinations(3, "abcdefghijkl".toList()).size, equalTo(220))
    }
}

class SameElementsMatcherTest {
    @Test fun `non-nested lists`() {
        assertThat(emptyList<Int>(), hasSameElementsAs(emptyList()))
        assertThat(listOf(1), hasSameElementsAs(listOf(1)))

        assertThat(listOf(1, 1), !hasSameElementsAs(listOf(1)))
        assertThat(listOf(1, 2), hasSameElementsAs(listOf(2, 1)))

        assertThat(listOf(1, 2, 3), hasSameElementsAs(listOf(3, 2, 1)))
        assertThat(listOf(1, 2, 3), !hasSameElementsAs(listOf(4, 2, 1)))
    }

    @Test fun `nested lists`() {
        assertThat(listOf(emptyList<Int>()), hasSameElementsAs(listOf(emptyList())))
        assertThat(listOf(listOf(1)), hasSameElementsAs(listOf(listOf(1))))

        assertThat(listOf(listOf(1, 1)), !hasSameElementsAs(listOf(listOf(1))))
        assertThat(listOf(listOf(1, 2)), hasSameElementsAs(listOf(listOf(2, 1))))

        assertThat(listOf(listOf(1, 2, 3), listOf(1)), hasSameElementsAs(listOf(listOf(1), listOf(3, 2, 1))))
        assertThat(listOf(listOf(1, 2, 4), listOf(1)), hasSameElementsAs(listOf(listOf(1), listOf(4, 2, 1))))
    }
}

fun <T> hasSameElementsAs(expected: Iterable<T>) : Matcher<Iterable<T>> {
    return object : Matcher.Primitive<Iterable<T>>() {
        override fun invoke(actual: Iterable<T>): MatchResult {
            val actualList = actual.toList()
            val expectedList = expected.toList()
            val isMatch =
                if (actualList.size != expectedList.size) {
                    false
                } else if (actualList.isNotEmpty() && actualList.first() is Iterable<*> && expectedList.first() is Iterable<*>) {
                    actualList.all { actualValue ->
                        expectedList.any{ expectedValue ->
                            hasSameElementsAs(expectedValue as Iterable<*>).invoke(actualValue as Iterable<*>) == MatchResult.Match
                        }
                    }
                } else {
                    actualList.toSet() == expectedList.toSet()
                }
            return if (isMatch) MatchResult.Match else MatchResult.Mismatch("was ${describe(actual)}")
        }
        override val description: String get() = "has the same elements as ${describe(expected)}"
        override val negatedDescription : String get() = "element are not the same as in ${describe(expected)}"
    }
}
