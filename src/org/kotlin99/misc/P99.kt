package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.transpose
import org.kotlin99.misc.Crossword.Cell
import org.kotlin99.misc.Crossword.Site
import java.io.File

class CrosswordFileReader(val filePath: String) {
    fun readWords(): List<String> {
        return File(filePath).readLines().takeWhile{ it != "" }
    }

    fun readCrossword(): Crossword {
        return Crossword.parse(File(filePath).readLines().dropWhile{ it != "" }.drop(1))
    }
}

data class Crossword(val sites: List<Site>) {
    override fun toString(): String {
        val cells = sites.flatMap{ it.cells }
        val maxX = cells.map{ it.x }.max()!!
        val maxY = cells.map{ it.y }.max()!!

        return (0..maxY).map { y ->
            (0..maxX).map { x -> cells.find{ it.x == x && it.y == y } }
                .map { it?.c ?: Cell.none.c }
                .joinToString("")
        }.joinToString("\n").trim()
    }

    companion object {
        fun parse(lines: List<String>): Crossword {
            val maxWidth = lines.map{ it.length }.max()!!
            val paddedLines = lines.map{ it.padEnd(maxWidth + 1, ' ') } + "".padEnd(maxWidth + 1, ' ')
            val cells = paddedLines.mapIndexed { row: Int, line: String ->
                line.mapIndexed { col: Int, c: Char ->
                    if (c != ' ') Cell(col, row, c) else Cell.none
                }
            }
            val horizontalSites = consumeSites(cells.flatten())
            val verticalSites = consumeSites(cells.transpose().flatten())
            val sites = (horizontalSites + verticalSites).filter{ it.cells.size >= 2 }

            return Crossword(sites)
        }

        private fun consumeSites(cells: List<Cell>): List<Site> {
            if (cells.isEmpty()) return emptyList()
            if (cells.first() == Cell.none) return consumeSites(cells.dropWhile{ it == Cell.none })
            val site = Site(cells.takeWhile{ it != Cell.none })
            return listOf(site) + consumeSites(cells.dropWhile{ it != Cell.none })
        }
    }

    data class Site(val cells: List<Cell>) {
        constructor(vararg cells: Cell): this(cells.toList())

        override fun toString(): String {
            val x = cells.first().x
            val y = cells.first().y
            val word = cells.map{ it.c }.joinToString("")
            return "Site($x,$y,$word)"
        }

        fun add(cell: Cell) = Site(cells + cell)
    }

    data class Cell(val x: Int, val y: Int, var c: Char) {
        override fun toString(): String {
            return "($x,$y,$c)"
        }

        companion object {
            val none = Cell(-1, -1, ' ')
        }
    }
}


class P99Test {
    @Test fun `read crossword file`() {
        val reader = CrosswordFileReader("data/p99a.dat")
        val words = reader.readWords()
        val crossword = reader.readCrossword()

        assertThat(words, equalTo(listOf(
                "LINUX", "PROLOG", "PERL", "ONLINE", "GNU", "XML", "NFS", "SQL", "EMACS", "WEB", "MAC"
        )))
        assertThat(crossword.toString(), equalTo("""
            |......  .
            |. .  .  .
            |. ..... .
            |. . . ...
            |  . ... .
            | ...
        """.trimMargin()))
    }

    @Test fun `parse minimal crossword as set of sites`() {
        val crossword = Crossword.parse(listOf(
            "..",
            "."
        ))
        assertThat(crossword.sites, equalTo(listOf(
                Site(Cell(0, 0, '.'), Cell(1, 0, '.')),
                Site(Cell(0, 0, '.'), Cell(0, 1, '.'))
        )))
    }

    @Test fun `modification of one cell in crossword affects all sites`() {
        val crossword = Crossword.parse(listOf(
            "..",
            "."
        ))
        crossword.sites.first().cells.first().c = 'x'

        assertThat(crossword.sites, equalTo(listOf(
                Site(Cell(0, 0, 'x'), Cell(1, 0, '.')),
                Site(Cell(0, 0, 'x'), Cell(0, 1, '.'))
        )))
    }
}
