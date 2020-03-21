package betbot.commands

import betbot.database.Database
import betbot.message.Ignored
import betbot.message.Invalid
import betbot.message.Message
import betbot.message.Valid

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
                .register(cancel)
                .register(accept)
                .register(list)
                .register(help)
        this.loadDb()
    }

    fun process(message: Message) {
        println("Processing $message")
        when (message) {
            is Valid -> {
                db?.commit(message)
                dispatch(message)
                println("Successfully Processed!")
            }
            is Ignored -> {}
            is Invalid -> println("Invalid Message!!")
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
