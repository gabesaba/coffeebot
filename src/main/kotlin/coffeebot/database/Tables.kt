package coffeebot.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

enum class WagerState {
    Proposed,
    Accepted,
    Canceled,
}

object CoffeeWager: IntIdTable() {
    val person1 = varchar("person1", 50)
    val person2 = varchar("person2", 50).nullable()
    val coffees1 = integer("coffees1")
    val coffees2 = integer("coffees2")
    val terms = varchar("terms", 500)
    val state = enumeration("state", WagerState::class)
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
    }
}
