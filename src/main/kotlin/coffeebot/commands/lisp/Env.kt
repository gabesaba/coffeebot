package coffeebot.commands.lisp

class Env(private val parent: Env? = null, private val registry: MutableMap<String, LispObject>) {

    fun find(symbol: String): LispObject? {
        return registry[symbol.toLowerCase()]
    }

    fun set(symbol: String, lispObject: LispObject) {
        registry[symbol.toLowerCase()] = lispObject
    }

    fun isGlobalEnv() = parent == null
}

val globalEnv = Env(registry =  mutableMapOf(
        "+" to add,
        "-" to sub,
        "*" to mul,
        "define" to define))
