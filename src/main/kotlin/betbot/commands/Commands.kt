package betbot.commands

import betbot.message.Valid

class Command(private val invoke: String, private val helpString: String, val handle: (Valid) -> Unit) {
    fun matches(message: Valid): Boolean {
        return message.contents.startsWith(invoke)
    }

    fun printHelp(): String {
        return "$invoke -> $helpString"
    }
}
