package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.toSeq
import org.kotlin99.misc.Sudoku.Board
import org.kotlin99.misc.Sudoku.Board.Companion.size
import org.kotlin99.misc.Sudoku.Cell
import org.kotlin99.misc.Sudoku.Companion.toBoard
import java.util.*

@Suppress("unused")
class Sudoku {
    data class Board(val cells: ArrayList<Cell> = ArrayList<Cell>().fill(size * size, Cell())) {
        private val positionedCells: List<PositionedCell>
            get() = cells.mapIndexed { i, cell -> PositionedCell(Point(i % size, i / size), cell) }

        operator fun get(x: Int, y: Int): Cell {
            return cells[x + y * size]
        }

        operator fun set(x: Int, y: Int, cell: Cell): Board {
            cells[x + y * size] = cell
            return this
        }

        fun solve(): Sequence<Board> {
            optimize()

            if (!isValid()) return emptySequence()
            if (cells.all{ it.isFilled() }) return sequenceOf(this)

            return positionedCells
                .filter { it.cell.isEmpty() }.toSeq()
                .flatMap {
                    it.cell.guesses.toSeq().flatMap{ guess ->
                        this.copy().set(it.point.x, it.point.y, Cell(guess)).solve()
                    }
                }
        }

        fun isValid(): Boolean {
            fun List<Point>.isValidFor(value: Int) = count{ get(it).guesses.contains(value) } <= 1
            return positionedCells
                .filter{ it.cell.isFilled() }
                .all {
                    it.point.row().isValidFor(it.cell.value!!) &&
                    it.point.column().isValidFor(it.cell.value) &&
                    it.point.square().isValidFor(it.cell.value)
                }
        }

        fun optimize(): Board {
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

            val cellsToFill = positionedCells.filter { it.cell.isEmpty() && it.cell.guesses.size == 1 }
            cellsToFill.forEach {
                this[it.point.x, it.point.y] = Cell(it.cell.guesses.first())
            }
            if (cellsToFill.isNotEmpty()) {
                optimize()
            }
            return this
        }

        fun copy(): Board {
            return Board(ArrayList(cells))
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

        private data class Point(val x: Int, val y: Int) {
            fun row() = 0.rangeTo(size - 1).map{ Point(it, y) }

            fun column() = 0.rangeTo(size - 1).map{ Point(x, it) }

            fun square(): List<Point> {
                fun rangeOfSquare(coordinate: Int): IntRange {
                    val squareCoordinate = coordinate / squareSize
                    return (squareCoordinate * squareSize).rangeTo(((squareCoordinate + 1) * squareSize) - 1)
                }
                return rangeOfSquare(y).flatMap { cellY ->
                    rangeOfSquare(x).map { cellX ->
                        Point(cellX, cellY)
                    }
                }
            }
        }

        private data class PositionedCell(val point: Point, val cell: Cell)

        companion object {
            val size = 9
            val squareSize = size / 3
        }
    }

    data class Cell(val value: Int?, val guesses: List<Int>) {
        constructor(): this(null, 1.rangeTo(Board.size).toList())
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