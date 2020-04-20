package coffeebot.commands

import coffeelisp.env.createEnv
import coffeelisp.env.eval


// Shared env is more fun, as everyone
// can share definitions
private val sharedEnv = createEnv()

val lisp = Command("!cl", "Eval a CoffeeLisp Expression") {
    try {
        val lisp = it.contents.removePrefix("!cl")
        it.reply(lisp.eval(sharedEnv))
    } catch (e: Exception) {
        it.reply("$e")
    }
}
