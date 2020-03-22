package coffeebot.commands.lisp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LexerText {
    @Test fun testSimple() {
        val res = lex("(+ 5 5)")
        assertEquals(listOf("(", "+", "5", "5", ")"), res)
    }

    @Test fun testFunnySpaces() {
        val res = lex(" (   +   5    5 )")
        assertEquals(listOf("(", "+", "5", "5", ")"), res)
    }

    @Test fun testEmpty() {
        assertFailsWith(SyntaxError::class) {  lex("") }
    }
}
