package betbot

import betbot.commands.dispatch
import betbot.commands.registerAllCommands
import discord4j.common.close.CloseException
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.DiscordClientBuilder
import java.io.File
import java.lang.Exception

// TODO: Add load from file functionality and ability to customize/omit
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

    // TODO: Factor this somewhere that can be shared with offline mode
    if ("BetBot" == message.user.name) {
        return
    }

    logMessage(message)
    println("${message.user}: ${message.contents}")
    dispatch(message)
}

fun offline() {

    val syntax = "Syntax:\n\tUSERNAME: COMMAND"

    println("Running offline mode!\n\n$syntax")
    var line: String?
    line = readLine()
    while (line != null) {
        try {
            val split = line.indexOf(": ")
            val user = line.substring(0, split)
            val msg = line.substring(split + 2)
            dispatch(Valid(User(user), msg, StdoutHandle()))
        } catch (e: Exception) {
            println("You caused an error: ${e.message}")
            println(syntax)
        }
        line = readLine()
    }
}

fun online(token: String) {
    val client = DiscordClientBuilder(token).build()

    client.eventDispatcher.on(ReadyEvent::class.java)
        .subscribe { ready -> println("Logged in as " + ready.self.username) }

    client.eventDispatcher.on(MessageCreateEvent::class.java)
            .subscribe {
                handleMessage(it)
            }

    println("Attempting to log on")
    try {
        client.login().block()
    } catch (e: CloseException) {
        if (4004 == e.closeStatus.code) {
            println("Invalid Token")
        }
    }
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Expecting exactly 1 arg: offline|BOT_TOKEN")
    }

    registerAllCommands()
    if (args[0].toLowerCase() == "offline") {
        offline()
    } else {
        val token = args[0]
        online(token)
    }
}
