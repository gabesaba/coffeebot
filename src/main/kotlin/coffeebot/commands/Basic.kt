package coffeebot.commands

val invalid = Command("!invalid",
        "Invalid command. Invoked when no other command matches") {
    it.reply("Invalid command. Type !help for available commands")
}

val ping = Command("!ping", "pong!") {
    it.reply("pong!")
}

val source = Command("!source", "Source Code") {
    it.reply("https://github.com/gabesaba/coffeebot")
}
