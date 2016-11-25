package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Ignore
import org.junit.Test
import org.kotlin99.common.fill
import org.kotlin99.common.tail
import org.kotlin99.common.toSeq
import org.kotlin99.misc.Nonogram.Companion.parse
import org.kotlin99.misc.Nonogram.Constraint
import java.util.*

@Suppress("unused") // Because this class is a "namespace".
class Nonogram {

    data class Board(val width: Int, val height: Int,
                     val rowConstrains: List<Constraint>, val columnConstraints: List<Constraint>,
                     private val cells: List<ArrayList<Boolean>> = 1.rangeTo(height).map{ ArrayList<Boolean>().fill(width, false)}) {

        fun solve(rowIndex: Int = 0): Board? {
            if (hasContradiction()) return null
            if (rowIndex == height) return this

            return rowConstrains.toSeq()
                .drop(rowIndex).flatMap { it.allMoves(width) }
                .mapNotNull { rowBoxes -> this.copy().apply(rowBoxes, rowIndex).solve(rowIndex + 1) }
                .first()
        }

        private fun hasContradiction(): Boolean {
            fun columnBoxHeights(column: Int): List<Int> {
                val columnCells = 0.rangeTo(height - 1).map{ cells[it][column] }

                val result = ArrayList<Int>()
                var lastCell = false
                var boxHeight = 0
                columnCells.forEach { cell ->
                    if (lastCell && !cell && boxHeight != 0) {
                        result.add(boxHeight)
                        boxHeight = 0
                    } else {
                        boxHeight++
                    }
                    lastCell = cell
                }
                if (boxHeight != 0) {
                    result.add(boxHeight)
                }
                return result
            }

            return columnConstraints != 0.rangeTo(width - 1).map(::columnBoxHeights)
        }

        fun apply(boxes: Sequence<Box>, row: Int): Board {
            boxes.forEach {
                it.index.rangeTo(it.width - 1).forEach {
                    cells[row][it] = true
                }
            }
            return this
        }

        fun copy(): Board {
            return Board(width, height, rowConstrains, columnConstraints, cells.map { ArrayList(it) })
        }

        override fun toString(): String {
            val max = columnConstraints.map{ it.boxes.size }.max()!!
            return cells.mapIndexed { i, row ->
                "|" + row.map { if (it) "X" else "_" }.joinToString("|") + "| " + rowConstrains[i].boxes.joinToString(" ")
            }.joinToString("\n") + "\n" + 0.rangeTo(max - 1).map { i ->
                " " + 0.rangeTo(width - 1).map{ if (i < columnConstraints[it].boxes.size) columnConstraints[it].boxes[i].toString() else " "}.joinToString(" ").trim()
            }.joinToString("\n")
        }
    }

    data class Box(val index: Int, val width: Int)

    data class Constraint(val boxes: List<Int>) {
        constructor(vararg boxes: Int): this(boxes.toList())

        fun allMoves(width: Int): Sequence<Sequence<Box>> {
            if (boxes.isEmpty()) return sequenceOf(emptySequence())

            return 0.rangeTo(width - boxes.first() - boxes.tail().sumBy{ it + 1 }).toSeq().flatMap { i ->
                Constraint(boxes.tail()).allMoves(width - i - boxes.first()).map {
                    sequenceOf(Box(i, boxes.first())) + it
                }
            }
        }
    }

    companion object {
        fun String.parse(): Board {
            fun List<List<Int>>.transpose(): List<List<Int>> {
                val max = maxBy{ it.size }!!.size
                val result = ArrayList<List<Int>>()
                0.rangeTo(max - 1).forEach { i ->
                    result.add(mapNotNull { list ->
                        if (i < list.size) list[i] else null
                    })
                }
                return result
            }

            val lines = split("\n")

            val rowConstraints = lines
                .takeWhile{ it.startsWith("|") }
                .map { it.replace(Regex("[|_]"), "") }
                .map { it.trim().split(" ").map(String::toInt) }
                .map(::Constraint)

            val columnConstraints = lines
                .dropWhile{ it.startsWith("|") }
                .map { it.trim().split(" ").map(String::toInt) }
                .transpose()
                .map(::Constraint)

            val width = columnConstraints.size
            val height = rowConstraints.size
            return Board(width, height, rowConstraints, columnConstraints)
        }
    }
}

class P98Test {

    @Test fun `parse string as nonogram`() {
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

        assertThat(nonogram, equalTo(Nonogram.Board(
            8, 9,
            listOf(
                Constraint(3),
                Constraint(2, 1),
                Constraint(3, 2),
                Constraint(2, 2),
                Constraint(6),
                Constraint(1, 5),
                Constraint(6),
                Constraint(1),
                Constraint(2)
            ),
            listOf(
                Constraint(1, 2),
                Constraint(3, 1),
                Constraint(1, 5),
                Constraint(7, 1),
                Constraint(5),
                Constraint(3),
                Constraint(4),
                Constraint(3)
            )
        )))
    }

    @Test fun `convert nonogram to string`() {
        val nonogram = Nonogram.Board(
            8, 9,
            listOf(
                Constraint(3),
                Constraint(2, 1),
                Constraint(3, 2),
                Constraint(2, 2),
                Constraint(6),
                Constraint(1, 5),
                Constraint(6),
                Constraint(1),
                Constraint(2)
            ),
            listOf(
                Constraint(1, 2),
                Constraint(3, 1),
                Constraint(1, 5),
                Constraint(7, 1),
                Constraint(5),
                Constraint(3),
                Constraint(4),
                Constraint(3)
            )
        )

        assertThat(nonogram.toString(), equalTo("""
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
        """.trimMargin("*")))
    }

    @Ignore
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

        assertThat(nonogram.solve()!!, equalTo(Nonogram.Board(
            8, 9,
            listOf(
                Constraint(3),
                Constraint(2, 1),
                Constraint(3, 2),
                Constraint(2, 2),
                Constraint(6),
                Constraint(1, 5),
                Constraint(6),
                Constraint(1),
                Constraint(2)
            ),
            listOf(
                Constraint(1, 2),
                Constraint(3, 1),
                Constraint(1, 5),
                Constraint(7, 1),
                Constraint(5),
                Constraint(3),
                Constraint(4),
                Constraint(3)
            )
        )))
    }
}