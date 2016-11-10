package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.tail
import java.util.*

class DLMatrix(matrix: List<List<Int>>) {
    val masterHeader: DL

    init {
        val dlMatrix = matrix.map { row -> row.map(::DL) }
        val width = dlMatrix.first().size

        masterHeader = DL(0)
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
            joinRight(headers.last(), masterHeader)
            joinRight(masterHeader, headers.first())
        }
        fun joinColumns() {
            columns.forEachIndexed { i, column ->
                column.pairs().forEach { joinDown(it.first, it.second) }
                column.forEach { it.header = headers[i] }
                joinDown(column.last(), headers[i])
                joinDown(headers[i], column.first())
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

    override fun toString(): String {
        return masterHeader.right!!.map(DL::down){ row ->
            row.map(DL::right, DL::value).joinToString("")
        }.joinToString("\n")
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

    fun each(direction: (DL) -> (DL?), f: (DL) -> Unit) {
        var next: DL? = this
        do {
            f(next!!)
            next = direction(next)
        } while (next != this)
    }
    fun <T> map(direction: (DL) -> (DL?), f: (DL) -> T): List<T> {
        val result = ArrayList<T>()
        each(direction) { result.add(f(it)) }
        return result
    }

    override fun toString() = value.toString()
}


class DancingLinksTest {
    @Test fun `create dancing links matrix`() {
        val dlMatrix = DLMatrix(listOf(
            listOf(0, 0, 1, 0, 1, 1, 0),
            listOf(1, 0, 0, 1, 0, 0, 1),
            listOf(0, 1, 1, 0, 0, 1, 0),
            listOf(1, 0, 0, 1, 0, 0, 0),
            listOf(0, 1, 0, 0, 0, 0, 1),
            listOf(0, 0, 0, 1, 1, 0, 1)
        ))
        assertThat(dlMatrix.toString(), equalTo("""
            |01234560
            |0010110
            |1001001
            |0110010
            |1001000
            |0100001
            |0001101
        """.trimMargin()))
    }
}