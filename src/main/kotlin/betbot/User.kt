package betbot

import discord4j.core.event.domain.message.MessageCreateEvent

data class User(val name: String) {
    override fun toString(): String {
        return this.name
    }
}

fun MessageCreateEvent.getUser(): User? {
    return this.message.author.getOptional()?.username?.let { User(it) }
}
