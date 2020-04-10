package org.kotlin99.multiwaytrees

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.kotlin99.common.tail
import java.util.*

fun MTree<*>.toLispString(): String =
    if (children.isEmpty()) value.toString()
    else "(" + value.toString() + " " + children.joinToString(" ") { it.toLispString() } + ")"

fun String.fromLispString(): MTree<String> =
    SExprParser.parse(this)!!.trim()!!.toMTree()


private object SExprParser: TokenParser {
    override fun parse(s: String): Token? {
        val parser = OrParser(
            SequenceParser(LParenParser, AtomParser, RepeatedParser(SequenceParser(SpaceParser, this)), RParenParser),
            AtomParser
        )
        return parser.parse(s)
    }
}

private fun Token.toMTree(): MTree<String> {
    fun Token.toList(): List<Token> =
        when (this) {
            is Atom -> listOf(this)
            is Seq  -> this.tokens
            else    -> throw IllegalStateException(this.toString())
        }

    return when (this) {
        is Seq  -> MTree((tokens[0] as Atom).value, tokens[1].toList().map { it.toMTree() })
        is Atom -> MTree(value)
        else    -> throw IllegalStateException(this.toString())
    }
}


private interface Token {
    fun length(): Int
    fun trim(): Token?
}

private data class Text(val value: String): Token {
    override fun trim(): Token? = null
    override fun length() = value.length
    override fun toString() = value
}

private data class Atom(val value: String): Token {
    override fun trim() = this
    override fun length() = value.length
    override fun toString() = "'$value'"
}

private data class Seq(val tokens: List<Token>): Token {
    constructor (vararg tokens: Token): this(tokens.asList())

    override fun trim(): Token? =
        if (tokens.isEmpty()) null
        else {
            val trimmed = tokens.mapNotNull { it.trim() }
            if (trimmed.size == 1) trimmed.first() else Seq(trimmed)
        }

    override fun length() = tokens.sumBy { it.length() }
    override fun toString() = "Seq[" + tokens.joinToString(" ") + "]"
}


private interface TokenParser {
    fun parse(s: String): Token?
}

private class TextParser(val value: String): TokenParser {
    override fun parse(s: String): Token? =
        if (s.startsWith(value)) Text(value) else null
}

private val LParenParser = TextParser("(")
private val RParenParser = TextParser(")")
private val SpaceParser = TextParser(" ")

private object AtomParser: TokenParser {
    override fun parse(s: String): Token? {
        val atom = s.takeWhile { it != '(' && it != ')' && it != ' ' }
        return if (atom.isEmpty()) null else Atom(atom)
    }
}

private class SequenceParser(val tokenParsers: List<TokenParser>): TokenParser {
    constructor(vararg tokenParsers: TokenParser): this(tokenParsers.asList())

    override fun parse(s: String): Token? {
        val tokens = ArrayList<Token>()
        var rest = s
        var parsers = tokenParsers
        while (rest.isNotEmpty() && parsers.isNotEmpty()) {
            val token = parsers.first().parse(rest) ?: return null
            tokens.add(token)
            rest = rest.drop(token.length())
            parsers = parsers.tail()
        }
        return Seq(tokens)
    }
}

private class RepeatedParser(val tokenParser: TokenParser): TokenParser {
    private fun Seq.prepend(token: Token): Seq = Seq(listOf(token) + tokens)

    override fun parse(s: String): Seq {
        val token = tokenParser.parse(s)
        return if (token == null) Seq()
        else parse(s.drop(token.length())).prepend(token)
    }
}

private class OrParser(vararg val tokenParsers: TokenParser): TokenParser {
    override fun parse(s: String): Token? {
        tokenParsers.forEach {
            val token = it.parse(s)
            if (token != null) return token
        }
        return null
    }
}


class P73Test {
    @Test fun `convert multiway tree to lisp string`() {
        assertThat(MTree("a").toLispString(), equalTo("a"))
        assertThat(MTree("a", MTree("b"), MTree("c")).toLispString(), equalTo("(a b c)"))
        assertThat(MTree("a", MTree("b"), MTree("c", MTree("d"))).toLispString(), equalTo("(a b (c d))"))
        assertThat("afg^^c^bd^e^^^".convertToMTree().toLispString(), equalTo("(a (f g) c (b d e))"))
    }

    @Test fun `parse s-expression into tokens`() {
        assertThat(SExprParser.parse("a")!!.trim()!!, equalTo(Atom("a")))
        assertThat(SExprParser.parse("(a)")!!.trim()!!, equalTo(Atom("a")))
        assertThat(SExprParser.parse("(a b c d)")!!.trim()!!, equalTo(
            Seq(Atom("a"),
                Seq(Atom("b"), Atom("c"), Atom("d")))
        ))
        assertThat(SExprParser.parse("(a b (c d))")!!.trim()!!, equalTo(
            Seq(Atom("a"),
                Seq(Atom("b"),
                    Seq(Atom("c"), Atom("d"))
                )
            )
        ))
    }

    @Test fun `transform tokens into multiway tree`() {
        assertThat(
            Seq(Atom("a"),
                Seq(Atom("b"), Atom("c"), Atom("d"))
            ).toMTree(),
            equalTo(MTree("a", MTree("b"), MTree("c"), MTree("d"))))

        assertThat(
            Seq(Atom("a"),
                Seq(Atom("b"), Atom("c"), Atom("d"))
            ).toMTree(),
            equalTo(MTree("a", MTree("b"), MTree("c"), MTree("d"))))
    }

    @Test fun `convert lisp string to multiway tree`() {
        assertThat("a".fromLispString(), equalTo(MTree("a")))
        assertThat("(a b c)".fromLispString(), equalTo(MTree("a", MTree("b"), MTree("c"))))
        assertThat("(a b (c d))".fromLispString(), equalTo(MTree("a", MTree("b"), MTree("c", MTree("d")))))
        assertThat("(a (f g) c (b d e))".fromLispString(), equalTo(
            MTree("a",
                  MTree("f", MTree("g")),
                  MTree("c"),
                  MTree("b", MTree("d"), MTree("e"))
            )
        ))
    }
}