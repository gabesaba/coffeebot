package coffeebot.commands

import coffeebot.database.*
import coffeebot.message.Valid
import org.jetbrains.exposed.sql.ResultRow
import java.lang.StringBuilder

private val coffeeRegex = Regex("(a|one|two|three|[1-7])(?: cups?(?: of)?)?(?: coffees?)?")
private val betRegex = Regex("!bet $coffeeRegex (?:to $coffeeRegex )?that (.+)")
private val cancelRegex = Regex("!cancel ([0-9]+)")
private val acceptRegex = Regex("!accept ([0-9]+)")
private val adjudicateRegex = Regex("!adjudicate ([0-9]+) ([A-Za-z]+)")
private val payOffRegex = Regex("!pay ([0-9]+)")

private const val coffeeSuffix = "cups of coffee"

data class Coffee(val num: Int) {
    override fun toString(): String {
        return when (num) {
            1 -> "a cup of coffee"
            else -> "$num $coffeeSuffix"
        }
    }
}

val bet = Command("!bet", "Initiate a coffee bet") { message ->
    if (message.contents == "!bet show_regex") {
        message.reply("Regex: $betRegex")
        return@Command
    }
    val groups = betRegex.matchEntire(message.contents)?.groupValues
    if (groups == null) {
        message.reply("Invalid Syntax. Try \"!bet [1-7] to [1-7]" +
                " cups of coffee that X\", or type !bet show_regex")
        return@Command
    }

    fun String.toCoffee(): Coffee {
        return Coffee(when (this) {
            "a", "one"-> 1
            "two" -> 2
            "three" -> 3
            else -> this.toInt()
        })
    }

    val coffee1 = groups[1].toCoffee()
    val coffee2 = if (groups[2].isEmpty()) {
        coffee1
    } else {
        groups[2].toCoffee()
    }

    val terms = groups[3]


    val requester = message.user.name
    val id = proposeWager(requester, coffee1.num, coffee2.num, terms)

    message.reply("Type !accept ${id} to accept ${requester}'s bet")
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
        message.reply("$id successfully cancelled!")
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
        message.reply("Congrats ${message.user}! You accepted $id")
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

val payOff = Command("!pay", "Mark a bet as having been paid off") {message ->
    val idString = payOffRegex.matchEntire(message.contents)?.groupValues?.getOrNull(1)
    if (idString == null) {
        message.reply("Expecting !pay ID")
        return@Command
    }

    val id = idString.toInt()
    if (payOffWager(id) == Result.Success) {
        message.reply("Bet $id marked as paid off")
    } else {
        message.reply("Failed to mark bet $id as paid off")
    }
}

val list = Command("!list", "List bets") { message ->

    fun formatBet(row: ResultRow): String {
        val betString = StringBuilder()
        val state = row[CoffeeWager.state]
        val id = row[CoffeeWager.id]
        val person1 = row[CoffeeWager.person1]
        val person2 = row[CoffeeWager.person2]
        val bet1 = Coffee(row[CoffeeWager.coffees1])
        val bet2 = Coffee(row[CoffeeWager.coffees2])
        val terms = row[CoffeeWager.terms]
        betString.append("    $id: $person1 ")

        if (state == WagerState.Proposed) {
            betString.append("wants to bet ")
        } else {
            betString.append("bet $person2 ")
        }

        betString.append("$bet1 ")
        if (bet1 != bet2) {
            betString.append("to $bet2 ")
        }

        betString.append("that $terms")
        val winner = row[CoffeeWager.winner]
        if (state == WagerState.Completed && winner != null) {
            betString.append(" - $winner won")
        }
        return betString.toString()
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
    // TODO do we want to hide paid off wagers instead? This could declutter the lists
    reply.append(formatRows("Paid off", getPaidOffWagers()))
    message.reply(reply.toString())
}

typealias FromTo = Pair<String, String>

val totals = Command("!totals", "Show coffee debt totals") { message ->
    val wagers = getCompletedWagers()
    val totals: MutableMap<FromTo, Int> = mutableMapOf()

    for (wager in wagers) {
        val (winnerCol, loserCol, coffeesCol) =
                if (wager[CoffeeWager.winner] == wager[CoffeeWager.person1]) {
                    Triple(CoffeeWager.person1, CoffeeWager.person2, CoffeeWager.coffees1)
                } else {
                    Triple(CoffeeWager.person2, CoffeeWager.person1, CoffeeWager.coffees2)
                }
        // Make sure fromTo is in lexicographic order so that bets a -> b and b -> a can be summed
        // together.
        // Also, the non-null coercion is fine because all Completed wagers have non-null person1 and
        // person2 entries.
        val (fromTo, inverter) = orderNames(wager[loserCol]!!, wager[winnerCol]!!)
        val current = totals.getOrDefault(fromTo, 0)
        totals[fromTo] = current + wager[coffeesCol] * inverter
    }

    val reply = StringBuilder()
    for (entry in totals) {
        val (name1, name2, balance) =
                ensurePositiveBalance(entry.key.first, entry.key.second, entry.value)
        reply.append("$name1 owes $name2 $balance coffees\n")
    }
    message.reply(reply.toString())
}

/**
 * Returns a pair (fromTo, inverter) such that
 *  - the two names in fromTo are in lexicographic order
 *  - inverter is 1 if name1 and name2 are in lexicographic order too, or -1 otherwise.
 */
fun orderNames(name1: String, name2: String): Pair<FromTo, Int> =
        if (name1 < name2) {
            Pair(Pair(name1, name2), 1)
        } else {
            Pair(Pair(name2, name1), -1)
        }

/**
 * Given two names and a balance that name1 owes name2, ensures the balance is positive
 * by possibly inverting the names.
 */
fun ensurePositiveBalance(name1: String, name2: String, nCoffees: Int): Triple<String, String, Int> =
        if (nCoffees < 0) {
            Triple(name2, name1, -nCoffees)
        } else {
            Triple(name1, name2, nCoffees)
        }
