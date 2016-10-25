package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test


fun eightQueensSolutions(boardSize: Int): List<Board> {
    return eightQueensSolutions(Board(boardSize, emptyList()))
}

fun eightQueensSolutions(board: Board): List<Board> {
    return if (board.isComplete()) listOf(board)
    else board.nextMoves().flatMap(::eightQueensSolutions)
}

data class Queen(val row: Int, val column: Int)

data class Board(val size: Int, val queens: List<Queen>) {

    constructor(vararg queens: Queen): this(maxPosition(queens), queens.toList())

    fun nextMoves(): List<Board> {
        val nextColumn = (queens.map{ it.column }.max() ?: -1) + 1
        return 0.until(size)
                .map{ Queen(it, nextColumn) }
                .filter{ isValidMove(it) }
                .map{ Board(size, queens + it) }
    }

    fun isComplete(): Boolean {
        return queens.size == size
    }

    private fun isValidMove(queen: Queen): Boolean {
        fun notOnTheSameLine() = queens.none { it.row == queen.row || it.column == queen.column }
        fun notOnTheSameDiagonal() = queens.none {
            Math.abs(it.row - queen.row) == Math.abs(it.column - queen.column)
        }
        return notOnTheSameLine() && notOnTheSameDiagonal()
    }

    fun toPrettyString(): String {
        return 0.until(size).map { row ->
            0.until(size).map { column ->
                if (queens.contains(Queen(row, column))) "*" else "-"
            }.joinToString("")
        }.joinToString("\n")
    }

    companion object {
        private fun maxPosition(queens: Array<out Queen>): Int {
            return (queens.flatMap{ listOf(it.row, it.column) }.max() ?: -1) + 1
        }
    }
}


class P90Test {
    @Test fun `solutions for eight queen puzzle`() {
        assertThat(eightQueensSolutions(boardSize = 0), equalTo(listOf(Board())))
        assertThat(eightQueensSolutions(boardSize = 1), equalTo(listOf(Board(Queen(0, 0)))))
        assertThat(eightQueensSolutions(boardSize = 2), equalTo(emptyList()))
        assertThat(eightQueensSolutions(boardSize = 3), equalTo(emptyList()))

        assertThat(eightQueensSolutions(boardSize = 4).map(Board::toPrettyString), equalTo(listOf(
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
        ).map{it.trimMargin()}))

        assertThat(eightQueensSolutions(boardSize = 8).size, equalTo(92))
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