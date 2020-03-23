package coffeebot.commands.lisp.functions

import coffeebot.commands.lisp.Fn
import coffeebot.commands.lisp.LispBool
import coffeebot.commands.lisp.LispError
import coffeebot.commands.lisp.LispNumber
import coffeebot.commands.lisp.TypeError

val if7 = Fn("if") { args, env ->

    valArgs(args, 3, "if")

    when (args[0].eval(env)) {
        LispBool.True -> args[1].eval(env)
        LispBool.False -> args[2].eval(env)
        else -> throw LispError("if expects first arg to be predicate")
    }
}

val zero = Fn("zero?") { args, env ->
    valArgs(args, 1, "zero")

    val res = args[0].eval(env)

    if (res !is LispNumber) {
        throw TypeError("zero? expected Num")
    }

    if (res == LispNumber(0)) {
        LispBool.True
    } else {
        LispBool.False
    }
}
