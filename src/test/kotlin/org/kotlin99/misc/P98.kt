package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Ignore
import org.junit.Test
import org.kotlin99.common.tail
import org.kotlin99.misc.Nonogram.Box
import org.kotlin99.misc.Nonogram.Companion.parse
import org.kotlin99.misc.Nonogram.Constraint
import java.util.*

@Suppress("unused") // Because this class is a "namespace".
class Nonogram {

    data class Board(
        private val width: Int,
        private val height: Int,
        private val rowConstrains: List<Constraint>,
        private val columnConstraints: List<Constraint>,
        private val cells: List<MutableList<Boolean>> = 1.rangeTo(height).map { MutableList(width) {false} }
    ) {

        fun solve(rowIndex: Int = 0): Sequence<Board> {
            if (hasContradiction()) return emptySequence()
            if (rowIndex == height) return sequenceOf(this)

            return rowConstrains.toList()[rowIndex]
                .possibleBoxes(width)
                .flatMap { rowBoxes ->
                    this.copy().apply(rowBoxes, rowIndex).solve(rowIndex + 1)
                }
        }

        private fun hasContradiction(): Boolean {
            fun Constraint.match(heights: List<Int>): Boolean {
                if (heights.size > boxes.size) return false
                if (heights.isEmpty()) return true
                if (heights.size == 1 && heights.first() <= boxes.first()) return true
                if (boxes.first() == heights.first()) return Constraint(boxes.tail()).match(heights.tail())
                return false
            }

            val boxHeights = 0.until(width).map { columnBoxHeights(it) }
            return !columnConstraints.zip(boxHeights).all { it.first.match(it.second) }
        }

        fun columnBoxHeights(column: Int): List<Int> {
            val columnCells = 0.until(height).map { cells[it][column] }

            val result = ArrayList<Int>()
            var lastCell = false
            var boxHeight = 0
            columnCells.forEach { cell ->
                if (lastCell && !cell && boxHeight != 0) {
                    result.add(boxHeight)
                    boxHeight = 0
                } else if (cell) {
                    boxHeight++
                }
                lastCell = cell
            }
            if (lastCell && boxHeight != 0) {
                result.add(boxHeight)
            }
            return result
        }

        private fun apply(boxes: List<Box>, rowIndex: Int): Board {
            boxes.forEach { (index, width) ->
                0.until(width).forEach {
                    cells[rowIndex][index + it] = true
                }
            }
            return this
        }

        private fun copy(): Board = Board(width, height, rowConstrains, columnConstraints, cells.map { ArrayList(it) })

        override fun toString(): String {
            val max = columnConstraints.map { it.boxes.size }.max()!!

            val rows = cells.mapIndexed { _, row ->
                "|" + row.joinToString("|") { if (it) "X" else "_" } + "|"
            }
            val constraints = 0.until(max).map { i ->
                " " + 0.until(width)
                    .map { columnConstraints[it] }
                    .joinToString(" ") { if (i < it.boxes.size) it.boxes[i].toString() else " " }
                    .trim()
            }
            return (rows.zip(rowConstrains).map { it.first + " " + it.second.boxes.joinToString(" ") } + constraints).joinToString("\n")
        }
    }

    data class Box(val index: Int, val width: Int)

    data class Constraint(val boxes: List<Int>) {
        constructor(vararg boxes: Int): this(boxes.toList())

        fun possibleBoxes(width: Int, startIndex: Int = 0): Sequence<List<Box>> {
            if (boxes.isEmpty()) return sequenceOf(emptyList())

            val endIndex = width - boxes.first() - boxes.tail().sumBy { it + 1 }
            if (startIndex > endIndex) return emptySequence()

            return startIndex.rangeTo(endIndex).asSequence().flatMap { i ->
                Constraint(boxes.tail()).possibleBoxes(width, i + boxes.first() + 1).map {
                    listOf(Box(i, boxes.first())) + it
                }
            }
        }
    }

