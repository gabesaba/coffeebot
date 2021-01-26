package database

import coffeebot.commands.pay
import coffeebot.database.*
import org.junit.Test
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class WagerDaoTest {
    private val testDb = "coffeebot-test.db"
    private val defaultTerms = "That I the coffee-counting system definitely doesn't work"

    private val caboose = "caboose"
    private val ishansAnnoyingUsername = "iiiiiiiiiiiiiiiiiiiivash"
    private val jason = "jason"
    private val zale = "zale"

    @BeforeTest
    fun before() {
        File(testDb).delete()
        connect(testDb)
        createTables()
    }

    @AfterTest
    fun after() {
        File(testDb).delete()
    }

    @Test
    fun testBalanceWagersOnly() {
        var id = proposeWager(zale, 3, 5, defaultTerms)
        acceptWager(caboose, id)
        adjudicateWager(id, zale)

        id = proposeWager(caboose, 1, 4, "yeah IDK something I would win")
        acceptWager(zale, id)
        adjudicateWager(id, caboose)

        id = proposeWager(ishansAnnoyingUsername, 2, 1, "bernie wins the primary")
        acceptWager(jason, id)
        adjudicateWager(id, jason)

        val payments = getBalancePayments().map { it.toPositive() }.toSet()
        assertEquals(
                // zale has won 5 coffees, and caboose has won 4, so the settlement payment is caboose -> zale 1
                setOf(PositivePayment(caboose, zale, 1), PositivePayment(ishansAnnoyingUsername, jason, 2)),
                payments
        )
    }

    @Test
    fun testBalancePaymentsOnly() {
        addPayment(Payment.payment(jason, ishansAnnoyingUsername, 2))
        addPayment(Payment.payment(jason, ishansAnnoyingUsername, 1))
        addPayment(Payment.payment(ishansAnnoyingUsername, jason, -1))
        addPayment(Payment.payment(ishansAnnoyingUsername, caboose, 1))
        val payments = getBalancePayments().map { it.toPositive() }.toSet()
        assertEquals(
                setOf(PositivePayment(ishansAnnoyingUsername, jason, 4), PositivePayment(caboose, ishansAnnoyingUsername, 1)),
                payments
        )
    }

    @Test
    fun testBalanceAggregate() {
        val id = proposeWager(zale, 3, 5, defaultTerms)
        acceptWager(caboose, id)
        adjudicateWager(id, zale)

        addPayment(Payment.payment(caboose, zale, 2))
        assertEquals(listOf(PositivePayment(caboose, zale, 3)), getBalancePayments().map { it.toPositive() })
    }
}