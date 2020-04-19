package coffeebot.database

import coffeebot.message.NullHandle
import coffeebot.message.RepliableMessageHandle
import coffeebot.message.User
import coffeebot.message.Valid
import java.io.File

class Database(fileName: String) {
    private val db = File(fileName)

    fun commit(message: Valid) {
        db.appendText("${message.user},${message.contents}\n")
    }

    fun loadMessagesFromDb(): List<Valid> {
        val messages = mutableListOf<Valid>()
        if (db.exists()) {
            println("DB exists! Loading from file")

            db.forEachLine { line ->
                val user = line.substringBefore(',')
                val content = line.substringAfter(',')
                val msg = Valid(User(user), content, RepliableMessageHandle(NullHandle))
                messages.add(msg)
            }
        } else {
            println("DB doesn't exist")
        }
        return messages
    }
}
