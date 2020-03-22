package coffeebot.commands.lisp

sealed class LispType {
    abstract fun type(): String

    abstract fun display(): String
}

class Number(val num: Int): LispType() {
    override fun display(): String {
        return num.toString()
    }

    override fun type(): String = "Number"
}

class Fn(private val name: String, private val fn: (List<Expression>, Env) -> LispType): LispType() {
    fun apply(exprs: List<Expression>, env: Env) = fn(exprs, env)

    override fun type(): String = "Fn"

    override fun display() = "The Fn $name"
}

class TypeError(override val message: String): Exception()
