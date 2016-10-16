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


class IsIterableContainingInAnyOrder<in T>(private val matchers: Collection<Matcher<T>>) : Matcher<Iterable<T>> {
    override fun invoke(actual: Iterable<T>): MatchResult {
        val matching = Matching(matchers)
        for (item in actual) {
            if (!matching.matches(item)) {
                return MatchResult.Mismatch(matching.mismatchDescription)
            }
        }
        return if (matching.isFinished(actual)) MatchResult.Match else MatchResult.Mismatch(matching.mismatchDescription)
    }

    override val description: String
        get() = "iterable with items [${matchers.joinToString{ it.description }}] in any order"


    private class Matching<in S>(matchers: Collection<Matcher<S>>, var mismatchDescription: String = "") {
        private val matchers: MutableCollection<Matcher<S>> = ArrayList(matchers)

        fun matches(item: S): Boolean {
            if (matchers.isEmpty()) {
                mismatchDescription += "no match for: $item"
                return false
            }
            return isMatched(item)
        }

        fun isFinished(items: Iterable<S>): Boolean {
            if (matchers.isEmpty()) {
                return true
            }
            mismatchDescription += "no item matches: [${matchers.joinToString(", "){ it.description }}] " +
                                   "in [${items.joinToString()}]"
            return false
        }

        private fun isMatched(item: S): Boolean {
            for (matcher in matchers) {
                if (matcher.invoke(item) == MatchResult.Match) {
                    matchers.remove(matcher)
                    return true
                }
            }
            mismatchDescription += "not matched: $item"
            return false
        }
    }

    companion object {
        fun <T> containsInAnyOrder(vararg itemMatchers: Matcher<T>): Matcher<Iterable<T>> {
            return containsInAnyOrder(itemMatchers.toList())
        }

        fun <T> containsInAnyOrder(vararg items: T): Matcher<Iterable<T>> {
            return IsIterableContainingInAnyOrder(items.map{ equalTo(it) })
        }

        fun <T> containsInAnyOrder(itemMatchers: Collection<Matcher<T>>): Matcher<Iterable<T>> {
            return IsIterableContainingInAnyOrder(itemMatchers)
        }
    }
}


class IsIterableContainingInAnyOrderTest {

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