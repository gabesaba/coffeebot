package coffeebot.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

// Represents result of a transaction.
sealed class Result {
    object Success : Result()
    object Failure : Result()
}

/**
 * Propose a CoffeeWager.
 * @param person: Person proposing wager.
 * @param coffeesFor: Number of coffees proposer wishes to wager.
 * @param coffeesAgainst: Number of coffees proposer wishes opponent to wager.
 * @param termsOfWager: What the dang thing is about!
 */
fun proposeWager(person: String, coffeesFor: Int, coffeesAgainst: Int, termsOfWager: String): Int {
    return transaction {
        CoffeeWager.insertAndGetId {
            it[person1] = person
            it[coffees1] = coffeesFor
            it[coffees2] = coffeesAgainst
            it[terms] = termsOfWager
            it[state] = WagerState.Proposed
        }
    }.value
}

/**
 * Cancel a proposed wager.
 * @param person: Person cancelling wager.
 * @param id: Id of bet to cancel.
 */
fun cancelWager(person: String, id: Int): Result {
    return transaction {
        val wager = CoffeeWager.select {
            (CoffeeWager.id eq id)
                    .and(CoffeeWager.person1 eq person)
                    .and(CoffeeWager.state eq WagerState.Proposed)
        }
        if (wager.count() != 1L) {
            return@transaction Result.Failure
        }
        CoffeeWager.update({
            CoffeeWager.id eq id
        }) {
            it[state] = WagerState.Canceled
        }
        Result.Success
    }
}

/**
 * Accept a proposed wager.
 * @param person: Person accepting wager.
 * @param id: Id of wager to accept.
 * @return: Id of accepted wager if successful.
 */
fun acceptWager(person: String, id: Int): Result {
    return transaction {
        val wager = CoffeeWager.select {
            (CoffeeWager.id eq id)
                    .and(CoffeeWager.person1 neq person)
                    .and(CoffeeWager.state eq WagerState.Proposed)
        }
        if (wager.count() != 1L) {
            return@transaction Result.Failure
        }
        CoffeeWager.update(
                {
                    (CoffeeWager.id eq id)
                }) {
            it[person2] = person
            it[state] = WagerState.Accepted
        }
        Result.Success
    }
}

/**
 * Decide who won a wager.
 * @param id: Id of wager to adjudicate.
 * @param whoWon: Person who won the wager.
 * @return: Whether or not the wager was successfully adjudicated.
 */
fun adjudicateWager(id: Int, whoWon: String): Result {
    return transaction {
        val wager = CoffeeWager.select {
            (CoffeeWager.id eq id)
                    .and((CoffeeWager.person1 eq whoWon).or(CoffeeWager.person2 eq whoWon))
        }
        if (wager.count() != 1L) {
            return@transaction Result.Failure
        }
        CoffeeWager.update({
            (CoffeeWager.id eq id)
        }) {
            it[state] = WagerState.Completed
            it[winner] = whoWon
        }
        Result.Success
    }
}

/**
 * Get a Wager by id.
 */
fun getId(id: Int): ResultRow? {
    return transaction {
        CoffeeWager.select {
            CoffeeWager.id eq id
        }.firstOrNull()
    }
}

/**
 * Get proposed wagers, sorted by id.
 */
fun getProposals(): List<ResultRow> = getRowsInState(WagerState.Proposed)

/**
 * Get active wagers, sorted by id.
 */
fun getActiveWagers(): List<ResultRow> = getRowsInState(WagerState.Accepted)

/**
 * Get canceled wagers, sorted by id.
 */
fun getCanceledWagers(): List<ResultRow> = getRowsInState(WagerState.Canceled)

/**
 * Get completed wagers that haven't been paid off, sorted by id.
 */
fun getCompletedWagers(): List<ResultRow> = getRowsInState(WagerState.Completed)

private fun getRowsInState(state: WagerState): List<ResultRow> {
    return transaction {
        CoffeeWager.select { CoffeeWager.state eq state }.sortedBy { CoffeeWager.id }
    }
}

fun getPayments(): Iterable<OrderedPayment> = transaction {
    CoffeePayment.selectAll().map {
        OrderedPayment(it[CoffeePayment.from], it[CoffeePayment.to], it[CoffeePayment.amount])
    }
}

fun addPayment(payment: Payment): Result = transaction {
    val ordered = payment.toOrdered()
    // TODO why do I have to call `execute` here?
    val inserted = CoffeePayment.insertIgnore {
        it[from] = ordered.from
        it[to] = ordered.to
        it[amount] = ordered.amount
    }.execute(this)!!
    if (inserted == 1) {
        return@transaction Result.Success
    }
    val modified = CoffeePayment.update({ CoffeePayment.from eq ordered.from and (CoffeePayment.to eq ordered.to) }) {
        it.update(amount, Expression.build { amount.plus(ordered.amount) })
    }
    return@transaction if (modified == 1) {
        Result.Success
    } else {
        Result.Failure
    }
}

/**
 * Holds a payment (or debt, or balance) with a source user, a target user, and an amount.
 *
 * These values can't be extracted until the payment is converted to a PositivePayment or an OrderedPayment,
 * which are two ways to represent the same logical payment.
 */
interface Payment {
    fun toOrdered(): OrderedPayment
    fun toPositive(): PositivePayment

    companion object {
        fun payment(from: String, to: String, amount: Int): Payment = if (amount >= 0) {
            PositivePayment(from, to, amount)
        } else {
            PositivePayment(to, from, -amount)
        }
    }
}

/**
 * A Payment where the payment amount is non-negative.
 */
data class PositivePayment(val from: String, val to: String, val amount: Int) : Payment {
    init {
        if (amount < 0) {
            throw IllegalArgumentException("PositivePayment amount must be non-negative, got $amount")
        }
    }

    override fun toOrdered(): OrderedPayment = if (from <= to) {
        OrderedPayment(from, to, amount)
    } else {
        OrderedPayment(to, from, -amount)
    }

    override fun toPositive(): PositivePayment = this
}

/**
 * A payment where `from` is lexicographically smaller or equal to `to`. `amount` can be negative.
 */
data class OrderedPayment(val from: String, val to: String, val amount: Int) : Payment {
    init {
        if (to < from) {
            throw IllegalArgumentException("OrderedPayment `from` ($from) must be smaller than `to` ($to)")
        }
    }

    override fun toOrdered(): OrderedPayment = this

    override fun toPositive(): PositivePayment = if (amount >= 0) {
        PositivePayment(from, to, amount)
    } else {
        PositivePayment(to, from, -amount)
    }
}
