package coffeebot.commands

import coffeebot.message.Valid

interface CommandHandler {
    fun handle(valid: Valid, backfill: Boolean)

    companion object {
        fun fromHandle(handle: (Valid) -> Unit) = object : CommandHandler {
            override fun handle(valid: Valid, backfill: Boolean) {
                handle(valid)
            }
        }
    }
}

class Command(private val invoke: String, private val helpString: String, private val handler: CommandHandler)
    : CommandHandler by handler {
    constructor(invoke: String, helpString: String, handle: (Valid) -> Unit) :
        this(invoke, helpString, CommandHandler.fromHandle(handle))

    fun matches(message: Valid): Boolean {
        return message.contents.startsWith(invoke)
    }

    fun printHelp(): String {
        return "$invoke -> $helpString"
    }
}
