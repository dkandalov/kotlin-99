package org.kotlin99.misc

import com.natpryce.hamkrest.anyElement
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Ignore
import org.junit.Test

fun knightsTours(boardSize: Int, point: Point, tour: Tour = Tour(point)): List<Tour> {
    if (tour.size == boardSize * boardSize) return listOf(tour)
    return point.knightMoves(boardSize)
            .filterNot { tour.contains(it) }
            // .sortedBy { it.knightMoves(boardSize).filterNot{ tour.contains(it) }.size } // https://en.wikipedia.org/wiki/Knight%27s_tour#Warnsdorf.27s_rule
            .flatMap{ knightsTours(boardSize, it, tour + it) }
}

data class Point(val x: Int, val y: Int) {
    fun knightMoves(boardSize: Int): List<Point> {
        val shifts = listOf(
            Point(2, -1),
            Point(-2, -1),
            Point(1, -2),
            Point(-1, -2)
        )
        return (shifts.map{ this + it } + shifts.map{ this - it })
                .filter{ it.x.within(0, boardSize) && it.y.within(0, boardSize)}
    }
    operator fun plus(other: Point): Point = Point(x + other.x, y + other.y)
    operator fun minus(other: Point): Point = Point(x - other.x, y - other.y)
    override fun toString() = "($x,$y)"
}

data class Tour(val points: Collection<Point>) {
    constructor (vararg points: Point): this(points.toList())
    val size: Int get() = points.size
    fun contains(point: Point) = points.contains(point)
    infix operator fun plus(point: Point) = Tour(points + point)
}

fun Tour.isClosed(boardSize: Int): Boolean {
    return if (size <= 1) false
    else points.last().knightMoves(boardSize).contains(points.first())
}

private fun Int.within(from: Int, until: Int) = this >= from && this < until


class P91Test {
    @Test fun `knight's tours`() {
        assertThat(knightsTours(1, Point(0, 0)), equalTo(listOf(Tour(Point(0, 0)))))
        assertThat(knightsTours(2, Point(0, 0)), equalTo(emptyList()))
        assertThat(knightsTours(3, Point(0, 0)), equalTo(emptyList()))

        knightsTours(5, Point(0, 0)).let {
            assertThat(it.size, equalTo(304))
            assertThat(it.filter { it.isClosed(5) }.size, equalTo(0))
            assertThat(it, anyElement(equalTo(Tour(
                    Point(0, 0), Point(2, 1), Point(4, 0), Point(3, 2), Point(1, 1),
                    Point(3, 0), Point(4, 2), Point(3, 4), Point(1, 3), Point(0, 1),
                    Point(2, 0), Point(4, 1), Point(2, 2), Point(0, 3), Point(2, 4),
                    Point(4, 3), Point(3, 1), Point(1, 0), Point(0, 2), Point(1, 4),
                    Point(3, 3), Point(1, 2), Point(0, 4), Point(2, 3), Point(4, 4)
            ))))
        }
    }

    @Ignore("too slow")
    @Test fun `knight's tours for larger boards`() {
        knightsTours(6, Point(0, 0)).let {
            assertThat(it.size, equalTo(304))
        }
        assertThat(knightsTours(8, Point(0, 0)).size, equalTo(100))
    }
}