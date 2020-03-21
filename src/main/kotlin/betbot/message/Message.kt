package betbot.message

fun loadMessage(user: User?, contents: String?, handle: Handle): Message {
        return if (contents == null || user == null) {
            Invalid
        } else if (contents.startsWith('!') || "BetBot" == user.name) {
            Ignored
        } else {
            Valid(user, contents, handle)
        }
}

sealed class Message

data class Valid(val user: User, val contents: String, private val handle: Handle): Message() {
    fun reply(message: String) {
        handle.sendMessage(message)
    }
}

object Ignored: Message()

object Invalid: Message()
