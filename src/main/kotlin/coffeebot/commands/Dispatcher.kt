package coffeebot.commands

import coffeebot.commands.lisp.lisp
import coffeebot.database.Database
import coffeebot.message.Ignored
import coffeebot.message.Invalid
import coffeebot.message.Message
import coffeebot.message.Valid

// Each Discord server should probably get its own Dispatcher so it has its own state
// TODO: Move state from Bet into here
class Dispatcher(private val db: Database?) {

    private val registered = mutableListOf<Command>()

    // TODO: Figure out how to represent help. This feels sloppy
    private val help = Command("!help", "Invokes help") {
        val helpString = registered.joinToString("\n\t") { command -> command.printHelp() }
        it.reply("Commands:\n\t$helpString")
    }

    init {
        this.register(ping)
                .register(bet)
                .register(accept)
                .register(cancel)
                .register(list)
                .register(lisp)
                .register(source)
                .register(help)
        this.loadDb()
    }

    fun process(message: Message) {
        when (message) {
            is Valid -> {
                db?.commit(message)
                dispatch(message)
            }
            is Ignored -> {}
            is Invalid -> {}
        }
    }

    fun register(command: Command): Dispatcher {
        registered.add(command)
        return this
    }

    private fun loadDb() {
        db?.loadMessagesFromDb()?.forEach {
            dispatch(it)
        }
    }

    private fun dispatch(message: Valid) {
        for (command in registered) {
            if (command.matches(message)) {
                command.handle(message)
                return
            }
        }
        invalid.handle(message)
    }
}
