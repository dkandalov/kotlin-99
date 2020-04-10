package org.kotlin99.misc

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kotlin99.common.transpose
import org.kotlin99.misc.Crossword.Cell
import org.kotlin99.misc.Crossword.Cell.Companion.vacant
import org.kotlin99.misc.Crossword.Site
import java.io.File

class CrosswordFileReader(private val filePath: String) {
    fun readWords(): List<String> =
        File(filePath).readLines().takeWhile { it != "" }

    fun readCrossword(): Crossword =
        Crossword.parse(File(filePath).readLines().dropWhile { it != "" }.drop(1))
}

data class Crossword(val sites: List<Site>) {
    fun solve(words: List<String>, i: Int = 0): Sequence<Crossword> {
        if (isInvalidFor(words)) return emptySequence()
        if (siteToFill().isEmpty()) return sequenceOf(this)
        if (i == words.size || words.size - i < siteToFill().size) return emptySequence()

        return siteToFill().filter { it.fits(words[i]) }.asSequence().flatMap { site ->
            copy().let { crossword ->
                crossword.sites.find { it == site }!!.fill(words[i])
                crossword.solve(words, i + 1)
            }
        } + solve(words, i + 1)
    }

    private fun siteToFill() = sites.filter { !it.filled }

    private fun isInvalidFor(words: List<String>) =
        sites.any { it.filled && !words.contains(it.word) } ||
        sites.any { !it.filled && words.none { word -> it.fits(word) } }

    private fun copy(): Crossword {
        val cells = sites.flatMap { it.cells }.distinct()
        val copyByCell = cells.associateWith { it.copy() }
        return Crossword(sites.map { site ->
            Site(site.cells.map { copyByCell.getValue(it) })
        })
    }

    override fun toString(): String {
        val cells = sites.flatMap { it.cells }
        val maxX = cells.map { it.x }.max()!!
        val maxY = cells.map { it.y }.max()!!

        return (0..maxY).joinToString("\n") { y ->
            (0..maxX).map { x -> cells.find { it.x == x && it.y == y } }
                .map { it?.c ?: Cell.none.c }
                .joinToString("").trimEnd()
        }
    }

    companion object {
        fun parse(lines: List<String>): Crossword {
            val maxWidth = lines.map { it.length }.max()!!
            val paddedLines = lines.map { it.padEnd(maxWidth + 1, ' ') } + "".padEnd(maxWidth + 1, ' ')
            val cells = paddedLines.mapIndexed { row: Int, line: String ->
                line.mapIndexed { col: Int, c: Char ->
                    if (c != ' ') Cell(col, row, c) else Cell.none
                }
            }
            val horizontalSites = consumeSites(cells.flatten())
            val verticalSites = consumeSites(cells.transpose().flatten())
            val sites = (horizontalSites + verticalSites).filter { it.cells.size >= 2 }

            return Crossword(sites)
        }

        private fun consumeSites(cells: List<Cell>): List<Site> {
            if (cells.isEmpty()) return emptyList()
            if (cells.first() == Cell.none) return consumeSites(cells.dropWhile { it == Cell.none })
            val site = Site(cells.takeWhile { it != Cell.none })
            return listOf(site) + consumeSites(cells.dropWhile { it != Cell.none })
        }
    }

    data class Site(val cells: List<Cell>) {
        constructor(vararg cells: Cell): this(cells.toList())

        val word: String
            get() = cells.map { it.c }.joinToString("")

        val filled: Boolean
            get() = cells.all { it.c != vacant }

        fun add(cell: Cell) = Site(cells + cell)

        fun fits(word: String): Boolean {
            if (cells.size != word.length) return false
            return cells.indices.all { i ->
                cells[i].c == vacant || cells[i].c == word[i]
            }
        }

        fun fill(word: String) {
            word.forEachIndexed { i, c -> cells[i].c = c }
        }

        override fun toString(): String {
            val x = cells.first().x
            val y = cells.first().y
            val word = cells.map { it.c }.joinToString("")
            return "Site($x,$y,$word)"
        }
    }

    data class Cell(val x: Int, val y: Int, var c: Char) {
        override fun toString() = "($x,$y,$c)"

