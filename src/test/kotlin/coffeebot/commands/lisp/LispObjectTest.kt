package coffeebot.commands.lisp

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LispObjectTest {
    @Test
    fun testIdentifier() {
        assertTrue((Identifier.isIdentifier("hello")))
        assertTrue((Identifier.isIdentifier("World")))
        assertTrue((Identifier.isIdentifier("g")))
        assertTrue((Identifier.isIdentifier("g8")))
        assertFalse((Identifier.isIdentifier("2go")))
    }
}
