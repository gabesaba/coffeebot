package coffeebot.commands.lisp.functions

import coffeebot.commands.lisp.Env
import coffeebot.commands.lisp.Fn
import coffeebot.commands.lisp.LispError
import coffeebot.commands.lisp.SExpression
import coffeebot.commands.lisp.Token
import coffeebot.commands.lisp.TypeError

// TODO: closure around names in definition environment
val lambda = Fn("Lambda") { exprs, _ ->
    if (exprs.size != 2) {
        throw LispError("Lambda takes 2 args")
    }

    val car = exprs[0]

    if (car !is SExpression) {
        throw TypeError("Formals must be a S-Expression")
    }

    val formals = car.exprs.map {
        if (it is Token && it.isIdentifier()) {
            it
        } else {
            throw TypeError("Lambda expects identifiers as formals")
        }
    }

    val cdr = exprs[1]

    Fn("Lambda") { args, lambdaEnv ->
        val newEnv = Env(lambdaEnv)
        val expectedArgs = car.exprs.size
        if (args.size != expectedArgs) {
            throw TypeError("Lambda takes $expectedArgs args")
        }

        val objects = args.map { it.eval(lambdaEnv) }
        for ((formal, arg) in formals.zip(objects)) {
            newEnv.set(formal.token, arg)
        }
        cdr.eval(newEnv)
    }
}
