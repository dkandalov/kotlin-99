package org.kotlin99.misc

import org.junit.Assert.assertTrue
import org.junit.Test


fun Int.toWords(): String {
    fun String.postfix(other: String) = if (this == "zero") "" else this + other
    fun List<String>.joinNonEmpty() = filter(String::isNotEmpty).joinToString(" ")

    fun convertMillions(n: Int) = (n / 1000000).toWords().postfix(" million")
    fun convertThousands(n: Int) = (n / 1000).toWords().postfix(" thousand")
    fun convertHundreds(n: Int) = (n / 100).toWords().postfix(" hundred")
    fun convertTensAndOnes(n: Int): String {
        return when {
            n < 10      -> ones[n]
            n in 10..19 -> teens[n - 10]
            else        -> listOf(tens[n / 10], ones[n % 10]).joinNonEmpty()
        }
    }

    if (this == 0) return "zero"
    return listOf(
        convertMillions(this),
        convertThousands(this % 1000000),
        convertHundreds(this % 1000),
        convertTensAndOnes(this % 100)
    ).joinNonEmpty()
}

private val ones = listOf("", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
private val teens = listOf("ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen")
private val tens = listOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")


class P95Test {
    @Test fun `convert number to words`() {
        val actual = listOf(
            0, 1, 2, 7, 10, 11, 12, 13, 15, 19, 20, 21, 25, 29, 30, 35, 50, 55, 69, 70, 99,
            100, 101, 119, 510, 900, 1000, 5001, 5019, 5555, 10000, 11000, 100000, 123456, 199001, 1000000,
            1111111, 190000009
        ).map { it.toString() + " - " + it.toWords() }

        val expected = """
            |0 - zero
            |1 - one
            |2 - two
            |7 - seven
            |10 - ten
            |11 - eleven
            |12 - twelve
            |13 - thirteen
            |15 - fifteen
            |19 - nineteen
            |20 - twenty
            |21 - twenty one
            |25 - twenty five
            |29 - twenty nine
            |30 - thirty
            |35 - thirty five
            |50 - fifty
            |55 - fifty five
            |69 - sixty nine
            |70 - seventy
            |99 - ninety nine
            |100 - one hundred
            |101 - one hundred one
            |119 - one hundred nineteen
            |510 - five hundred ten
            |900 - nine hundred
            |1000 - one thousand
            |5001 - five thousand one
            |5019 - five thousand nineteen
            |5555 - five thousand five hundred fifty five
            |10000 - ten thousand
            |11000 - eleven thousand
            |100000 - one hundred thousand
            |123456 - one hundred twenty three thousand four hundred fifty six
            |199001 - one hundred ninety nine thousand one
            |1000000 - one million
            |1111111 - one million one hundred eleven thousand one hundred eleven
            |190000009 - one hundred ninety million nine
        """.trimMargin().trim().split("\n")

        val mismatches = actual.zip(expected).filter { it.first != it.second }
        mismatches.forEach(::println)
        assertTrue(mismatches.isEmpty())
    }
}