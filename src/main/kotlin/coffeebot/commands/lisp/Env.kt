package coffeebot.commands.lisp

import coffeebot.commands.lisp.functions.*

class Env(private val parent: Env? = null, private val registry: MutableMap<String, LispObject> = mutableMapOf()) {
    private val level: Int = if (parent == null) {
        0
    } else {
        parent.level + 1
    }

    init {
        if (level > 10) {
            throw LispError("Exceeded stack depth")
        }
    }

    fun find(symbol: String): LispObject? {

        val symbolLower = symbol.toLowerCase()
        return registry[symbolLower]?: parent?.find(symbolLower)
    }

    fun set(symbol: String, lispObject: LispObject) {
        registry[symbol.toLowerCase()] = lispObject
    }

    fun getDefinitions(): Set<String> {
        return registry.keys.union(parent?.getDefinitions() ?: emptySet())
    }

    fun isGlobalEnv() = parent == null
}

fun initialEnv(): Env {
    return Env(registry = mutableMapOf(
            "+" to add,
            "-" to sub,
            "*" to mul,
            "define" to define,
            "type?" to type,
            "lambda" to lambda,
            "clear" to clear,
            "definitions" to definitions
    ))
}

var globalEnv: Env = initialEnv()
