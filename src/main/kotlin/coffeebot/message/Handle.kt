package coffeebot.message

import discord4j.core.event.domain.message.MessageCreateEvent

interface Handle {
    fun sendMessage(message: String)
}

object NullHandle: Handle {
    override fun sendMessage(message: String) {}
}

object StdoutHandler: Handle {
    override fun sendMessage(message: String) {
        println("BetBot: $message")
    }
}

class DiscordHandle(private val event: MessageCreateEvent): Handle {
    override fun sendMessage(message: String) {
        event.sendMessage(message)
    }
}
