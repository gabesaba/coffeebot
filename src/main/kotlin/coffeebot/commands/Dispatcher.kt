package coffeebot.commands

import coffeebot.database.Database
import coffeebot.message.Passive
import coffeebot.message.Invalid
import coffeebot.message.Message
import coffeebot.message.Valid

// Each Discord server should probably get its own Dispatcher so it has its own state
// TODO: Move state from Bet into here
class Dispatcher(private val db: Database?, miltonSecret: String?) {

    private val registered = mutableListOf<Command>()
    private val miltonCommand: PassiveCommand? = if (miltonSecret != null) {
        MiltonClient(miltonSecret).command
    } else {
        println("No milton secret passed, not starting Milton client")
        null
    }
    private val passiveRegistered = listOfNotNull(miltonCommand)

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
            is Passive -> {
                dispatchPassive(message)
            }
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

    private fun dispatchPassive(message: Passive) {
        passiveRegistered.forEach { it.handle(message) }
    }
}