    companion object {
        fun String.parse(): Board {
            fun List<List<Int>>.transpose(): List<List<Int>> {
                val max = maxBy { it.size }!!.size
                val result = ArrayList<List<Int>>()
                0.until(max).forEach { i ->
                    result.add(mapNotNull { list ->
                        if (i < list.size) list[i] else null
                    })
                }
                return result
            }

            val lines = split("\n")

            val cells = lines
                .takeWhile { it.startsWith("|") }
                .map { it.replace(Regex("[|]"), "").replace(Regex(" .*"), "") }
                .map { it.toCharArray().mapTo(ArrayList()) { char -> char != '_' } }

            val rowConstraints = lines
                .takeWhile { it.startsWith("|") }
                .map { it.replace(Regex("[|_X]"), "") }
                .map { it.trim().split(" ").map(String::toInt) }
                .map(::Constraint)

            val columnConstraints = lines
                .dropWhile { it.startsWith("|") }
                .map {
                    it.mapIndexedNotNull { i, char -> if (i % 2 == 1) char else null }
                        .map { char -> if (char == ' ') 0 else char.toString().toInt() }
                }
                .transpose()
                .map { it.dropLastWhile { it == 0 } }
                .map(::Constraint)

            val width = columnConstraints.size
            val height = rowConstraints.size
            return Board(width, height, rowConstraints, columnConstraints, cells)
        }
    }
}

class P98Test {

    @Test fun `all possibles boxes within constraint`() {
        Constraint(3).apply {
            assertThat(this.possibleBoxes(width = 2).toList(), equalTo(emptyList()))
            assertThat(this.possibleBoxes(width = 3).toList(), equalTo(listOf(
                listOf(Box(0, 3))
            )))
            assertThat(this.possibleBoxes(width = 5).toList(), equalTo(listOf(
                listOf(Box(0, 3)), listOf(Box(1, 3)), listOf(Box(2, 3))
            )))
        }

        Constraint(2, 1).apply {
            assertThat(this.possibleBoxes(width = 4).toList(), equalTo(listOf(
                listOf(Box(0, 2), Box(3, 1))
            )))
            assertThat(this.possibleBoxes(width = 6).toList(), equalTo(listOf(
                listOf(Box(0, 2), Box(3, 1)),
                listOf(Box(0, 2), Box(4, 1)),
                listOf(Box(0, 2), Box(5, 1)),
                listOf(Box(1, 2), Box(4, 1)),
                listOf(Box(1, 2), Box(5, 1)),
                listOf(Box(2, 2), Box(5, 1))
            )))
        }

        Constraint(3, 2).apply {
            assertThat(this.possibleBoxes(width = 7).toList(), equalTo(listOf(
                listOf(Box(0, 3), Box(4, 2)),
                listOf(Box(0, 3), Box(5, 2)),
                listOf(Box(1, 3), Box(5, 2))
            )))
        }
    }

    @Test fun `counting column boxes heights`() {
        val nonogram = """
            *|_|X|X|X|_|_|_|_| 3
            *|X|X|_|X|_|_|_|_| 2 1
            *|_|X|X|X|_|_|X|X| 3 2
            *|_|_|X|X|_|_|X|X| 2 2
            *|_|_|X|X|X|X|X|X| 6
            *|X|_|X|X|X|X|X|_| 1 5
            *|X|X|X|X|X|X|_|_| 6
            *|_|_|_|_|X|_|_|_| 1
            *|_|_|_|X|X|_|_|_| 2
            * 1 3 1 7 5 3 4 3
            * 2 1 5 1
        """.trimMargin("*").parse()

        assertThat(nonogram.columnBoxHeights(0), equalTo(listOf(1, 2)))
        assertThat(nonogram.columnBoxHeights(1), equalTo(listOf(3, 1)))
        assertThat(nonogram.columnBoxHeights(2), equalTo(listOf(1, 5)))
        assertThat(nonogram.columnBoxHeights(3), equalTo(listOf(7, 1)))
    }

    @Test fun `parse and convert back to string`() {
        val s = """
            *|_|X|X|X|_|_|_|_| 3
            *|X|X|_|X|_|_|_|_| 2 1
            *|_|X|X|X|_|_|X|X| 3 2
            *|_|_|X|X|_|_|X|X| 2 2
            *|_|_|X|X|X|X|X|X| 6
            *|X|_|X|X|X|X|X|_| 1 5
            *|X|X|X|X|X|X|_|_| 6
            *|_|_|_|_|X|_|_|_| 1
            *|_|_|_|X|X|_|_|_| 2
            * 1 3 1 7 5 3 4 3
            * 2 1 5 1
        """
        val nonogram = s.trimMargin("*").parse()

        assertThat(nonogram.toString(), equalTo(s.trimMargin("*")))
    }

