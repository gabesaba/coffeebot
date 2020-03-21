package betbot.database

import betbot.message.NullHandle
import betbot.message.User
import betbot.message.Valid
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
                val msg = Valid(User(user), content, NullHandle)
                messages.add(msg)
            }
        } else {
            println("DB doesn't exist")
        }
        return messages
    }
}
