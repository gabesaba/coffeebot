package coffeebot.commands.lisp

class Env {
    private val registry = mutableMapOf(
            "+" to add,
            "-" to sub,
            "*" to mul
    )

    fun find(symbol: String): LispType? {
        return registry[symbol]
    }
}
