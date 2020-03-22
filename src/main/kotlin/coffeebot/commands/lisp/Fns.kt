package coffeebot.commands.lisp


fun getInts(exprs: List<Expression>, env: Env): Iterable<Int> {
    return exprs.map {
        val v = it.eval(env)
        if (v is Number) {
            v.num
        } else {
            throw TypeError("Expected num")
        }
    }
}
val add = Fn { exprs, env ->
    Number(getInts(exprs, env).fold(0) { a, b -> a + b })
}

val sub = Fn { exprs, env ->
    Number(getInts(exprs, env).fold(0) { a, b -> a - b })
}

val mult = Fn { exprs, env ->
    Number(getInts(exprs, env).fold(1) { a, b -> a * b })
}
