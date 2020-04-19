package coffeebot.message

import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.message.MessageCreateEvent

interface Handle {
    fun sendMessage(message: String)

    fun react(emoji: String)
}

object NullHandle: Handle {
    override fun sendMessage(message: String) {}

    override fun react(emoji: String) {}
}

class StdoutHandler(private val baseMessage: String): Handle {
    override fun sendMessage(message: String) {
        println("BetBot: $message")
    }

    override fun react(emoji: String) {
        println("Reacted $emoji to '$baseMessage'")
    }
}

class DiscordHandle(private val event: MessageCreateEvent): Handle {
    override fun sendMessage(message: String) {
        event.sendMessage(message)
    }

    override fun react(emoji: String) {
        event.message.addReaction(ReactionEmoji.unicode(emoji))
    }
}
