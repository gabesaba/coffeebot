package coffeebot.commands.lisp.functions

import coffeebot.commands.lisp.Fn
import coffeebot.commands.lisp.LispError
import coffeebot.commands.lisp.LispUnit
import coffeebot.commands.lisp.globalEnv
import coffeebot.commands.lisp.initialEnv

var clear = Fn("clear") { args, _ ->
    if (args.isNotEmpty()) {
        throw LispError("Clear takes no args")
    }

    globalEnv = initialEnv()
    LispUnit
}

val type = Fn("Type?") { exprs, env ->

    if (exprs.size != 1) {
        throw LispError("Type? expects exactly 1 arg")
    }
    exprs.first().eval(env).type()
}
