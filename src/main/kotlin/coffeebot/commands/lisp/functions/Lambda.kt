package coffeebot.commands.lisp.functions

import coffeebot.commands.lisp.Env
import coffeebot.commands.lisp.Fn
import coffeebot.commands.lisp.SExpression
import coffeebot.commands.lisp.Token
import coffeebot.commands.lisp.TypeError

// TODO: closure around names in definition environment
val lambda = Fn("Lambda") { outerArgs, _ ->
    valArgs(outerArgs, 2, "Lambda")

    val car = outerArgs[0]
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

    val lambdaFn = outerArgs[1]
    Fn("Lambda") { args, lambdaEnv ->

        valArgs(args, formals.size, "Lambda")


        val objects = args.map { it.eval(lambdaEnv) }
        val newEnv = Env(lambdaEnv)

        for ((formal, arg) in formals.zip(objects)) {
            newEnv.set(formal.token, arg)
        }
        lambdaFn.eval(newEnv)
    }
}
