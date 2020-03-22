package coffeebot.commands.lisp

sealed class LispType {
    abstract fun type(): String
}

class Number(val num: Int): LispType() {
    override fun toString(): String {
        return num.toString()
    }

    override fun type(): String = "Number"
}

class Fn(private val fn: (List<Expression>, Env) -> LispType): LispType() {
    fun apply(exprs: List<Expression>, env: Env) = fn(exprs, env)

    override fun type(): String = "Fn"
}

class TypeError(override val message: String): Exception()
