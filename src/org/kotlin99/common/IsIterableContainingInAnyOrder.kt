package org.kotlin99.common

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.equalTo
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotlin99.common.IsIterableContainingInAnyOrder.Companion.containsInAnyOrder
import java.util.*


class IsIterableContainingInAnyOrder<in T>(private val matchers: Iterable<Matcher<T>>) : Matcher<Iterable<T>> {

    override fun invoke(actual: Iterable<T>): MatchResult {
        val matching = Matching(matchers)
        for (item in actual) {
            val matchResult = matching.matches(item)
            if (matchResult != MatchResult.Match) {
                return matchResult
            }
        }
        return matching.isFinished(actual)
    }

    override val description: String
        get() = "iterable with items [${matchers.joinToString{ it.description }}] in any order"


    private class Matching<in S>(matchers: Iterable<Matcher<S>>) {
        private val matchers = ArrayList(matchers.toList())

        fun matches(item: S): MatchResult {
            if (matchers.isEmpty()) {
                return MatchResult.Mismatch("no match for: $item")
            }
            for (matcher in matchers) {
                if (matcher.invoke(item) == MatchResult.Match) {
                    matchers.remove(matcher)
                    return MatchResult.Match
                }
            }
            return MatchResult.Mismatch("not matched: $item")
        }

        fun isFinished(items: Iterable<S>): MatchResult {
            if (matchers.isEmpty()) {
                return MatchResult.Match
            }
            return MatchResult.Mismatch("no item matches: [${matchers.joinToString(", "){ it.description }}] in [${items.joinToString()}]")
        }
    }

    companion object {
        fun <T> containsInAnyOrder(vararg itemMatchers: Matcher<T>): Matcher<Iterable<T>> {
            return containsInAnyOrder(itemMatchers.toList())
        }

        fun <T> containsInAnyOrder(vararg items: T): Matcher<Iterable<T>> {
            return containsInAnyOrder(items.map{ equalTo(it) })
        }

        @Suppress("UNCHECKED_CAST")
        @JvmName("containsInAnyOrderEqualTo")
        fun <T> containsInAnyOrder(items: Iterable<T>, leafMatcher: (T) -> Matcher<T> = { equalTo(it) }): Matcher<Iterable<T>> {
            val matchers = if (items.count() > 0 && items.first() is Iterable<*>)
                items.map{ containsInAnyOrder(it as Iterable<*>) as Matcher<T> } else items.map { leafMatcher(it) }
            return containsInAnyOrder(matchers)
        }

        fun <T> containsInAnyOrder(itemMatchers: Iterable<Matcher<T>>): Matcher<Iterable<T>> {
            return IsIterableContainingInAnyOrder(itemMatchers)
        }
    }
}


class IsIterableContainingInAnyOrderTest {

    @Test fun `matches empty lists`() {
        assertMatches("empty list", containsInAnyOrder(emptyList()), emptyList<Int>())
    }

    @Test fun `matches single item iterable`() {
        assertMatches("single item", containsInAnyOrder(1), listOf(1))
        assertMatches("single item", containsInAnyOrder(equalTo(1)), listOf(1))
    }

    @Test fun `does not match empty`() {
        assertMismatchDescription("no item matches: [is equal to 1, is equal to 2] in []", containsInAnyOrder(1, 2), emptyList())
    }

    @Test fun `matches iterable out of order`() {
        assertMatches("Out of order", containsInAnyOrder(1, 2), listOf(2, 1))
    }

    @Test fun `matches iterable out of order with duplicates`() {
        assertMatches("Out of order", containsInAnyOrder(1, 2, 2), listOf(2, 2, 1))
    }

    @Test fun `matches iterable in order`() {
        assertMatches("In order", containsInAnyOrder(1, 2), listOf(1, 2))
    }

    @Test fun `does not match if one of multiple elements mismatches`() {
        assertMismatchDescription("not matched: 4", containsInAnyOrder(1, 2, 3), listOf(1, 2, 4))
    }

    @Test fun `does not match if there is different amount if duplicate elements`() {
        assertMismatchDescription("not matched: 1", containsInAnyOrder(1, 2, 2), listOf(2, 1, 1))
    }

    @Test fun `does not match if there are more elements than matchers`() {
        assertMismatchDescription("not matched: 2", containsInAnyOrder(1, 3), listOf(1, 2, 3))
    }

    @Test fun `does not match if there are more matchers than elements`() {
        assertMismatchDescription("no item matches: [is equal to 4] in [1, 2, 3]", containsInAnyOrder(1, 2, 3, 4), listOf(1, 2, 3))
    }

    @Test fun `matches nested empty lists`() {
        assertMatches("nested empty list", containsInAnyOrder(listOf(emptyList<Int>())), listOf(emptyList()))
    }

    @Test fun `matches nested single item lists`() {
        assertMatches("single item", containsInAnyOrder(listOf(listOf(1))), listOf(listOf(1)))
    }

    @Test fun `matches nested lists with multiple elements`() {
        assertMatches("multiple items", containsInAnyOrder(listOf(listOf(2, 1))), listOf(listOf(1, 2)))
        assertMatches("multiple items", containsInAnyOrder(listOf(listOf(2, 1, 1))), listOf(listOf(1, 1, 2)))
        assertMatches("multiple items", containsInAnyOrder(listOf(listOf(1), listOf(3, 2, 1))), listOf(listOf(1, 2, 3), listOf(1)))
        assertMatches("multiple items", containsInAnyOrder(listOf(listOf(1), listOf(4, 2, 1))), listOf(listOf(1, 2, 4), listOf(1)))
    }

    @Test fun `has a readable description`() {
        assertEquals("iterable with items [is equal to 1, is equal to 2] in any order", containsInAnyOrder(1, 2).description)
    }

    private fun <T> assertMatches(message: String, matcher: Matcher<T>, arg: T) {
        val result = matcher.invoke(arg)
        if (result is MatchResult.Mismatch) {
            Assert.fail("$message because: '${result.description}'")
        }
    }

    private fun <T> assertMismatchDescription(expected: String, matcher: Matcher<T>, arg: T) {
        val matchResult = matcher.invoke(arg)
        assertTrue("Precondition: Matcher should not match item.", matchResult is MatchResult.Mismatch)
        matchResult as MatchResult.Mismatch
        assertEquals("Expected mismatch description", expected, matchResult.description)
    }
}