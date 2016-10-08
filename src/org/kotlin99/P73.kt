package org.kotlin99

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.*

fun MTree<*>.toLispString(): String =
        if (children.isEmpty()) value.toString()
        else "(" + value.toString() + " " + children.joinToString(" "){ it.toLispString() } + ")"

fun String.fromLispString(): MTree<String> =
        SExprParser.parse(this).token!!.toMTree()


fun Token.toMTree(): MTree<String> =
        if (this is Seq) {
            val atom = tokens.filter{ it is Atom }.map{ (it as Atom).value }.first()
            val children = tokens.filter{ it is Seq }.flatMap{ (it as Seq).tokens }.map{ it.toMTree() }
            MTree(atom, children)
        } else if (this is Atom) {
            MTree(value)
        } else {
            throw IllegalStateException()
        }


interface Token {
    fun canDrop(): Boolean
}
data class Text(val value: String) : Token {
    override fun toString() = "'$value'"
    override fun canDrop() = true
}
data class Atom(val value: String): Token {
    override fun toString() = "v'$value'"
    override fun canDrop(): Boolean = false
}
data class Seq(val tokens: List<Token>): Token {
    constructor (vararg tokens: Token) : this(tokens.toList())
    fun prepend(token: Token): Seq = Seq(listOf(token) + tokens)
    override fun canDrop() = tokens.isEmpty()
    override fun toString() = "Seq[" + tokens.joinToString(" ") + "]"
}


interface TokenParser {
    fun parse(s: String): ParseResult<Token>
}
data class ParseResult<out T : Token>(val token: T?, val s: String)
fun <T : Token> okParseResult(token: T, s: String): ParseResult<T> = ParseResult(token, s)
fun failedParseResult(s: String): ParseResult<Nothing> = ParseResult(null, s)

class TextParser(val value: String): TokenParser {
    override fun parse(s: String): ParseResult<Token> =
            if (s.startsWith(value)) okParseResult(Text(value), s.drop(1))
            else failedParseResult(s)
}
val LParenParser = TextParser("(")
val RParenParser = TextParser(")")
val SpaceParser = TextParser(" ")

object AtomParser : TokenParser {
    override fun parse(s: String): ParseResult<Token> {
        val atom = s.takeWhile{
            LParenParser.value != it.toString() &&
            RParenParser.value != it.toString() &&
            SpaceParser.value != it.toString()
        }
        return if (atom.isEmpty()) failedParseResult(s) else okParseResult(Atom(atom), s.drop(atom.length))
    }
}

class SequenceParser(val tokenParsers: List<TokenParser>) : TokenParser {
    constructor(vararg tokenParsers: TokenParser) : this(tokenParsers.toList())

    override fun parse(s: String): ParseResult<Token> {
        val tokens = ArrayList<Token>()
        var rest = s
        var parsers = tokenParsers
        while (rest.isNotEmpty() && parsers.isNotEmpty()) {
            val (token, s1) = parsers.first().parse(rest)
            if (token == null) return failedParseResult(s)
            if (!token.canDrop()) {
                tokens.add(token)
            }
            rest = s1
            parsers = parsers.drop(1)
        }
        if (tokens.size == 1) return okParseResult(tokens.first(), rest)
        else return okParseResult(Seq(tokens), rest)
    }
}

class RepeatedParser(val tokenParser: TokenParser) : TokenParser {
    override fun parse(s: String): ParseResult<Seq> {
        val (token, s1) = tokenParser.parse(s)
        return when (token) {
            null -> okParseResult(Seq(), s)
            else -> {
                val result = parse(s1)
                okParseResult(result.token!!.prepend(token), result.s)
            }
        }
    }
}

class OrParser(vararg val tokenParsers: TokenParser): TokenParser {
    override fun parse(s: String): ParseResult<Token> {
        val result = failedParseResult(s)
        tokenParsers.forEach {
            val (token, s1) = it.parse(s)
            if (token != null) return okParseResult(token, s1)
        }
        return result
    }
}

object SExprParser : TokenParser {
    override fun parse(s: String): ParseResult<Token> {
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
        assertThat(SExprParser.parse("a").token!!, equalTo<Token>(Atom("a")))
        assertThat(SExprParser.parse("(a)").token!!, equalTo<Token>(Atom("a")))
        assertThat(SExprParser.parse("(a b c d)").token!!, equalTo<Token>(
            Seq(Atom("a"),
                Seq(Atom("b"), Atom("c"), Atom("d")))
        ))
        assertThat(SExprParser.parse("(a b (c d))").token!!, equalTo<Token>(
            Seq(Atom("a"),
                Seq(Atom("b"),
                    Seq(Atom("c"), Seq(Atom("d")))
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