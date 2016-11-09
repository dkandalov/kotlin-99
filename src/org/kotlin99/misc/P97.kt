package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.toSeq
import org.kotlin99.misc.Sudoku.*
import org.kotlin99.misc.Sudoku.Board.Companion.size
import org.kotlin99.misc.Sudoku.Board.Companion.squareSize
import org.kotlin99.misc.Sudoku.Companion.toBoard
import org.kotlin99.misc.Sudoku.Point
import java.util.*

@Suppress("unused")
class Sudoku {
    data class Board(val cells: ArrayList<Cell> = ArrayList<Cell>().fill(size * size, Cell())) {
        private val positionedCells: List<PositionedCell>
            get() = cells.mapIndexed { i, cell -> PositionedCell(Point(i % size, i / size), cell) }

        fun solve(): Sequence<Board> {
            optimizeGuesses()

            if (cells.any{ it.isEmpty() && it.guesses.isEmpty() }) return emptySequence()
            if (cells.all{ it.isFilled() }) return sequenceOf(this)

            return positionedCells.find{ it.cell.isEmpty() }!!.let{
                    it.cell.guesses.toSeq().flatMap { guess ->
                        Board(ArrayList(cells)).set(it.point.x, it.point.y, Cell(guess)).solve()
                    }
                }
        }

        private fun optimizeGuesses() {
            fun List<Point>.removeGuesses(value: Int) = forEach {
                set(it.x, it.y, get(it).removeGuess(value))
            }

            positionedCells
                .filter{ it.cell.isFilled() }
                .forEach{
                    it.point.row().removeGuesses(it.cell.value!!)
                    it.point.column().removeGuesses(it.cell.value)
                    it.point.square().removeGuesses(it.cell.value)
                }
        }

        operator fun get(x: Int, y: Int): Cell {
            return cells[x + y * size]
        }

        operator fun set(x: Int, y: Int, cell: Cell): Board {
            cells[x + y * size] = cell
            return this
        }

        override fun toString(): String {
            var s = ""
            positionedCells.forEach {
                s += if (it.cell.isFilled()) it.cell.value.toString() else "."

                val x1 = it.point.x + 1
                val y1 = it.point.y + 1
                if (x1 < size || y1 < size) {
                    if (x1 == size && y1 % squareSize == 0) s += "\n---+---+---\n"
                    else if (x1 % size == 0) s += "\n"
                    else if (x1 % squareSize == 0) s += "|"
                }
            }
            return s
        }

        private operator fun get(point: Point): Cell {
            return this[point.x, point.y]
        }

        companion object {
            val size = 9
            val squareSize = size / 3
        }
    }

    private data class PositionedCell(val point: Point, val cell: Cell)

    data class Point(val x: Int, val y: Int) {
        fun row() = 0.rangeTo(size - 1).map{ Point(it, y) }

        fun column() = 0.rangeTo(size - 1).map{ Point(x, it) }

        fun square(): List<Point> {
            fun rangeOfSquare(coordinate: Int): IntRange {
                return (coordinate / squareSize).let {
                    (it * squareSize).rangeTo(((it + 1) * squareSize) - 1)
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
        fun isEmpty() = !isFilled()
        fun removeGuess(value: Int) = if (guesses.isEmpty()) this else copy(guesses = guesses - value)
    }

    companion object {
        fun String.toBoard(): Board {
            val cells = replace(Regex("[|\\-+\n]"), "").mapTo(ArrayList()) { c ->
                if (c == '.') Cell() else Cell(c.toString().toInt())
            }
            return Board(cells)
        }

        fun <T> ArrayList<T>.fill(n: Int, value: T): ArrayList<T> {
            1.rangeTo(n).forEach { add(value) }
            return this
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

    @Test fun `solve easy sudoku from dailysudoku-dot-com`() {
        // http://dailysudoku.com/sudoku/archive/2016/11/2016-11-8_solution.shtml
        val board = """
            |..3|...|..9
            |945|..7|.38
            |8..|3.1|4.5
            |---+---+---
            |...|6.3|294
            |62.|.5.|.87
            |398|4.2|...
            |---+---+---
            |4.9|8.6|..3
            |53.|9..|876
            |1..|...|9..
        """.trimMargin().toBoard()

        assertThat(board.solve().first(), equalTo("""
            |213|548|769
            |945|267|138
            |867|391|425
            |---+---+---
            |751|683|294
            |624|159|387
            |398|472|651
            |---+---+---
            |479|826|513
            |532|914|876
            |186|735|942
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
        val board = Board().apply{
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