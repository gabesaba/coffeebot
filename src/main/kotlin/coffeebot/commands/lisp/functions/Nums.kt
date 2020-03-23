package coffeebot.commands.lisp.functions

import coffeebot.commands.lisp.Env
import coffeebot.commands.lisp.Expression
import coffeebot.commands.lisp.Fn
import coffeebot.commands.lisp.LispNumber
import coffeebot.commands.lisp.TypeError

val add1 = Fn("add1") { args, env ->
    valArgs(args, 1, "add1")
    when (val res = args[0].eval(env)) {
        is LispNumber -> LispNumber(res.num + 1)
        else -> throw TypeError("add1 expected Num")
    }
}

val sub1 = Fn("sub1") { args, env ->
    when (val res = args[0].eval(env)) {
        is LispNumber -> LispNumber(res.num - 1)
        else -> throw TypeError("sub1 expected Num")
    }
}

val mul = Fn("*") { exprs, env ->
    LispNumber(getInts(exprs, env).fold(1) { a, b -> a * b })
}

fun getInts(exprs: List<Expression>, env: Env): Iterable<Int> {
    return exprs.map {
        val v = it.eval(env)
        if (v is LispNumber) {
            v.num
        } else {
            throw TypeError("Expected num")
        }
    }
}
