package coffeebot.commands.lisp.functions

import coffeebot.commands.lisp.Env
import coffeebot.commands.lisp.Expression
import coffeebot.commands.lisp.Fn
import coffeebot.commands.lisp.LispNumber
import coffeebot.commands.lisp.TypeError
import java.math.BigInteger

val add1 = Fn("add1") { args, env ->
    valArgs(args, 1, "add1")
    when (val res = args[0].eval(env)) {
        is LispNumber -> LispNumber(res.num + BigInteger.ONE)
        else -> throw TypeError("add1 expected Num")
    }
}

val sub1 = Fn("sub1") { args, env ->
    when (val res = args[0].eval(env)) {
        is LispNumber -> LispNumber(res.num - BigInteger.ONE)
        else -> throw TypeError("sub1 expected Num")
    }
}

val mul = Fn("*") { exprs, env ->
    LispNumber(getInts(exprs, env).fold(BigInteger.ONE) { a, b -> a * b })
}

fun getInts(exprs: List<Expression>, env: Env): Iterable<BigInteger> {
    return exprs.map {
        val v = it.eval(env)
        if (v is LispNumber) {
            v.num
        } else {
            throw TypeError("Expected num")
        }
    }
}
