package coffeebot.commands

import coffeebot.message.User
import coffeelisp.env.Env
import coffeelisp.env.createEnv
import coffeelisp.env.eval

val lisp = Command("!cl", "Eval a CoffeeLisp Expression") {
    try {
        val lisp = it.contents.removePrefix("!cl")
        val env = it.user.getEnv()

        it.reply(lisp.eval(env))
    } catch (e: Exception) {
        it.reply("$e")
    }
}

private val envs = mutableMapOf<User, Env>()

private fun User.getEnv(): Env {
    return envs.getOrPut(this) { createEnv() }
}
