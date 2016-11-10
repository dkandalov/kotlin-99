package org.kotlin99.misc

import org.junit.Test
import org.kotlin99.common.tail

class DLMatrix(matrix: List<List<Int>>) {
    val masterHeader: DL

    init {
        val dlMatrix = matrix.map { row -> row.map(::DL) }
        val width = dlMatrix.first().size

        masterHeader = DL(Int.MAX_VALUE)
        val headers = 0.rangeTo(width - 1).map(::DL)
        val columns = 0.rangeTo(width - 1).map{ i -> dlMatrix.map{ it[i] } }

        fun joinRight(dl1: DL, dl2: DL) {
            dl1.right = dl2
            dl2.left = dl1
        }
        fun joinDown(dl1: DL, dl2: DL) {
            dl1.down = dl2
            dl2.up = dl1
        }
        fun joinHeaders() {
            headers.pairs().forEach { joinRight(it.first, it.second) }
            joinRight(masterHeader, headers.first())
            joinRight(headers.last(), masterHeader)
        }
        fun joinColumns() {
            columns.forEachIndexed { i, column ->
                column.pairs().forEach { joinRight(it.first, it.second) }
                column.forEach { it.header = headers[i] }
                joinDown(headers[i], column.first())
                joinDown(column.last(), headers[i])
            }
        }
        fun joinRows() {
            dlMatrix.forEach { row ->
                row.pairs().forEach { joinRight(it.first, it.second) }
                joinRight(row.last(), row.first())
            }
        }

        joinHeaders()
        joinColumns()
        joinRows()
    }

    private fun <T> List<T>.pairs(): List<Pair<T, T>> {
        return if (size <= 1) emptyList()
        else listOf(Pair(this[0], this[1])) + tail().pairs()
    }
}

class DL(val value: Int) {
    var left: DL? = null
    var right: DL? = null
    var up: DL? = null
    var down: DL? = null
    var header: DL? = null
}


class DancingLinksTest {
    @Test fun `creating dancing links matrix`() {
        DLMatrix(listOf(
            listOf(0, 0, 1, 0, 1, 1, 0),
            listOf(1, 0, 0, 1, 0, 0, 1),
            listOf(0, 1, 1, 0, 0, 1, 0),
            listOf(1, 0, 0, 1, 0, 0, 0),
            listOf(0, 1, 0, 0, 0, 0, 1),
            listOf(0, 0, 0, 1, 1, 0, 1)
        ))
    }
}