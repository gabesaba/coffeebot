package betbot.commands

val ping = Command("!ping", "pong!") {
    it.reply("pong!")
}