        companion object {
            val none = Cell(-1, -1, ' ')
            const val vacant = '.'
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
        val solvedCrossword = crossword.solve(listOf("ab", "Abc", "abcd", "abc")).first()
        assertThat(solvedCrossword.toString(), equalTo("abc"))
    }

    @Test fun `solve two-words crossword`() {
        val crossword = parseCrossword("""
            |ab.
            |.
        """)

        val solvedCrossword = crossword.solve(listOf("ab", "Abc", "abcd", "abc")).first()

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
        val solvedCrossword = crossword.solve(listOf("abc", "efg", "adeh")).first()
        assertThat(solvedCrossword.toString(), equalTo("""
            |abc
            |d
            |efg
            |h
        """.trimMargin()))
    }

    @Test fun `solve crossword from p99a file`() {
        val reader = CrosswordFileReader("data/p99a.dat")
        val crossword = reader.readCrossword().solve(reader.readWords()).first()

        assertThat(crossword.toString(), equalTo("""
            |PROLOG  E
            |E N  N  M
            |R LINUX A
            |L I F MAC
            |  N SQL S
            | WEB
        """.trimMargin()))
    }

    @Test fun `solve crossword from p99b file`() {
        val reader = CrosswordFileReader("data/p99b.dat")
        val crossword = reader.readCrossword().solve(reader.readWords()).first()

        assertThat(crossword.toString(), equalTo("""
            |P TUEBINGEN TRAUBENZUCKER
            |R A       E A         R I
            |O TEMPERAMENT   FORTUNA V
            |T T       F       E   N I
            |EGERIA ZEUS T SAMPAN  K E
            |K R         E T   U   E R
            |T S WALZER  LIANE MADONNA
            |O A A  A  TAL N   U   K
            |RELIGION  R   N   R TIARA
            |A L G  K  U   I S   I S
            |T   O     E STOIKER L S
            | GRANAT   F   L E  OSTEN
            |  E     S F     L   I   C
            |  G  TURKMENEN VENDETTA H
            |  I B N R L     T     T R
            | ISEL T U  H STETTIN  T O
            |S T A E P  I   T    DER N
            |E E S R E BRIEFTAUBE  A O
            |KARRE T L  T   A    I K G
            |U     A    E AAL M  T T R
            |N ALLENSTEIN  N  I  A I A
            |D L    T      K  S  L O P
            |EOSIN  USAMBARA SERBIEN H
            |  E    H      R  R  E   I
            |HANNIBAL   MELASSE  NONNE
        """.trimMargin()))
    }

    @Test fun `solve crossword from p99c file`() {
        val reader = CrosswordFileReader("data/p99c.dat")
        val solution = reader.readCrossword().solve(reader.readWords())
        assertThat(solution.toList(), equalTo(emptyList()))
    }

    @Test fun `solve crossword from p99d file`() {
        val reader = CrosswordFileReader("data/p99d.dat")
        val crossword = reader.readCrossword().solve(reader.readWords()).first()

        assertThat(crossword.toString(), equalTo("""
            |BARKASSE REAKTION SIDERIT
            |E   N    I   R    I E   A
            |T F A AUSTRALIEN  G K   U
            |ERRATEN  T   A A BEDANKEN
            |I A O G  E A N T  L D R U
            |L G M O TRANIG A    E A S
            |I M I L    P ERLASSEN W
            |G E EKARTE A L I O  T A U
            |E N  A  E  S   T M    T N
            |NATTER  NESSEL A M AZETAT
            |     O  T  E E E E R  E E
            |GELEISE A  N U TERRIER  R
            |A    S  K    M     E    S
            |R S  ERREGER UEBERALL   T
            |A A     L    N    G     A
            |NENNER A HAENDEL VERGEBEN
            |T T    L A        N A   D
            |I A BULLAUGEN   M T S
            |E N    E S  E   E U SESAM
            |  D    N  OSTEREI R E   A
            |OBERHAUSEN  Z   S    I  N
            |R R    T  L  BESTELLEN  A
            |A  VERBERGEN A  E    N  G
            |D      I  N  N  R    E  E
            |EINLADEN  AFRIKANER ANKER
        """.trimMargin()))
    }

    private fun parseCrossword(s: String): Crossword = Crossword.parse(s.trimMargin().split("\n"))
}
