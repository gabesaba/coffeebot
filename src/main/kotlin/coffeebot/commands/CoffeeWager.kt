package coffeebot.commands

import coffeebot.database.*
import coffeebot.message.Valid
import org.jetbrains.exposed.sql.ResultRow
import java.lang.StringBuilder

private val userRegex = Regex("\\w+")
private val coffeeRegex = Regex("(a|one|two|three|[0-2]?[0-9])(?: cups?(?: of)?)?(?: coffees?)?")
private val betRegex = Regex("!bet $coffeeRegex (?:to $coffeeRegex )?that (.+)")
private val cancelRegex = Regex("!cancel ([0-9]+)")
private val acceptRegex = Regex("!accept ([0-9]+)")
private val payRegex = Regex("!pay ($userRegex) $coffeeRegex")
private val adjudicateRegex = Regex("!adjudicate ([0-9]+) ($userRegex)")

private const val coffeeSuffix = "cups of coffee"

data class Coffee(val num: Int) {
    override fun toString(): String {
        return when (num) {
            1 -> "a cup of coffee"
            else -> "$num $coffeeSuffix"
        }
    }
}

fun String.toCoffee() = Coffee(
        when (this) {
            "a", "one"-> 1
            "two" -> 2
            "three" -> 3
            else -> this.toInt()
        })

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

val pay = Command("!pay", "Register a coffee payment to another user") { message ->
    val groups = payRegex.matchEntire(message.contents)?.groupValues
    if (groups == null) {
        message.reply("Expecting !pay USER NUM_COFFEES")
        return@Command
    }

    val payeeName = groups[1]
    val amount = groups[2].toCoffee().num
    val payerName = message.user.name
    if (payerName == payeeName) {
        message.reply("Can't pay coffees to yourself")
        return@Command
    }

    if (addPayment(PositivePayment(payerName, payeeName, amount)) == Result.Success) {
        message.reply("$payerName paid $payeeName ${Coffee(amount)}")
    } else {
        message.reply("Failed to add payment between $payerName and $payeeName")
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
    message.reply(reply.toString())
}

val totals = Command("!totals", "Show coffee debt totals") { message ->
    val reply = StringBuilder()
    for (balancePayment in getBalancePayments()) {
        val total = balancePayment.toPositive()
        if (total.amount != 0) {
            reply.append("${total.from} owes ${total.to} ${total.amount} coffees\n")
        }
    }
    message.reply(reply.toString())
}
