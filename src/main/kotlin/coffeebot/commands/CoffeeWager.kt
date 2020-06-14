package coffeebot.commands

import coffeebot.database.CoffeeWager
import coffeebot.database.Result
import coffeebot.database.WagerState
import coffeebot.database.acceptWager
import coffeebot.database.cancelWager
import coffeebot.database.getActiveWagers
import coffeebot.database.getCompletedWagers
import coffeebot.database.getProposals
import coffeebot.database.adjudicateWager
import coffeebot.database.proposeWager
import coffeebot.message.Valid
import org.jetbrains.exposed.sql.ResultRow
import java.lang.StringBuilder

private val betRegex = Regex("!bet (a|one|two|three|[1-3]) (?:cups? of )?coffees? that (.+)")
private val cancelRegex = Regex("!cancel ([0-9]+)")
private val acceptRegex = Regex("!accept ([0-9]+)")
private val adjudicateRegex = Regex("!adjudicate ([0-9]+) ([A-Za-z]+)")

data class Coffee(val num: Int) {
    override fun toString(): String {
        return when (num) {
            1 -> "a cup of coffee"
            2 -> "two cups of coffee"
            3 -> "three cups of coffee"
            else -> throw IllegalStateException("Invalid num coffees")
        }
    }
}

val bet = Command("!bet", "Initiate a coffee bet") { message ->
    val groups = betRegex.matchEntire(message.contents)?.groupValues
    if (groups == null) {
        message.reply("Invalid Syntax. Try: !bet (1-3) cup(s) of coffee that XYZ")
        return@Command
    }

    val coffee = Coffee(when (groups[1]) {
        "a", "1", "one"-> 1
        "2", "two" -> 2
        "3", "three" -> 3
        else -> {
            message.reply("Invalid num cups. Try a | two | three")
            return@Command
        }
    })


    val requester = message.user.name
    val id = proposeWager(requester, coffee.num, coffee.num, groups[2])

    message.reply("Type !accept ${id} to accept ${message.user.getMentionString()}'s bet")
}

private fun Valid.invalidId() {
    this.reply("Invalid Id!")
}

val cancel = Command("!cancel", "Cancel a bet") { message ->

    val groups = cancelRegex.matchEntire(message.contents)?.groupValues

    if (groups == null) {
        message.reply("Invalid Syntax. Try: !cancel ID")
        return@Command
    }

    val id = groups[1].toInt()
    if (cancelWager(message.user.name, id) is Result.Success) {
        message.reply("$id successfully cancelled!\n${message.user.getMentionString()}")
    } else {
        message.reply("Couldn't cancel request")
    }
}

val accept = Command("!accept", "Accept a bet") { message ->
    val groups = acceptRegex.matchEntire(message.contents)?.groupValues

    if (groups == null) {
        message.reply("Invalid Syntax. Try: !accept ID")
        return@Command
    }

    val id = groups[1].toInt()

    if (acceptWager(message.user.name, id) is Result.Success) {
        message.reply("Congrats ${message.user.getMentionString()}! You accepted $id")
    } else {
        message.reply("Failed to accept $id, noob")
    }
}

val adjudicate = Command("!adjudicate", "Adjudicate a bet. !adjudicate ID WINNER") {message ->
    val groups = adjudicateRegex.matchEntire(message.contents)?.groupValues

    if (groups == null) {
        message.reply("Expecting !adjudicate ID WINNER")
        return@Command
    }
    val parts = message.contents.split(" ")
    if (parts.size != 3) {
        message.reply("Requires 2 args: ID WINNER")
    }

    val id = groups[1].toIntOrNull()
    if (id == null) {
        message.reply("Error parsing id")
        return@Command
    }
    val winner = groups[2]

    if (adjudicateWager(id, winner) == Result.Success) {
        message.reply("$winner has won $id")
    } else {
        message.reply("Failed to adjudicate bet $id")
    }
}

val list = Command("!list", "List bets") { message ->

    fun formatBet(row: ResultRow): String {
        val betString = StringBuilder()

        val id = row[CoffeeWager.id]
        val person1 = row[CoffeeWager.person1]
        betString.append("    $id. `$person1` ")


        val person2 = row[CoffeeWager.person2]
        val state = row[CoffeeWager.state]
        if (state == WagerState.Proposed) {
            betString.append("wants to bet ")
        } else {
            betString.append("bet `$person2` ")
        }

        val bet1 = Coffee(row[CoffeeWager.coffees1])
        val bet2 = Coffee(row[CoffeeWager.coffees2])
        betString.append("__$bet1")
        if (bet1 != bet2) {
            betString.append("to $bet2")
        }
        betString.append("__ ")

        val terms = row[CoffeeWager.terms]
        betString.append("that __***$terms***__")

        val res = betString.toString()
        val winner = row[CoffeeWager.winner]
        return if (state == WagerState.Completed && winner != null) {
            res.replace(winner, "$winner (winner)")
        } else {
            res
        }
    }

    fun formatRows(name: String, rows: List<ResultRow>): String {
        val rowStr = rows.joinToString("\n") {
            formatBet(it)
        }
        return "$name (${rows.size}):\n$rowStr\n"
    }

    val reply = StringBuilder()
    reply.append(formatRows("Proposals", getProposals()))
    reply.append(formatRows("Active", getActiveWagers()))
    reply.append(formatRows("Completed", getCompletedWagers()))
    reply.append(message.user.getMentionString())
    message.reply(reply.toString())
}
