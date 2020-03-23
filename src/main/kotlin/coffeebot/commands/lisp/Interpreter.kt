package coffeebot.commands.lisp

import coffeebot.commands.Command

fun interpret(s: String): String {
    return parseLisp(s).eval(globalEnv).display()
}

val lisp = Command("!cl", "Eval a CoffeeLisp Expression") {
    try {
        val expr = it.contents.removePrefix("!cl")
        it.reply("$expr -> ${interpret(expr)}")
    } catch (e: Exception) {
        it.reply("$e")
    }
}
