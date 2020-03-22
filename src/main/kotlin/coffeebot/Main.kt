package coffeebot

import coffeebot.processor.Offline
import coffeebot.processor.Online

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Expecting exactly 1 arg: offline|BOT_TOKEN")
        return
    }

    if (args[0].toLowerCase() == "offline") {
        Offline().run()
    } else {
        val token = args[0]
        Online(token).run()
    }
}
