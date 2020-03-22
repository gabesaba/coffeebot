package coffeebot.commands.lisp

class Env {
    private val registry = mutableMapOf(
            "+" to add,
            "-" to sub,
            "*" to mult
    )

    fun find(symbol: String): LispType? {
        return registry[symbol]
    }
}
