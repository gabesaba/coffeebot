package coffeebot.message

fun loadMessage(user: User?, contents: String?, handle: Handle): Message {
    return if (contents == null || user == null) {
        Invalid
    } else if (!contents.startsWith('!') || "CoffeeBot" == user.name) {
        Passive(user, contents, RepliableMessageHandle(handle))
    } else {
        Valid(user, contents, RepliableMessageHandle(handle))
    }
}

interface RepliableMessage {
    fun reply(message: String)
    fun react(emoji: String)
}

class RepliableMessageHandle(private val handle: Handle) : RepliableMessage {
    override fun reply(message: String) {
        handle.sendMessage(message)
    }

    override fun react(emoji: String) {
        handle.react(emoji)
    }
}

sealed class Message

data class Valid(val user: User, val contents: String, private val handle: RepliableMessage): Message(),
        RepliableMessage by handle

data class Passive(val user: User, val contents: String, private val handle: RepliableMessage): Message(),
        RepliableMessage by handle

object Invalid: Message()
