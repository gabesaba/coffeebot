package betbot

import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent

fun MessageCreateEvent.getContents(): String? {
    return this.message.content.orElse(null)
}

fun MessageChannel.sendMessage(message: String) {
    this.createMessage(message).block()
}

fun MessageCreateEvent.sendMessage(message: String) {
    this.getChannel()?.sendMessage(message)
}

fun MessageCreateEvent.getChannel(): MessageChannel? {
    return message.channel.block()
}
