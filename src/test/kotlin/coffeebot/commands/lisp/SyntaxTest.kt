package coffeebot.commands.lisp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyntaxTest {
    @Test fun testSimple() {

        val lisp1 = parseLisp("+")
        assertEquals(Token("+"), lisp1)

        val lisp2 = parseLisp("(+ 5 5)")
        assertEquals(s(t("+"), t("5"), Token("5")), lisp2)
    }

    @Test fun testNested() {
        val lisp = parseLisp("(+ (- 3 2) (/ 5 3))")
        assertEquals(
                s(t("+"),
                        s(t("-"), t("3"), t("2")),
                        s(t("/"), t("5"), t("3"))), lisp)
    }

    @Test fun testEmptyList() {
        assertEquals(s(), parseLisp("()"))
    }

    @Test fun testSyntaxError() {
        assertFailsWith(SyntaxError::class) {
            parseLisp("(+ 5 5))")
        }

        assertFailsWith(SyntaxError::class) {
            parseLisp(")(")
        }
    }
}

