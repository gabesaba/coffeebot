package coffeebot.commands.lisp

import org.junit.Test
import kotlin.test.assertEquals

class MathTest {
    @Test
    fun testSubtraction() {
        assertEquals(LispNumber(0), interpret("(sub1 1)"))
    }

    @Test fun testAddition() {
        assertEquals(LispNumber(1), interpret("(add1 0)"))
    }
}