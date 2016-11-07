package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.toSeq
import org.kotlin99.misc.Sudoku.*
import org.kotlin99.misc.Sudoku.Board.Companion.size
import org.kotlin99.misc.Sudoku.Companion.toBoard
import java.util.*

@Suppress("unused")
class Sudoku {
    data class Board(val cells: ArrayList<Cell> = ArrayList<Cell>().fill(size * size, EmptyCell())) {
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
            if (cells.all{ it is FilledCell }) return sequenceOf(this)

            return cells.zip(0.rangeTo((cells.size) - 1)).toSeq()
                .flatMap { pair ->
                    val (cell, i) = pair
                    if (cell is EmptyCell) {
                        cell.guesses.toSeq().flatMap{ guess ->
                            val x = i % size
                            val y = i / size
                            this.copy().set(x, y, FilledCell(guess)).solve()
                        }
                    } else {
                        emptySequence<Board>()
                    }
                }
        }

        fun isValid(): Boolean {
            fun isFilledCell(x: Int, y: Int, value: Int): Boolean {
                return this[x, y].let { it is FilledCell && it.value == value }
            }
            fun isValidRow(y: Int, value: Int): Boolean {
                return 0.rangeTo(size - 1).count { x -> isFilledCell(x, y, value) } <= 1
            }
            fun isValidColumn(x: Int, value: Int): Boolean {
                return 0.rangeTo(size - 1).count { y -> isFilledCell(x, y, value) } <= 1
            }

            fun isValidSquare(x: Int, y: Int, value: Int): Boolean {
                fun squareRange(coordinate: Int): IntRange {
                    val squareSize = size / 3
                    val squareCoordinate = coordinate / squareSize
                    return (squareCoordinate * squareSize).rangeTo(((squareCoordinate + 1) * squareSize) - 1)
                }
                return squareRange(y).sumBy { cellY ->
                    squareRange(x).count { cellX ->
                        isFilledCell(cellX, cellY, value)
                    }
                } <= 1
            }
            cells.forEachIndexed { i, cell ->
                if (cell is FilledCell) {
                    val x = i % size
                    val y = i / size
                    if (cell is FilledCell) {
                        if (!isValidRow(y, cell.value) || !isValidColumn(x, cell.value) || !isValidSquare(x, y, cell.value)) {
                            return false
                        }
                    }
                }
            }
            return true
        }

        fun optimize(): Board {
            fun removeGuess(x: Int, y: Int, value: Int) {
                val cell = this[x, y]
                if (cell is EmptyCell) {
                    this[x, y] = cell.copy(cell.guesses - value)
                }
            }
            fun removeRowGuesses(y: Int, value: Int) {
                0.rangeTo(size - 1).forEach { x -> removeGuess(x, y, value) }
            }
            fun removeColumnGuesses(x: Int, value: Int) {
                0.rangeTo(size - 1).forEach { y -> removeGuess(x, y, value) }
            }
            fun removeSquareGuesses(x: Int, y: Int, value: Int) {
                fun squareRange(coordinate: Int): IntRange {
                    val squareSize = size / 3
                    val squareCoordinate = coordinate / squareSize
                    return (squareCoordinate * squareSize).rangeTo(((squareCoordinate + 1) * squareSize) - 1)
                }
                for (cellY in squareRange(y)) {
                    for (cellX in squareRange(x)) {
                        removeGuess(cellX, cellY, value)
                    }
                }
            }

            cells.forEachIndexed { i, cell ->
                if (cell is FilledCell) {
                    val x = i % size
                    val y = i / size
                    removeRowGuesses(y, cell.value)
                    removeColumnGuesses(x, cell.value)
                    removeSquareGuesses(x, y, cell.value)
                }
            }

            var hadFilledCells = false
            cells.forEachIndexed { i, cell ->
                if (cell is EmptyCell && cell.guesses.size == 1) {
                    val x = i % size
                    val y = i / size
                    this[x, y] = FilledCell(cell.guesses.first())
                    hadFilledCells = true
                }
            }
            if (hadFilledCells) {
                optimize()
            }
            return this
        }

        fun copy(): Board {
            return Board(ArrayList(cells))
        }

        override fun toString(): String {
            var s = ""
            cells.forEachIndexed { i, cell ->
                val cellValue =
                    if (cell is FilledCell) cell.value.toString()
                    else if (cell is EmptyCell) "."
                    else " "

                s += cellValue

                if ((i + 1) != size * size) {
                    if ((i + 1) % (size * 3) == 0) s += "\n---+---+---\n"
                    else if ((i + 1) % size == 0) s += "\n"
                    else if ((i + 1) % (size / 3) == 0) s += "|"
                }
            }
            return s
        }

        companion object {
            val size = 9
        }
    }

    interface Cell
    data class FilledCell(val value: Int) : Cell
    data class EmptyCell(val guesses: List<Int> = 1.rangeTo(Board.size).toList()) : Cell

    companion object {
        fun String.toBoard(): Board {
            val cells = ArrayList<Cell>()
            replace(Regex("[|\\-+\n]"), "").forEach { c ->
                cells.add(if (c == '.') EmptyCell() else FilledCell(c.toString().toInt()))
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
            assertThat(this[0, 0], equalTo<Cell>(EmptyCell()))
            assertThat(this[2, 0], equalTo<Cell>(FilledCell(4)))
            assertThat(this[0, 2], equalTo<Cell>(FilledCell(5)))
            assertThat(this[8, 7], equalTo<Cell>(FilledCell(1)))
        }
    }

    @Test fun `convert board to string`() {
        val board = Board().apply{
            set(0, 0, FilledCell(9))
            set(4, 0, FilledCell(8))
            set(8, 0, FilledCell(7))
            set(0, 4, FilledCell(6))
            set(4, 4, FilledCell(5))
            set(8, 4, FilledCell(4))
            set(0, 8, FilledCell(3))
            set(4, 8, FilledCell(2))
            set(8, 8, FilledCell(1))
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