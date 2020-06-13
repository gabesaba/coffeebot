package coffeebot.database

import coffeebot.message.NullHandle
import coffeebot.message.RepliableMessageHandle
import coffeebot.message.User
import coffeebot.message.Valid
import java.io.File

class Log(fileName: String) {
    private val log = File(fileName)

    fun commit(message: Valid) {
        log.appendText("${message.user},${message.contents}\n")
    }

    fun loadMessagesFromLog(): List<Valid> {
        val messages = mutableListOf<Valid>()
        if (log.exists()) {
            println("Log exists! Loading from file")

            log.forEachLine { line ->
                val user = line.substringBefore(',')
                val content = line.substringAfter(',')
                val msg = Valid(User(user), content, RepliableMessageHandle(NullHandle))
                messages.add(msg)
            }
        } else {
            println("Log doesn't exist")
        }
        return messages
    }
}
