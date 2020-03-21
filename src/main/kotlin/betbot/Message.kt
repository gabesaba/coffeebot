package betbot

import discord4j.core.event.domain.message.MessageCreateEvent

interface Handle {
    fun sendMessage(message: String)
}

class StdoutHandle: Handle {
    override fun sendMessage(message: String) {
        println(message)
    }
}

class DiscordHandle(private val event: MessageCreateEvent): Handle {
    override fun sendMessage(message: String) {
        event.sendMessage(message)
    }
}

fun MessageCreateEvent.toBetBotMessage(): Message {
    val message = this.getContents()
    val user = this.getUser()

    return if (message == null || user == null) {
        Invalid
    } else {
        Valid(user, message, DiscordHandle(this))
    }
}

sealed class Message


class Valid(val user: User, val contents: String, private val handle: Handle): Message() {
    fun reply(message: String) {
        handle.sendMessage(message)
    }
}

object Invalid: Message()
