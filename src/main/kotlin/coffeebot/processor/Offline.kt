package coffeebot.processor

import coffeebot.commands.Dispatcher
import coffeebot.database.Log
import coffeebot.message.StdoutHandler
import coffeebot.message.User
import coffeebot.message.loadMessage

class Offline(miltonSecret: String?): MessageProcessor {
    private val dispatcher = Dispatcher(Log("offline_log.txt"), miltonSecret)

    override fun run() {
        val syntax = "Syntax:\n\tCOMMAND | switch USERNAME"

        val userRegex = Regex("switch ([A-Za-z]+)")
        println("Running offline mode!\n\n$syntax")
        var user = "User"

        var line: String?
        line = readLine()
        while (line != null) {
            try {
                val match = userRegex.matchEntire(line)
                if (match != null) {
                    user = match.groupValues.component2()
                    println("Switching to user $user")
                } else if (line.startsWith("(")) {
                    dispatcher.process(loadMessage(User(user, user.hashCode().toLong()), "!cl $line",
                            StdoutHandler(line)))
                }
                else {
                    dispatcher.process(loadMessage(User(user, user.hashCode().toLong()), line, StdoutHandler(line)))
                }
            } catch (e: Exception) {
                println("You caused an error: ${e.message}")
                println(syntax)
            }
            line = readLine()
        }
    }
}
