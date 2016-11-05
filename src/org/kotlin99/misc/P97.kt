package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.misc.Sudoku.*
import org.kotlin99.misc.Sudoku.Companion.toBoard
import org.kotlin99.misc.Sudoku.Companion.toPrettyString
import java.util.*

@Suppress("unused")
class Sudoku {
    data class Board(val cells: ArrayList<Cell> = ArrayList<Cell>().fill(9 * 9, EmptyCell())) {
        operator fun get(x: Int, y: Int): Cell {
            return cells[x + y * 9]
        }
        operator fun set(x: Int, y: Int, cell: Cell): Board {
            cells[x + y * 9] = cell
            return this
        }
    }

    interface Cell
    data class FilledCell(val value: Int) : Cell
    data class EmptyCell(val guesses: List<Int> = 1.rangeTo(9).toList()) : Cell

    companion object {
        fun Board.toPrettyString(): String {
            var s = ""
            cells.forEachIndexed { i, cell ->
                val cellValue =
                    if (cell is FilledCell) cell.value.toString()
                    else if (cell is EmptyCell) "."
                    else " "

                s += cellValue

                if ((i+1) != 9 * 9) {
                    if ((i+1) % 27 == 0) s += "\n---+---+---\n"
                    else if ((i+1) % 9 == 0) s += "\n"
                    else if ((i+1) % 3 == 0) s += "|"
                }
            }
            return s
        }

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
    @Test fun `convert string to board`() {
        val s = """
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
        """.trimMargin()

        s.toBoard().apply {
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
        assertThat(board.toPrettyString(), equalTo("""
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