package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotlin99.common.containsAll

fun List<Int>.findValidEquations(): Sequence<Equal> {
    if (size <= 1) return emptySequence()
    val operators = sequenceOf(::Add, ::Subtract, ::Multiply, ::Divide)
    return allSplits()
        .flatMap { (left, right) ->
            left.operatorCombinations(operators).flatMap { leftExpr ->
                right.operatorCombinations(operators).map { rightExpr ->
                    Equal(leftExpr, rightExpr)
                }
            }
        }
        .filter(Equal::evaluate)
}

private fun List<Int>.operatorCombinations(operators: Sequence<(Expr<Int>, Expr<Int>) -> Expr<Int>>): Sequence<Expr<Int>> {
    if (size == 0) return emptySequence()
    if (size == 1) return sequenceOf(Number(first()))
    return allSplits().flatMap { (left, right) ->
        left.operatorCombinations(operators).flatMap { leftExpr ->
            right.operatorCombinations(operators).flatMap { rightExpr ->
                operators.map { operator ->
                    operator(leftExpr, rightExpr)
                }
            }
        }
    }
}

private fun <T> List<T>.allSplits(i: Int = 1): Sequence<Pair<List<T>, List<T>>> {
    if (size <= i) return emptySequence()
    val pair = Pair(subList(0, i), subList(i, size))
    return sequenceOf(pair) + allSplits(i + 1)
}


interface Expr<out T> {
    fun evaluate(): T
    override fun toString(): String
}

data class Number(val n: Int): Expr<Int> {
    override fun evaluate() = n
    override fun toString() = "$n"
}

data class Add(val left: Expr<Int>, val right: Expr<Int>): Expr<Int> {
    override fun evaluate() = left.evaluate() + right.evaluate()
    override fun toString() = "($left + $right)"
}

data class Subtract(val left: Expr<Int>, val right: Expr<Int>): Expr<Int> {
    override fun evaluate() = left.evaluate() - right.evaluate()
    override fun toString() = "($left - $right)"
}

data class Multiply(val left: Expr<Int>, val right: Expr<Int>): Expr<Int> {
    override fun evaluate() = left.evaluate() * right.evaluate()
    override fun toString() = "($left * $right)"
}

data class Divide(val left: Expr<Int>, val right: Expr<Int>): Expr<Int> {
    override fun evaluate(): Int {
        val rightValue = right.evaluate()
        return if (rightValue == 0) Int.MAX_VALUE else left.evaluate() / rightValue
    }

    override fun toString() = "($left / $right)"
}

data class Equal(val left: Expr<Int>, val right: Expr<Int>): Expr<Boolean> {
    override fun evaluate() = left.evaluate() == right.evaluate()
    override fun toString() = "$left = $right"
}


class P93Test {
    @Test fun `find all valid equations`() {
        assertThat(listOf(1, 2).findValidEquations().toList(), equalTo(emptyList()))
        assertThat(listOf(1, 2, 3).findValidEquations().toList(), equalTo(listOf(
            Equal(1() + 2(), 3())
        )))
        assertThat(listOf(10, 2, 5).findValidEquations().toList(), equalTo(listOf(
            Equal(10(), 2() * 5()),
            Equal(10() / 2(), 5())
        )))
    }

    @Test fun `find all valid (unsimplified) equations with the example from readme`() {
        val equations = listOf(2, 3, 5, 7, 11).findValidEquations().toList()
        assertTrue(equations.contains(Equal(2() - 3() + 5() + 7(), 11())))
        assertTrue(equations.contains(Equal(2(), (3() * 5() + 7()) / 11())))
        assertThat(equations.size, equalTo(31))
    }

    @Test fun `generating operator combinations`() {
        val operators = sequenceOf(::Add, ::Subtract)
        assertThat(listOf(1, 2).operatorCombinations(operators).toList(), equalTo(listOf(
            1() + 2(),
            1() - 2()
        )))
        assertThat(listOf(1, 2, 3).operatorCombinations(operators).toList(), containsAll(listOf(
            1() + (2() + 3()),
            1() + (2() - 3()),
            1() - (2() + 3()),
            1() - (2() - 3()),
            (1() + 2()) + 3(),
            (1() + 2()) - 3(),
            (1() - 2()) + 3(),
            (1() - 2()) - 3()
        )))
    }

    @Test fun `all splits of a list`() {
        assertThat(listOf(1, 2, 3).allSplits().toList(), equalTo(listOf(
            Pair(listOf(1), listOf(2, 3)),
            Pair(listOf(1, 2), listOf(3))
        )))
    }

    @Test fun `expression conversion to string`() {
        val expression = Equal(
            Divide(Number(25), Number(8)),
            Add(Number(1), Multiply(Number(2), Number(5)))
        )
        assertThat(expression.toString(), equalTo("(25 / 8) = (1 + (2 * 5))"))
    }

    @Test fun `expression evaluation`() {
        assertTrue(Equal(Number(1), Number(1)).evaluate())
        assertFalse(Equal(Number(1), Number(2)).evaluate())

        assertTrue(Equal(Add(Number(1), Number(2)), Number(3)).evaluate())
        assertTrue(Equal(Subtract(Number(1), Number(2)), Number(-1)).evaluate())
        assertTrue(Equal(Multiply(Number(2), Number(5)), Number(10)).evaluate())
        assertTrue(Equal(Divide(Number(25), Number(8)), Number(3)).evaluate())

        assertTrue(Equal(Divide(Number(25), Number(8)), Add(Number(1), Number(2))).evaluate())
    }

    private operator fun Int.invoke() = Number(this)
    private operator fun Expr<Int>.plus(that: Expr<Int>): Expr<Int> = Add(this, that)
    private operator fun Expr<Int>.minus(that: Expr<Int>): Expr<Int> = Subtract(this, that)
    private operator fun Expr<Int>.times(that: Expr<Int>): Expr<Int> = Multiply(this, that)
    private operator fun Expr<Int>.div(that: Expr<Int>): Expr<Int> = Divide(this, that)
}