package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.misc.EightQueens.Companion.Board
import org.kotlin99.misc.EightQueens.Companion.Queen
import org.kotlin99.misc.EightQueens.Companion.solutions
import kotlin.math.abs

@Suppress("unused")
class EightQueens {
    companion object {
        fun solutions(boardSize: Int): List<Board> {
            return solutions(Board(boardSize, emptyList()))
        }

        fun solutions(board: Board): List<Board> {
            return if (board.isComplete()) listOf(board)
            else board.nextMoves().flatMap { solutions(it) }
        }

        data class Queen(val row: Int, val column: Int)

        data class Board(val size: Int, val queens: List<Queen>) {

            constructor(vararg queens: Queen): this(maxPosition(queens), queens.asList())

            fun nextMoves(): List<Board> {
                val nextColumn = (queens.map { it.column }.max() ?: -1) + 1
                return 0.until(size)
                    .map { Queen(it, nextColumn) }
                    .filter { isValidMove(it) }
                    .map { Board(size, queens + it) }
            }

            fun isComplete(): Boolean {
                return queens.size == size
            }

            private fun isValidMove(queen: Queen): Boolean {
                fun notOnTheSameLine() = queens.none { it.row == queen.row || it.column == queen.column }
                fun notOnTheSameDiagonal() = queens.none {
                    abs(it.row - queen.row) == abs(it.column - queen.column)
                }
                return notOnTheSameLine() && notOnTheSameDiagonal()
            }

            fun toPrettyString(): String {
                return 0.until(size).joinToString("\n") { row ->
                    0.until(size).joinToString("") { column ->
                        if (queens.contains(Queen(row, column))) "*" else "-"
                    }
                }
            }

            companion object {
                private fun maxPosition(queens: Array<out Queen>): Int {
                    return (queens.flatMap { listOf(it.row, it.column) }.max() ?: -1) + 1
                }
            }
        }
    }
}


class P90Test {
    @Test fun `solutions for eight queen puzzle`() {
        assertThat(solutions(boardSize = 0), equalTo(listOf(Board())))
        assertThat(solutions(boardSize = 1), equalTo(listOf(Board(Queen(0, 0)))))
        assertThat(solutions(boardSize = 2), equalTo(emptyList()))
        assertThat(solutions(boardSize = 3), equalTo(emptyList()))

        assertThat(solutions(boardSize = 4).map(Board::toPrettyString), equalTo(listOf(
            """
            |--*-
            |*---
            |---*
            |-*--
            """,
            """
            |-*--
            |---*
            |*---
            |--*-
            """
        ).map { it.trimMargin() }))

        assertThat(solutions(boardSize = 8).size, equalTo(92))
    }

    @Test fun `convert board to string`() {
        assertThat(Board(Queen(0, 0), Queen(2, 1), Queen(1, 2)).toPrettyString(), equalTo("""
            |*--
            |--*
            |-*-
        """.trimMargin()
        ))
    }
}