package coffeebot

import coffeebot.database.connect
import coffeebot.database.createTables
import coffeebot.processor.Offline
import coffeebot.processor.Online
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

fun main(args: Array<String>) = Main().main(args)

class Main : CliktCommand() {
    private val botToken by argument(help = "Either the string `offline` or a Discord Token.")
    private val database by option(help = "File name of SQLite database").default("coffeebot.db")
    private val miltonSecret by option(help = "Milton bot secret. If omitted, disables Milton support.")

    override fun run() {
        connect(database)
        createTables()

        if (botToken.toLowerCase() == "offline") {
            Offline(miltonSecret).run()
        } else {
            Online(botToken, miltonSecret).run()
        }
    }
}
