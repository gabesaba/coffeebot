package coffeebot.commands.lisp

import kotlin.test.Test
import kotlin.test.assertEquals

class InterpreterTest {
    @Test fun testSimple() {
        assertEquals("10", interpret("(+ 5 5)"))
    }
}

