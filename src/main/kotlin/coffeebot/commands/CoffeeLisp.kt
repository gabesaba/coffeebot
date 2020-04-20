package coffeebot.commands

import coffeelisp.env.interpret

val lisp = Command("!cl", "Eval a CoffeeLisp Expression") {
    try {
        val expr = it.contents.removePrefix("!cl")
        it.reply("$expr -> ${interpret(expr).display()}")
    } catch (e: Exception) {
        it.reply("$e")
    }
}
