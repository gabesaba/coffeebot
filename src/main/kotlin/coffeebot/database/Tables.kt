package coffeebot.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

val NAME_LENGTH = 50

enum class WagerState {
    Proposed,
    Accepted,
    Canceled,
    Completed,
}

object CoffeeWager: IntIdTable() {
    val person1 = varchar("person1", NAME_LENGTH)
    val person2 = varchar("person2", NAME_LENGTH).nullable()
    val coffees1 = integer("coffees1")
    val coffees2 = integer("coffees2")
    val terms = varchar("terms", 500)
    val state = enumeration("state", WagerState::class)
    val winner = varchar("winner", NAME_LENGTH).nullable()
}

/**
 * Represents an aggregate payment in coffees between two people.
 *
 * Invariant: `from` < `to` in lexicographic order
 */
object CoffeePayment: Table() {
    val from = varchar("erson1", NAME_LENGTH)
    val to = varchar("person1", NAME_LENGTH)
    val amount = integer("amount")

    override val primaryKey = PrimaryKey(from, to)
}

fun connect(filename: String) {
    Database.connect("jdbc:sqlite:file:$filename", "org.sqlite.JDBC")

    // https://github.com/JetBrains/Exposed/wiki/Transactions
    TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_SERIALIZABLE
}

fun createTables() {
    transaction {
        SchemaUtils.create(CoffeeWager)
        SchemaUtils.create(CoffeePayment)
    }
}
