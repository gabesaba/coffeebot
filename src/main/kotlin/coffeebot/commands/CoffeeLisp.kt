package coffeebot.commands

import coffeelisp.env.createEnv
import coffeelisp.env.eval


// Shared env is more fun, as everyone
// can share definitions
private val sharedEnv = createEnv()

val lisp = Command("!cl", "Eval a CoffeeLisp Expression") {
    val lisp = it.contents.removePrefix("!cl")

    val lispOutput = try {
        lisp.eval(sharedEnv)
    } catch (e: Exception) {
        e.toString()
    }
    it.reply(lispOutput)
}
