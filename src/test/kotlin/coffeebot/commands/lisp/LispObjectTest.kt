package coffeebot.commands.lisp

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LispObjectTest {
    @Test
    fun testIdentifier() {
        assertTrue((Token("hello").isIdentifier()))
        assertTrue((Token("World").isIdentifier()))
        assertTrue((Token("g").isIdentifier()))
        assertTrue((Token("g8").isIdentifier()))
        assertFalse((Token("2go").isIdentifier()))
    }
}
