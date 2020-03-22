package coffeebot.commands.lisp

import coffeebot.commands.Command

val env = Env()

fun interpret(s: String): String {
    return parseLisp(s).eval(env).display()
}

val lisp = Command("!cl", "Eval a CoffeeLisp Expression") {
    try {
        it.reply(interpret(it.contents.removePrefix("!cl")))
    } catch (e: Exception) {
        it.reply("$e")
    }
}
