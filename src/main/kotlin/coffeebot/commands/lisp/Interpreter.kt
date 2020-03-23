package coffeebot.commands.lisp

import coffeebot.commands.Command

fun interpret(s: String): LispObject {
    return parseLisp(s).eval(globalEnv)
}

val lisp = Command("!cl", "Eval a CoffeeLisp Expression") {
    try {
        val expr = it.contents.removePrefix("!cl")
        it.reply("$expr -> ${interpret(expr).display()}")
    } catch (e: Exception) {
        it.reply("$e")
    }
}
