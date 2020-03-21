package betbot.processor

import betbot.commands.Dispatcher
import betbot.database.Database
import betbot.message.StdoutHandler
import betbot.message.User
import betbot.message.Valid
import betbot.message.loadMessage

class Offline: MessageProcessor {
    private val dispatcher = Dispatcher(Database("offline_db.txt"))

    override fun run() {
        val syntax = "Syntax:\n\tUSERNAME COMMAND"

        println("Running offline mode!\n\n$syntax")
        var line: String?
        line = readLine()
        while (line != null) {
            try {
                val user = line.substringBefore(' ')
                val msg = line.substringAfter(' ')
                dispatcher.process(loadMessage(User(user), msg, StdoutHandler))
            } catch (e: Exception) {
                println("You caused an error: ${e.message}")
                println(syntax)
            }
            line = readLine()
        }
    }
}
