package betbot.processor

import betbot.commands.Dispatcher
import betbot.database.Database
import betbot.message.toBetBotMessage
import discord4j.common.close.CloseException
import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent

class Online(private val token: String) : MessageProcessor {

    private val db = Database("db.txt")
    private val dispatcher = Dispatcher(db)

    override fun run() {
        val client = DiscordClientBuilder(token).build()

        client.eventDispatcher.on(ReadyEvent::class.java)
                .subscribe { ready -> println("Logged in as " + ready.self.username) }

        client.eventDispatcher.on(MessageCreateEvent::class.java)
                .subscribe {
                    dispatcher.process(it.toBetBotMessage())
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
}