    @Test fun `solve nonogram from readme`() {
        val nonogram = """
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

        assertThat(nonogram.solve().first(), equalTo("""
            *|_|X|X|X|_|_|_|_| 3
            *|X|X|_|X|_|_|_|_| 2 1
            *|_|X|X|X|_|_|X|X| 3 2
            *|_|_|X|X|_|_|X|X| 2 2
            *|_|_|X|X|X|X|X|X| 6
            *|X|_|X|X|X|X|X|_| 1 5
            *|X|X|X|X|X|X|_|_| 6
            *|_|_|_|_|X|_|_|_| 1
            *|_|_|_|X|X|_|_|_| 2
            * 1 3 1 7 5 3 4 3
            * 2 1 5 1
        """.trimMargin("*").parse()
        ))
    }

    @Ignore // because it's too slow for CI (~ 2 minutes 30 seconds)
    @Test fun `solve GCHQ Christmas Puzzle`() {
        // See http://makercasts.org/articles/gchq-christmas-puzzle
        val nonogram = """
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 7 3 1 1 7
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 1 2 2 1 1
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 3 1 3 1 1 3 1
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 3 1 1 6 1 3 1
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 3 1 5 2 1 3 1
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 1 2 1 1
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 7 1 1 1 1 1 7
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 3 3
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 2 3 1 1 3 1 1 2
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 1 3 2 1 1
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 4 1 4 2 1 2
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 1 1 1 1 4 1 3
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 2 1 1 1 2 5
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 3 2 2 6 3 1
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 9 1 1 2 1
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 2 1 2 2 3 1
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 3 1 1 1 1 5 1
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 2 2 5
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 7 1 2 1 1 1 3
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 1 2 1 2 2 1
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 3 1 4 5 1
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 3 1 3 10 2
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 3 1 1 6 6
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1 1 2 1 1 2
            *|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 7 2 1 2 5
            * 7 1 1 1 1 1 7 1 2 2 1 1 4 3 1 2 1 6 7 1 1 1 1 1 7
            * 2 1 3 3 3 1 1 1 1 2 7 2 1 3 2 2 3 2 1 1 3 3 3 1 1
            * 1 2 1 1 1 1 1 3 2 1 3 3 1 1 5 1 3 1 4 1 1 1 1 2 3
            * 1 2 3 1 1 2 1   1 2 2 1 2 1 2 1 2   1 1 3 1 4 2 2
            * 7 1 1 5 4 1 1   8 1 1 1 6 1 2 1 1   1 4 7 1 3 2 1
            *   1 3 1 1 1 1   2 1   1   3   1 8   3   1 2 3 6 1
            *     1 3 3   7   1 1   1   1   1 1         1   1
            *     3 1 1         2   1       2           1
            *     1                         2           4
        """.trimMargin("*").parse()

        assertThat(nonogram.solve().first(), equalTo("""
            *|X|X|X|X|X|X|X|_|X|X|X|_|_|_|X|_|X|_|X|X|X|X|X|X|X| 7 3 1 1 7
            *|X|_|_|_|_|_|X|_|X|X|_|X|X|_|_|_|_|_|X|_|_|_|_|_|X| 1 1 2 2 1 1
            *|X|_|X|X|X|_|X|_|_|_|_|_|X|X|X|_|X|_|X|_|X|X|X|_|X| 1 3 1 3 1 1 3 1
            *|X|_|X|X|X|_|X|_|X|_|_|X|X|X|X|X|X|_|X|_|X|X|X|_|X| 1 3 1 1 6 1 3 1
            *|X|_|X|X|X|_|X|_|_|X|X|X|X|X|_|X|X|_|X|_|X|X|X|_|X| 1 3 1 5 2 1 3 1
            *|X|_|_|_|_|_|X|_|_|X|X|_|_|_|_|_|_|_|X|_|_|_|_|_|X| 1 1 2 1 1
            *|X|X|X|X|X|X|X|_|X|_|X|_|X|_|X|_|X|_|X|X|X|X|X|X|X| 7 1 1 1 1 1 7
            *|_|_|_|_|_|_|_|_|X|X|X|_|_|_|X|X|X|_|_|_|_|_|_|_|_| 3 3
            *|X|_|X|X|_|X|X|X|_|_|X|_|X|_|X|X|X|_|X|_|_|X|_|X|X| 1 2 3 1 1 3 1 1 2
            *|X|_|X|_|_|_|_|_|_|X|X|X|_|X|X|_|_|_|_|X|_|_|_|X|_| 1 1 3 2 1 1
            *|_|X|X|X|X|_|X|_|X|X|X|X|_|X|X|_|X|_|_|_|_|X|X|_|_| 4 1 4 2 1 2
            *|_|X|_|X|_|_|_|X|_|_|_|X|_|X|_|X|X|X|X|_|X|_|X|X|X| 1 1 1 1 1 4 1 3
            *|_|_|X|X|_|_|X|_|X|_|X|_|_|_|_|_|_|X|X|_|X|X|X|X|X| 2 1 1 1 2 5
            *|_|_|_|X|X|X|_|X|X|_|X|X|_|X|X|X|X|X|X|_|X|X|X|_|X| 3 2 2 6 3 1
            *|X|_|X|X|X|X|X|X|X|X|X|_|X|_|X|_|_|X|X|_|_|_|_|X|_| 1 9 1 1 2 1
            *|_|X|X|_|X|_|_|X|X|_|_|X|X|_|_|X|X|X|_|_|_|_|_|X|_| 2 1 2 2 3 1
            *|X|X|X|_|X|_|X|_|X|_|_|_|_|X|_|_|X|X|X|X|X|_|X|_|_| 3 1 1 1 1 5 1
            *|_|_|_|_|_|_|_|_|X|_|_|X|X|_|_|X|X|_|_|_|X|X|X|X|X| 1 2 2 5
            *|X|X|X|X|X|X|X|_|X|_|_|_|X|X|_|_|X|_|X|_|X|_|X|X|X| 7 1 2 1 1 1 3
            *|X|_|_|_|_|_|X|_|X|X|_|_|X|_|_|X|X|_|_|_|X|X|_|X|_| 1 1 2 1 2 2 1
            *|X|_|X|X|X|_|X|_|_|_|X|X|X|X|_|_|X|X|X|X|X|_|_|X|_| 1 3 1 4 5 1
            *|X|_|X|X|X|_|X|_|X|X|X|_|X|X|X|X|X|X|X|X|X|X|_|X|X| 1 3 1 3 10 2
            *|X|_|X|X|X|_|X|_|X|_|_|X|X|X|X|X|X|_|X|X|X|X|X|X|_| 1 3 1 1 6 6
            *|X|_|_|_|_|_|X|_|_|X|X|_|_|_|_|_|_|X|_|X|_|X|X|_|_| 1 1 2 1 1 2
            *|X|X|X|X|X|X|X|_|X|X|_|_|_|X|_|X|X|_|_|_|X|X|X|X|X| 7 2 1 2 5
            * 7 1 1 1 1 1 7 1 2 2 1 1 4 3 1 2 1 6 7 1 1 1 1 1 7
            * 2 1 3 3 3 1 1 1 1 2 7 2 1 3 2 2 3 2 1 1 3 3 3 1 1
            * 1 2 1 1 1 1 1 3 2 1 3 3 1 1 5 1 3 1 4 1 1 1 1 2 3
            * 1 2 3 1 1 2 1   1 2 2 1 2 1 2 1 2   1 1 3 1 4 2 2
            * 7 1 1 5 4 1 1   8 1 1 1 6 1 2 1 1   1 4 7 1 3 2 1
            * 1 3 1 1 1 1   2 1   1   3   1 8   3   1 2 3 6 1
            * 1 3 3   7   1 1   1   1   1 1         1   1
            * 3 1 1         2   1       2           1
            * 1                         2           4
        """.trimMargin("*").parse()
        ))
    }
}