package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotlin99.misc.GameOfLife.Companion.Board
import org.kotlin99.misc.GameOfLife.Companion.Cell

@Suppress("unused")
class GameOfLife {
    companion object {

        data class Cell(val x: Int, val y: Int) {
            fun neighbours(): List<Cell> {
                return listOf(
                    copy(x = x - 1, y = y - 1),
                    copy(           y = y - 1),
                    copy(x = x + 1, y = y - 1),
                    copy(x = x + 1           ),
                    copy(x = x + 1, y = y + 1),
                    copy(           y = y + 1),
                    copy(x = x - 1, y = y + 1),
                    copy(x = x - 1           )
                )
            }
            override fun toString() = "($x,$y)"
        }

        data class Board(val cells: Set<Cell>) {
            constructor(vararg cells: Cell): this(cells.toSet())

            fun evolve(): Board {
                val survivedCells = cells.filter { cell ->
                    liveNeighboursOf(cell).let { it == 2 || it == 3 }
                }
                val newbornCells = cells.flatMap { cell ->
                    cell.neighbours().filter { liveNeighboursOf(it) == 3 }
                }
                return Board((survivedCells + newbornCells).toSet())
            }

            private fun liveNeighboursOf(cell: Cell): Int = cell.neighbours().count { cells.contains(it) }
        }
    }
}

class P96Test {
    @Test fun `lonely cells die`() {
        assertThat(Board(Cell(0, 0)).evolve(), equalTo(Board()))
        assertThat(Board(Cell(0, 0), Cell(0, 1)).evolve(), equalTo(Board()))
        assertThat(Board(Cell(0, 0), Cell(2, 2)).evolve(), equalTo(Board()))
    }

    @Test fun `cells with enough neighbours stay alive`() {
        assertTrue(parseBoard(
            "**-",
            "*--"
        ).evolve().cells.containsAll(listOf(
            Cell(0, 0), Cell(1, 0), Cell(0, 1)
        )))
        assertTrue(parseBoard(
            "**-",
            "**-"
        ).evolve().cells.containsAll(listOf(
            Cell(0, 0), Cell(0, 1), Cell(1, 0), Cell(1, 1)
        )))
    }

    @Test fun `overpopulated cells die`() {
        assertFalse(parseBoard(
            "**-",
            "**-",
            "*--"
        ).evolve().cells.containsAll(listOf(
            Cell(0, 1), Cell(1, 1)
        )))
    }

    @Test fun `dead cell with three neighbours becomes alive`() {
        assertTrue(parseBoard(
            "**",
            "*-"
        ).evolve().cells.containsAll(listOf(
            Cell(1, 1)
        )))
    }

    @Test fun `block pattern never changes`() {
        assertThat(
            parseBoard(
                "**-",
                "**-"
            ).evolve(), equalTo(
            parseBoard(
                "**-",
                "**-"
            )
        ))
    }

    @Test fun `beehive pattern never changes`() {
        assertThat(
            parseBoard(
                "-**-",
                "*--*",
                "-**-"
            ).evolve(), equalTo(
            parseBoard(
                "-**-",
                "*--*",
                "-**-"
            )
        ))
    }

    @Test fun `blinker pattern oscillates`() {
        assertThat(
            parseBoard(
                "-*-",
                "-*-",
                "-*-"
            ).evolve(), equalTo(
            parseBoard(
                "---",
                "***",
                "---"
            )
        ))
        assertThat(
            parseBoard(
                "---",
                "***",
                "---"
            ).evolve(), equalTo(
            parseBoard(
                "-*-",
                "-*-",
                "-*-"
            )
        ))
    }

    @Test fun `toad pattern oscillates`() {
        assertThat(
            parseBoard(
                "----",
                "-***",
                "***-",
                "----"
            ).evolve(), equalTo(
            parseBoard(
                "--*-",
                "*--*",
                "*--*",
                "-*--"
            )
        ))
        assertThat(
            parseBoard(
                "--*-",
                "*--*",
                "*--*",
                "-*--"
            ).evolve(), equalTo(
            parseBoard(
                "----",
                "-***",
                "***-",
                "----"
            )
        ))
    }

    @Test fun `convert board to string`() {
        assertThat(Board(Cell(0, 0), Cell(2, 0), Cell(1, 1), Cell(0, 2)).toPrettyString(), equalTo(
            "*-*\n" +
            "-*-\n" +
            "*--"
        ))
    }

    @Test fun `convert string to board`() {
        val board = parseBoard(
            "*-*\n" +
            "-*-\n" +
            "*--"
        )
        assertThat(board, equalTo(
            Board(Cell(0, 0), Cell(2, 0), Cell(1, 1), Cell(0, 2))
        ))
    }

    private fun parseBoard(s: String): Board = parseBoard(*s.split(Regex("\n")).toTypedArray())

    private fun parseBoard(vararg lines: String): Board {
        val cells = lines.mapIndexed { y, line ->
            line.toCharArray().mapIndexed { x, c ->
                if (c == '*') Cell(x, y) else null
            }
        }
        return Board(cells.flatten().filterNotNull().toSet())
    }

    private fun Board.toPrettyString(): String {
        val xMax = cells.map { it.x }.max()!!
        val yMax = cells.map { it.y }.max()!!

        return 0.rangeTo(yMax).joinToString("\n") { y ->
            0.rangeTo(xMax).joinToString("") { x ->
                if (cells.any { it.x == x && it.y == y }) "*" else "-"
            }
        }
    }

}