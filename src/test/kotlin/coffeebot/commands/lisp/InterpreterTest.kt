package coffeebot.commands.lisp

import kotlin.test.Test
import kotlin.test.assertEquals

class InterpreterTest {
    @Test fun testSimple() {
        assertEquals(LispNumber(10), interpret("(+ 5 5)"))
    }

    @Test fun define() {
        assertEquals(LispUnit, interpret("(define a 5)"))
        assertEquals(LispType("Number"), interpret("(type? a)"))
    }

    @Test fun type() {
        assertEquals(LispType("Fn"), interpret("(type? type?)"))
        assertEquals(LispType("Type"), interpret("(type? (type? 5))"))
        assertEquals(LispType("Type"), interpret("(type? (type? type?))"))
    }
}
