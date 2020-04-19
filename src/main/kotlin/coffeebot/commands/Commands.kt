package coffeebot.commands

import coffeebot.message.Passive
import coffeebot.message.Valid

class Command(private val invoke: String, private val helpString: String, val handle: (Valid) -> Unit) {
    fun matches(message: Valid): Boolean {
        return message.contents.startsWith(invoke)
    }

    fun printHelp(): String {
        return "$invoke -> $helpString"
    }
}

class PassiveCommand(val handle: (Passive) -> Unit)
