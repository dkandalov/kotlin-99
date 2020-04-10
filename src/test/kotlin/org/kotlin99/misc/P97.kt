package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.misc.Sudoku.*
import org.kotlin99.misc.Sudoku.Board.Companion.size
import org.kotlin99.misc.Sudoku.Board.Companion.squareSize
import org.kotlin99.misc.Sudoku.Companion.toBoard
import org.kotlin99.misc.Sudoku.Point
import java.util.*

@Suppress("unused") // Because this class is a "namespace".
class Sudoku {

    data class Board(private val cells: MutableList<Cell> = MutableList(size * size){ Cell() }) {
        private val positionedCells: List<PositionedCell>
            get() = cells.mapIndexed { i, cell -> PositionedCell(Point(i % size, i / size), cell) }

        fun solve(): Sequence<Board> {
            optimizeGuesses()

            if (cells.any { it.isNotFilled() && it.guesses.isEmpty() }) return emptySequence()
            if (cells.all { it.isFilled() }) return sequenceOf(this)

            return positionedCells.find { it.cell.isNotFilled() }!!.let {
                it.cell.guesses.asSequence().flatMap { guess ->
                    copy().set(it.point.x, it.point.y, Cell(guess)).solve()
                }
            }
        }

        private fun optimizeGuesses() {
            fun cell(point: Point) = this[point.x, point.y]

            fun List<Point>.removeGuesses(value: Int) =
                filter { cell(it).isNotFilled() }
                    .forEach { set(it.x, it.y, cell(it).removeGuess(value)) }

            positionedCells
                .filter { it.cell.isFilled() }
                .forEach {
                    it.point.row().removeGuesses(it.cell.value!!)
                    it.point.column().removeGuesses(it.cell.value)
                    it.point.square().removeGuesses(it.cell.value)
                }
        }

        private fun copy(): Board = Board(ArrayList(cells))

        operator fun get(x: Int, y: Int): Cell = cells[x + y * size]

        operator fun set(x: Int, y: Int, cell: Cell): Board {
            cells[x + y * size] = cell
            return this
        }

        override fun toString(): String {
            fun <T> List<T>.slicedBy(sliceSize: Int): List<List<T>> =
                if (size <= sliceSize) listOf(this)
                else listOf(take(sliceSize)) + drop(sliceSize).slicedBy(sliceSize)

            fun <T> List<T>.mapJoin(separator: String, f: (T) -> String) = joinToString(separator) { f(it) }

            return positionedCells.slicedBy(size * squareSize).mapJoin("\n---+---+---\n") { section ->
                section.slicedBy(size).mapJoin("\n") { row ->
                    row.slicedBy(squareSize).mapJoin("|") { slice ->
                        slice.mapJoin("") {
                            if (it.cell.isFilled()) it.cell.value.toString() else "."
                        }
                    }
                }
            }
        }

        companion object {
            const val size = 9
            const val squareSize = size / 3
        }
    }

    private data class PositionedCell(val point: Point, val cell: Cell)

    data class Point(val x: Int, val y: Int) {
        fun row() = 0.until(size).map { Point(it, y) }

        fun column() = 0.until(size).map { Point(x, it) }

        fun square(): List<Point> {
            fun rangeOfSquare(coordinate: Int): IntRange {
                return (coordinate / squareSize).let {
                    (it * squareSize).until((it + 1) * squareSize)
                }
            }
            return rangeOfSquare(y).flatMap { cellY ->
                rangeOfSquare(x).map { cellX ->
                    Point(cellX, cellY)
                }
            }
        }
    }

    data class Cell(val value: Int?, val guesses: List<Int>) {
        constructor(): this(null, 1.rangeTo(size).toList())
        constructor(value: Int): this(value, emptyList())

        fun isFilled() = value != null
        fun isNotFilled() = !isFilled()
        fun removeGuess(value: Int) = if (guesses.isEmpty()) this else copy(guesses = guesses - value)
    }

    companion object {
        fun String.toBoard(): Board {
            val cells = replace(Regex("[|\\-+\n]"), "").mapTo(ArrayList()) { c ->
                if (c == '.') Cell() else Cell(c.toString().toInt())
            }
            return Board(cells)
        }
    }
}

class P97Test {
    @Test fun `solve sudoku example from readme`() {
        val board = """
            |..4|8..|.17
            |67.|9..|...
            |5.8|.3.|..4
            |---+---+---
            |3..|74.|1..
            |.69|...|78.
            |..1|.69|..5
            |---+---+---
            |1..|.8.|3.6
            |...|..6|.91
            |24.|..1|5..
        """.trimMargin().toBoard()

        assertThat(board.solve().first(), equalTo("""
            |934|825|617
            |672|914|853
            |518|637|924
            |---+---+---
            |325|748|169
            |469|153|782
            |781|269|435
            |---+---+---
            |197|582|346
            |853|476|291
            |246|391|578
        """.trimMargin().toBoard()))
    }

