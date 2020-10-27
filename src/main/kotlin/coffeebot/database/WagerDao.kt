package coffeebot.database

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

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
fun getProposals(): List<ResultRow> {
    return getRowsInState(CoffeeWager, WagerState.Proposed)
}

/**
 * Get active wagers, sorted by id.
 */
fun getActiveWagers(): List<ResultRow> {
    return getRowsInState(CoffeeWager, WagerState.Accepted)
}

/**
 * Get canceled wagers, sorted by id.
 */
fun getCanceledWagers(): List<ResultRow> {
    return getRowsInState(CoffeeWager, WagerState.Canceled)
}

private fun getRowsInState(table: Table, state: WagerState): List<ResultRow> {
    return transaction {
        table.select { CoffeeWager.state eq state }.sortedBy { CoffeeWager.id }
    }
}
