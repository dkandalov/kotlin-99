package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotlin99.common.tail
import org.kotlin99.common.toSeq
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
    fun solveUsing(words: List<String>, allWords: Set<String> = words.toSet()): Sequence<Crossword> {
        if (sites.all{ it.isFilled() }) {
            return if (sites.all{ allWords.contains(it.word) }) sequenceOf(this) else emptySequence()
        }
        if (words.isEmpty()) return emptySequence()

        return words.toSeq().flatMap{ word ->
            sites.toSeq().flatMap { site ->
                if (!site.fits(word)) emptySequence()
                else this.copy().let { crossword ->
                    crossword.sites.find{ it == site }!!.fill(word)
                    crossword.solveUsing(words.tail(), allWords)
                }
            }
        }
    }

    fun copy(): Crossword {
        val cells = sites.flatMap{ it.cells }.distinct()
        val cellsCopy = cells.map{ it.copy() }
        return Crossword(sites.map {
            Site(it.cells.map{ cellsCopy[cells.indexOf(it)] })
        })
    }

    override fun toString(): String {
        val cells = sites.flatMap{ it.cells }
        val maxX = cells.map{ it.x }.max()!!
        val maxY = cells.map{ it.y }.max()!!

        return (0..maxY).map { y ->
            (0..maxX).map { x -> cells.find{ it.x == x && it.y == y } }
                .map { it?.c ?: Cell.none.c }
                .joinToString("").trimEnd()
        }.joinToString("\n")
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

        val word: String
            get() = cells.map{ it.c }.joinToString("")

        fun add(cell: Cell) = Site(cells + cell)

        fun fits(word: String): Boolean {
            if (cells.size != word.length) return false
            return cells.zip(word.toCharArray().toList()).all {
                val (cell, char) = it
                cell.c == '.' || cell.c == char
                
            }
        }

        fun fill(word: String) {
            word.forEachIndexed { i, c -> cells[i].c = c }
        }

        fun isFilled(): Boolean {
            return cells.all{ it.c != '.' }
        }

        override fun toString(): String {
            val x = cells.first().x
            val y = cells.first().y
            val word = cells.map{ it.c }.joinToString("")
            return "Site($x,$y,$word)"
        }
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
        val crossword = parseCrossword("""
            |..
            |.
        """)
        assertThat(crossword.sites, equalTo(listOf(
                Site(Cell(0, 0, '.'), Cell(1, 0, '.')),
                Site(Cell(0, 0, '.'), Cell(0, 1, '.'))
        )))
    }

    @Test fun `changed cell affects all sites`() {
        val crossword = parseCrossword("""
            |..
            |.
        """)
        crossword.sites.first().let {
            it.cells[0].c = 'a'
            it.cells[1].c = 'b'
        }
        assertThat(crossword.sites, equalTo(listOf(
                Site(Cell(0, 0, 'a'), Cell(1, 0, 'b')),
                Site(Cell(0, 0, 'a'), Cell(0, 1, '.'))
        )))
    }

    @Test fun `changed cell affects all sites in crossword copy`() {
        val crossword = parseCrossword("""
            |..
            |.
        """)

        crossword.sites.first().let {
            it.cells[0].c = 'a'
            it.cells[1].c = 'b'
        }
        assertThat(crossword.sites, equalTo(listOf(
                Site(Cell(0, 0, 'a'), Cell(1, 0, 'b')),
                Site(Cell(0, 0, 'a'), Cell(0, 1, '.'))
        )))
    }

    @Test fun `site can determine if word fits in and be filled`() {
        val site = Site(Cell(0, 0, 'a'), Cell(1, 0, 'b'), Cell(2, 0, '.'))
        assertFalse(site.fits("ab"))
        assertFalse(site.fits("Abc"))
        assertFalse(site.fits("abcd"))
        assertTrue(site.fits("abc"))

        site.fill("abc")
        assertThat(site, equalTo(Site(Cell(0, 0, 'a'), Cell(1, 0, 'b'), Cell(2, 0, 'c'))))
    }

    @Test fun `solve one-word crossword`() {
        val crossword = parseCrossword("ab.")
        val solvedCrossword = crossword.solveUsing(listOf("ab", "Abc", "abcd", "abc")).first()
        assertThat(solvedCrossword.toString(), equalTo("abc"))
    }

    @Test fun `solve two-words crossword`() {
        val crossword = parseCrossword("""
            |ab.
            |.
        """)

        val solvedCrossword = crossword.solveUsing(listOf("ab", "Abc", "abcd", "abc")).first()

        assertThat(solvedCrossword.toString(), equalTo("""
            |abc
            |b
        """.trimMargin()))
    }

    @Test fun `solve crossword where several word fits several sites`() {
        val crossword = parseCrossword("""
            |...
            |.
            |...
            |h
        """)
        val solvedCrossword = crossword.solveUsing(listOf("abc", "efg", "adeh")).first()
        assertThat(solvedCrossword.toString(), equalTo("""
            |abc
            |d
            |efg
            |h
        """.trimMargin()))
    }

    @Test fun `solve crossword from p99a file`() {
        val reader = CrosswordFileReader("data/p99a.dat")
        val crossword = reader.readCrossword().solveUsing(reader.readWords()).first()

        assertThat(crossword.toString(), equalTo("""
            |PROLOG  E
            |E N  N  M
            |R LINUX A
            |L I F MAC
            |  N SQL S
            | WEB
        """.trimMargin()))
    }

    private fun parseCrossword(s: String): Crossword = Crossword.parse(s.trimMargin().split("\n"))
}
