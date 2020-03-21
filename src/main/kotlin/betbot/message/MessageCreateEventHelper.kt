package betbot.message

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

fun MessageCreateEvent.toBetBotMessage(): Message {
    val user = this.getUser()
    val contents = this.getContents()
    return loadMessage(user, contents, DiscordHandle(this))
}


fun MessageCreateEvent.getUser(): User? {
    return this.message.author.orElse(null)?.username?.let { User(it) }
}
