package coffeebot.commands.lisp

import coffeebot.commands.Command

val env = Env()

fun interpret(s: String): String {
    return parseLisp(s).eval(env).toString()
}

val lisp = Command("!lisp", "Eval a Lisp Expression") {
    try {
        it.reply(interpret(it.contents.removePrefix("!lisp")))
    } catch (e: Exception) {
        it.reply("$e")
    }
}
