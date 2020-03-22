package coffeebot.commands

import coffeebot.message.User
import coffeebot.message.Valid

private val betRegex = Regex("!bet (a|one|two|three|[1-3]) (?:cups? of )?coffees? that (.+)")
private val cancelRegex = Regex("!cancel ([0-9]+)")
private val acceptRegex = Regex("!accept ([0-9]+)")

private val active = mutableListOf<Active>()
private val proposals = mutableMapOf<String, Proposal>()
private var currBet = 0

data class Proposal(val requester: User, val coffee: Coffee, val subject: String) {

    val id = currBet++.toString()

    fun toActive(acceptor: User): Active {
        return Active(this.requester, acceptor, this.coffee, this.subject)
    }

    override fun toString(): String {
        return "${this.id}: ${this.requester} wants to bet ${this.coffee} that ${this.subject}"
    }
}

data class Active(val requester: User, val acceptor: User, val coffee: Coffee, val subject: String) {
    override fun toString(): String {
        return "${this.requester} has bet ${this.acceptor} ${this.coffee} that ${this.subject}"
    }
}

data class Coffee(private val num: Int) {
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

    val proposal = Proposal(message.user, coffee, groups[2])

    proposals[proposal.id] = proposal

    message.reply("Type !accept ${proposal.id} to accept ${proposal.requester}'s bet")
}

private fun String.getProposal(): Proposal? {
    return proposals[this]
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

    val proposal = groups[1].getProposal()

    when {
        proposal == null -> message.invalidId()
        proposal.requester != message.user -> message.reply("Away hacker!")
        else -> {
            proposals.remove(proposal.id)
            message.reply("Bet ${proposal.id} successfully cancelled!")
        }
    }
}

val accept = Command("!accept", "Accept a bet") { message ->
    val groups = acceptRegex.matchEntire(message.contents)?.groupValues

    if (groups == null) {
        message.reply("Invalid Syntax. Try: !accept ID")
        return@Command
    }

    val proposal = groups[1].getProposal()

    when {
        proposal == null -> message.invalidId()
        proposal.requester == message.user -> message.reply("You can't accept your own request noob")
        else -> {
            active.add(proposal.toActive(message.user))
            proposals.remove(proposal.id)
            message.reply("Congrats ${message.user}! You accepted ${proposal.requester}'s bet")
        }
    }
}

val list = Command("!list", "List bets") { message ->
    val activeStr = active.joinToString("\n") { "\t$it" }
    val proposalsStr = proposals.values.joinToString("\n") { "\t$it" }

    val reply = "Num Active Bets: ${active.size}\n$activeStr\n" +
            "Num Proposals: ${proposals.size}\n$proposalsStr\n"
    // TODO: Add completed bets once !reckon is added
    message.reply(reply)
}

// TODO: Add !reckon command to have a third party adjudicate bets
