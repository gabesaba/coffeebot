package coffeebot.commands.lisp

sealed class LispObject {
    abstract fun type(): LispType

    abstract fun display(): String
}

data class LispNumber(val num: Int): LispObject() {
    override fun display(): String {
        return num.toString()
    }

    override fun type() = LispType("Num")
}

sealed class LispBool(): LispObject() {
    override fun type()= LispType("Bool")

    object True: LispBool() {
        override fun display() = "#t"
    }

    object False: LispBool() {
        override fun display() = "#f"
    }
}

data class LispString(val s: String): LispObject() {
    override fun display() = s

    override fun type() = LispType("String")
}

class Fn(val name: String, private val fn: (List<Expression>, Env) -> LispObject): LispObject() {
    fun apply(expressions: List<Expression>, env: Env) = fn(expressions, env)

    fun register(): Pair<String, Fn> = Pair(this.name.toLowerCase(), this)

    override fun type() = LispType("Fn")

    override fun display() = "The Fn $name"
}

object LispUnit: LispObject() {
    override fun type() = LispType("Unit")

    override fun display() = "()"
}

data class LispType(private val name: String): LispObject() {
    override fun type() = LispType("Type")

    override fun display() = name

    override fun toString() = name
}


class TypeError(override val message: String): Exception()