    @Test fun `solve medium sudoku from dailysudoku-dot-com`() {
        // http://dailysudoku.com/sudoku/archive/2016/11/2016-11-6_solution.shtml
        val board = """
            |6..|...|2.3
            |...|4.3|8..
            |.3.|7..|..9
            |---+---+---
            |...|.2.|1..
            |49.|...|.65
            |..6|.9.|...
            |---+---+---
            |1..|..5|.8.
            |..9|6..|...
            |8.4|...|..2
        """.trimMargin().toBoard()

        assertThat(board.solve().first(), equalTo("""
            |641|859|273
            |927|413|856
            |538|762|419
            |---+---+---
            |785|326|194
            |492|187|365
            |316|594|728
            |---+---+---
            |163|245|987
            |279|638|541
            |854|971|632
        """.trimMargin().toBoard()))
    }

    @Test fun `solve hard sudoku from dailysudoku-dot-com`() {
        // http://dailysudoku.com/sudoku/archive/2016/11/2016-11-5_solution.shtml
        val board = """
            |.7.|..1|...
            |19.|6.5|...
            |84.|.7.|.9.
            |---+---+---
            |9..|..8|5..
            |5.7|...|8.1
            |..1|5..|..2
            |---+---+---
            |.5.|.2.|.19
            |...|9.4|.86
            |...|1..|.5.
        """.trimMargin().toBoard()

        assertThat(board.solve().first(), equalTo("""
            |275|891|634
            |193|645|728
            |846|372|195
            |---+---+---
            |964|218|573
            |527|439|861
            |381|567|942
            |---+---+---
            |658|723|419
            |712|954|386
            |439|186|257
        """.trimMargin().toBoard()))
    }

    @Test fun `solve very hard sudoku from dailysudoku-dot-com`() {
        // http://dailysudoku.com/sudoku/archive/2016/11/2016-11-7_solution.shtml
        val board = """
            |.1.|...|..4
            |7.9|..1|..5
            |..5|9.7|..6
            |---+---+---
            |3.4|...|.2.
            |...|3.2|...
            |.7.|...|9.3
            |---+---+---
            |9..|8.6|4..
            |2..|5..|3.1
            |1..|...|.6.
        """.trimMargin().toBoard()

        assertThat(board.solve().first(), equalTo("""
            |816|235|794
            |729|461|835
            |435|987|216
            |---+---+---
            |384|659|127
            |591|372|648
            |672|148|953
            |---+---+---
            |953|816|472
            |267|594|381
            |148|723|569
        """.trimMargin().toBoard()))
    }

    @Test fun `square coordinates of a point`() {
        assertThat(Point(1, 2).square(), equalTo(listOf(
            Point(0, 0), Point(1, 0), Point(2, 0),
            Point(0, 1), Point(1, 1), Point(2, 1),
            Point(0, 2), Point(1, 2), Point(2, 2)
        )))
        assertThat(Point(0, 8).square(), equalTo(listOf(
            Point(0, 6), Point(1, 6), Point(2, 6),
            Point(0, 7), Point(1, 7), Point(2, 7),
            Point(0, 8), Point(1, 8), Point(2, 8)
        )))
        assertThat(Point(7, 7).square(), equalTo(listOf(
            Point(6, 6), Point(7, 6), Point(8, 6),
            Point(6, 7), Point(7, 7), Point(8, 7),
            Point(6, 8), Point(7, 8), Point(8, 8)
        )))
    }

    @Test fun `convert string to board`() {
        val board = """
            |..4|8..|.17
            |67.|9..|...
            |5.8|.3.|..4
            |---+---+---
            |3..|74.|1..
            |.69|...|78.
            |..1|.69|..5
            |---+---+---
            |1..|.8.|3.6
            |...|..6|.91
            |24.|..1|5..
        """.trimMargin().toBoard()

        board.apply {
            assertThat(this[0, 0], equalTo(Cell()))
            assertThat(this[2, 0], equalTo(Cell(4)))
            assertThat(this[0, 2], equalTo(Cell(5)))
            assertThat(this[8, 7], equalTo(Cell(1)))
        }
    }

    @Test fun `convert board to string`() {
        val board = Board().apply {
            set(0, 0, Cell(9))
            set(4, 0, Cell(8))
            set(8, 0, Cell(7))
            set(0, 4, Cell(6))
            set(4, 4, Cell(5))
            set(8, 4, Cell(4))
            set(0, 8, Cell(3))
            set(4, 8, Cell(2))
            set(8, 8, Cell(1))
        }
        assertThat(board.toString(), equalTo("""
            |9..|.8.|..7
            |...|...|...
            |...|...|...
            |---+---+---
            |...|...|...
            |6..|.5.|..4
            |...|...|...
            |---+---+---
            |...|...|...
            |...|...|...
            |3..|.2.|..1
        """.trimMargin()))
    }
}