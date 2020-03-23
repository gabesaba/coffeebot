package coffeebot.commands.lisp

import kotlin.test.Test
import kotlin.test.assertEquals

class InterpreterTest {
    @Test fun define() {
        assertEquals(LispUnit, interpret("(define a 5)"))
        assertEquals(LispType("Num"), interpret("(type? a)"))
    }

    @Test fun type() {
        assertEquals(LispType("Fn"), interpret("(type? type?)"))
        assertEquals(LispType("Type"), interpret("(type? (type? 5))"))
        assertEquals(LispType("Type"), interpret("(type? (type? type?))"))
    }

    @Test fun recursion() {
        interpret("(define fact (lambda (x) (if (zero? x) 1 (* x (fact (sub1 x))))))")
        assertEquals(LispNumber(720), interpret("(fact 6)"))
    }
}
