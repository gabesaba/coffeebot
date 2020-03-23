package coffeebot.commands.lisp.functions

import coffeebot.commands.lisp.Env
import coffeebot.commands.lisp.Expression
import coffeebot.commands.lisp.Fn
import coffeebot.commands.lisp.LispError
import coffeebot.commands.lisp.LispUnit
import coffeebot.commands.lisp.LispNumber
import coffeebot.commands.lisp.Token
import coffeebot.commands.lisp.TypeError

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

val add = Fn("+") { exprs, env ->
    LispNumber(getInts(exprs, env).fold(0) { a, b -> a + b })
}

val sub = Fn("-") { exprs, env ->
    LispNumber(getInts(exprs, env).fold(0) { a, b -> a - b })
}

val mul = Fn("*") { exprs, env ->
    LispNumber(getInts(exprs, env).fold(1) { a, b -> a * b })
}


val undefinable = setOf(clear.name, definitions.name)
val define = Fn("Define") { exprs, env ->
    when {
        !env.isGlobalEnv() -> throw LispError("Must call Define at top level")
        2 != exprs.size -> throw LispError("Define expects 2 args")
    }

    val car = exprs[0]
    if (car is Token && car.isIdentifier()) {

        if (undefinable.contains(car.token.toLowerCase())) {
            throw LispError("Cannot redefine ${car.token}")
        }
        val res = exprs[1].eval(env)
        env.set(car.token, res)
        LispUnit
    } else {
        throw TypeError("Define expects first arg to be an identifier")
    }
}

