package coffeebot.commands.lisp

sealed class LispObject {
    abstract fun type(): String

    abstract fun display(): String
}

class Number(val num: Int): LispObject() {
    override fun display(): String {
        return num.toString()
    }

    override fun type(): String = "Number"
}

class Fn(private val name: String, private val fn: (List<Expression>, Env) -> LispObject): LispObject() {
    fun apply(expressions: List<Expression>, env: Env) = fn(expressions, env)

    override fun type(): String = "Fn"

    override fun display() = "The Fn $name"
}

class Identifier(private val s: String): LispObject() {
    companion object {
        fun isIdentifier(s: String) = s.matches("[a-zA-Z][A-Za-z0-9]*".toRegex())
    }

    override fun type() = "Identifier"

    override fun display() = s
}

class LispString(private val s: String): LispObject() {
    override fun type() = "String"

    override fun display() = s
}

class TypeError(override val message: String): Exception()
