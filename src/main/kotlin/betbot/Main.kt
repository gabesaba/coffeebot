package betbot

import betbot.commands.dispatch
import betbot.commands.registerAllCommands
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.DiscordClientBuilder
import java.io.File

private val logFile = File("log.txt")

fun handleMessage(messageCreateEvent: MessageCreateEvent) {
    when (val message = messageCreateEvent.toBetBotMessage()) {
        is Invalid -> {}
        is Valid -> handleValidMessage(message)
    }
}

fun logMessage(message: Valid) {
    logFile.appendText("${message.user},${message.contents}\n")
}

fun handleValidMessage(message: Valid) {
    if ("BetBot" == message.user.name) {
        return
    }

    logMessage(message)
    println("${message.user}: ${message.contents}")
    dispatch(message)
}

fun offline() {
    var line: String?
    line = readLine()
    while (line != null) {
        dispatch(Valid(User("USER"), line, StdoutHandle()))
        line = readLine()
    }
}

fun online(token: String) {
    val client = DiscordClientBuilder(token).build()

    client.eventDispatcher.on(ReadyEvent::class.java)
        .subscribe { ready -> println("Logged in as " + ready.self.username) }

    client.eventDispatcher.on(MessageCreateEvent::class.java)
        .map { handleMessage(it) }
        .subscribe()

    client.login().block()
}

fun main(args: Array<String>) {
    val token = args[0]
    registerAllCommands()
    online(token)
}
