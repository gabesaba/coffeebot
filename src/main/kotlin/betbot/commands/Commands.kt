package betbot.commands

import betbot.Valid

private val registered = mutableListOf<Command>()

class Command(private val invoke: String, private val helpString: String, val handle: (Valid) -> Unit) {
    fun matches(message: Valid): Boolean {
        return message.contents.startsWith(invoke)
    }

    fun printHelp(): String {
        return "$invoke -> $helpString"
    }
}

fun Command.register(): Command {
    registered.add(this)
    return this
}

fun registerAllCommands() {
    ping.register()
    bet.register()
    cancel.register()
    accept.register()
    list.register()
    help.register()
}
fun dispatch(message: Valid) {
    if (!message.contents.startsWith("!")) {
        return
    }

    for (command in registered) {
        if (command.matches(message)) {
            command.handle(message)
            return
        }
    }
    help.handle(message)
}

private val help = Command("!help", "Invokes help") {
    val helpString = registered.joinToString("\n\t") { command -> command.printHelp() }
    it.reply("Commands:\n\t$helpString")
}
