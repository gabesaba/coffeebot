package betbot.commands

val invalid = Command("!invalid",
        "Invalid command. Invoked when no other command matches") {
    it.reply("Invalid command. Type !help for available commands")
}

val ping = Command("!ping", "pong!") {
    it.reply("pong!")
}
