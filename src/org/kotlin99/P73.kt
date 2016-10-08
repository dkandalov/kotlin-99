package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.*

fun MTree<*>.toLispString(): String =
        if (children.isEmpty()) value.toString()
        else "(" + value.toString() + " " + children.joinToString(" "){ it.toLispString() } + ")"

fun String.fromLispString(): MTree<String> =
        SExprParser.parse(this)!!.trim()!!.toMTree()


fun Token.toMTree(): MTree<String> =
        if (this is Seq) {
            val atom = (tokens.first() as Atom).value
            val children = tokens.drop(1).flatMap{ token ->
                if (token is Seq) {
                    token.tokens.map{ it.toMTree() }
                } else if (token is Atom) {
                    listOf(MTree(token.value))
                } else {
                    emptyList()
                }
            }
            MTree(atom, children)
        } else if (this is Atom) {
            MTree(value)
        } else {
            throw IllegalStateException()
        }


interface Token {
    fun length(): Int
    fun trim(): Token?
}
data class Text(val value: String) : Token {
    override fun toString() = "'$value'"
    override fun trim() = null
    override fun length() = value.length
}
data class Atom(val value: String): Token {
    override fun toString() = "v'$value'"
    override fun trim() = this
    override fun length() = value.length
}
data class Seq(val tokens: List<Token>): Token {
    constructor (vararg tokens: Token) : this(tokens.toList())
    fun prepend(token: Token): Seq = Seq(listOf(token) + tokens)
    override fun trim(): Token? =
        if (tokens.isEmpty()) null
        else {
            val trimmed = tokens.map{ it.trim() }.filter{ it != null }.map{ it!! }
            if (trimmed.size == 1) trimmed.first() else Seq(trimmed)
        }
    override fun toString() = "Seq[" + tokens.joinToString(" ") + "]"
    override fun length() = tokens.sumBy{ it.length() }
}


interface TokenParser {
    fun parse(s: String): Token?
}
class TextParser(val value: String): TokenParser {
    override fun parse(s: String): Token? =
            if (s.startsWith(value)) Text(value) else null
}
val LParenParser = TextParser("(")
val RParenParser = TextParser(")")
val SpaceParser = TextParser(" ")

object AtomParser : TokenParser {
    override fun parse(s: String): Token? {
        val atom = s.takeWhile{ it != '(' && it != ')' && it != ' ' }
        return if (atom.isEmpty()) null else Atom(atom)
    }
}

class SequenceParser(val tokenParsers: List<TokenParser>) : TokenParser {
    constructor(vararg tokenParsers: TokenParser) : this(tokenParsers.toList())

    override fun parse(s: String): Token? {
        val tokens = ArrayList<Token>()
        var rest = s
        var parsers = tokenParsers
        while (rest.isNotEmpty() && parsers.isNotEmpty()) {
            val token = parsers.first().parse(rest) ?: return null
            tokens.add(token)
            rest = rest.drop(token.length())
            parsers = parsers.drop(1)
        }
        if (tokens.size == 1) return tokens.first()
        else return Seq(tokens)
    }
}

class RepeatedParser(val tokenParser: TokenParser) : TokenParser {
    override fun parse(s: String): Seq {
        val token = tokenParser.parse(s)
        return if (token == null) Seq()
        else parse(s.drop(token.length())).prepend(token)
    }
}

class OrParser(vararg val tokenParsers: TokenParser): TokenParser {
    override fun parse(s: String): Token? {
        val result = null
        tokenParsers.forEach {
            val token = it.parse(s)
            if (token != null) return token
        }
        return result
    }
}

object SExprParser : TokenParser {
    override fun parse(s: String): Token? {
        val parser = OrParser(
                SequenceParser(LParenParser, AtomParser, RepeatedParser(SequenceParser(SpaceParser, this)), RParenParser),
                AtomParser
        )
        return parser.parse(s)
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
        assertThat(SExprParser.parse("a")!!.trim()!!, equalTo<Token>(Atom("a")))
        assertThat(SExprParser.parse("(a)")!!.trim()!!, equalTo<Token>(Atom("a")))
        assertThat(SExprParser.parse("(a b c d)")!!.trim()!!, equalTo<Token>(
            Seq(Atom("a"),
                Seq(Atom("b"), Atom("c"), Atom("d")))
        ))
        assertThat(SExprParser.parse("(a b (c d))")!!.trim()!!, equalTo<Token>(
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