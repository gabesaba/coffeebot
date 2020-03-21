package betbot.commands

import betbot.User
import betbot.Valid

private val betRegex = Regex("!bet ([0-9]+) that (.*)")
private val cancelRegex = Regex("!cancel ([0-9]+)")
private val acceptRegex = Regex("!accept ([0-9]+)")

private val active = mutableListOf<ActiveBet>()
private val proposals = mutableMapOf<String, BetProposal>()
private var currBet = 0

// TODO: Make betting thread safe
// TODO: Separate bets between different servers

data class BetProposal(val requester: User, val betAmount: Int, val subject: String) {

    val id = currBet++.toString()

    fun toActive(acceptor: User): ActiveBet {
        return ActiveBet(this.requester, acceptor, this.betAmount, this.subject)
    }

    override fun toString(): String {
        return "${this.id}: ${this.requester} wants to bet ${this.betAmount} that ${this.subject}"
    }
}

data class ActiveBet(val requester: User, val acceptor: User, val betAmount: Int, val subject: String) {
    override fun toString(): String {
        return "${this.requester} has bet ${this.acceptor} ${this.betAmount} that ${this.subject}"
    }
}

val bet = Command("!bet", "Initiate a bet") { message ->
    val groups = betRegex.matchEntire(message.contents)?.groupValues
    if (groups == null) {
        message.reply("Invalid Syntax. Try: !bet NATURAL_NUM that CONTENTS_OF_BET")
        return@Command
    }

    // TODO: Add unit test to make sure this handles overflow
    val amount = groups[1].toIntOrNull()?: 0

    if (amount > 7) {
        message.reply("Keep it casual.. That bet's too big!")
        return@Command
    } else if (amount < 1) {
        message.reply("Invalid bet")
        return@Command
    }

    val proposal = BetProposal(message.user, amount, groups[2])
    proposals[proposal.id] = proposal

    message.reply("Type !accept ${proposal.id} to accept ${proposal.requester}'s bet")
}

private fun String.getProposal(): BetProposal? {
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

    when  {
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

val list = Command("!list", "List bets") {message ->
    val activeStr = active.joinToString("\n")
    val proposalsStr = proposals.values.joinToString("\n")

    val reply = "Num Active Bets: ${active.size}\nNum Proposals: ${proposals.size}\n$activeStr\n$proposalsStr"
    message.reply(reply)
}
