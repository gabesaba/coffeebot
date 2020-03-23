package coffeebot.commands.lisp.functions

import coffeebot.commands.lisp.Expression
import coffeebot.commands.lisp.LispError

fun valArgs(args: List<Expression>, expected: Int, fnName: String) {
    if (args.size != expected) {
        throw LispError("$fnName expected $expected args. Got ${args.size}")
    }
}
