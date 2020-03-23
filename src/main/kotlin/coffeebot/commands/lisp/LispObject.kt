package coffeebot.commands.lisp

sealed class LispObject {
    abstract fun type(): LispType

    abstract fun display(): String
}

data class Number(val num: Int): LispObject() {
    override fun display(): String {
        return num.toString()
    }

    override fun type() = LispType("Number")
}

class Fn(private val name: String, private val fn: (List<Expression>, Env) -> LispObject): LispObject() {
    fun apply(expressions: List<Expression>, env: Env) = fn(expressions, env)

    override fun type() = LispType("Fn")

    override fun display() = "The Fn $name"
}

class Identifier(private val s: String): LispObject() {
    companion object {
        fun isIdentifier(s: String) = s.matches("[a-zA-Z][A-Za-z0-9]*".toRegex())
    }

    override fun type() = LispType("Identifier")

    override fun display() = s
}

object LispUnit: LispObject() {
    override fun type() = LispType("Unit")

    override fun display() = "()"
}

data class LispType(private val name: String): LispObject() {
    override fun type() = LispType("Type")

    override fun display() = name
}


class TypeError(override val message: String): Exception()
