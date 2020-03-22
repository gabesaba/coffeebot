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
val add = Fn("Add") { exprs, env ->
    Number(getInts(exprs, env).fold(0) { a, b -> a + b })
}

val sub = Fn("Sub") { exprs, env ->
    Number(getInts(exprs, env).fold(0) { a, b -> a - b })
}

val mul = Fn("Mul") { exprs, env ->
    Number(getInts(exprs, env).fold(1) { a, b -> a * b })
}
