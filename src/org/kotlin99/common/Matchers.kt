package org.kotlin99.common

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.describe

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