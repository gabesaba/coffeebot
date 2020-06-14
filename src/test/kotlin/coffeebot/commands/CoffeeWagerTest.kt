package coffeebot.commands

import coffeebot.database.CoffeeWager
import coffeebot.database.CoffeeWager.coffees1
import coffeebot.database.CoffeeWager.coffees2
import coffeebot.database.CoffeeWager.person1
import coffeebot.database.CoffeeWager.person2
import coffeebot.database.CoffeeWager.state
import coffeebot.database.CoffeeWager.winner
import coffeebot.database.Result
import coffeebot.database.WagerState
import coffeebot.database.acceptWager
import coffeebot.database.cancelWager
import coffeebot.database.connect
import coffeebot.database.createTables
import coffeebot.database.getActiveWagers
import coffeebot.database.getCanceledWagers
import coffeebot.database.getCompletedWagers
import coffeebot.database.getId
import coffeebot.database.getProposals
import coffeebot.database.adjudicateWager
import coffeebot.database.proposeWager
import coffeebot.message.Message
import coffeebot.message.NullHandle
import coffeebot.message.RepliableMessageHandle
import coffeebot.message.User
import coffeebot.message.Valid
import org.jetbrains.exposed.sql.ResultRow
import org.junit.Test
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CoffeeWagerTest {

    private val testDb = "coffeebot-test.db"

    private val defaultTerms = "That I lose this wager!"

    @BeforeTest
    fun before() {
        connect(testDb)
        createTables()
    }

    @AfterTest
    fun after() {
        File(testDb).delete()
    }

    @Test
    fun testInitiateWager() {
        proposeWager("Gabe", 1, 2, defaultTerms)

        val proposals = getProposals()
        assertEquals(1, proposals.size)
        assertEquals("Gabe", proposals.first()[person1])
        assertEquals(1, proposals.first()[coffees1])
        assertEquals(2, proposals.first()[coffees2])
    }

    @Test
    fun testCancelWager() {
        val id = proposeWager("Gabe", 1, 2, defaultTerms)
        assertEquals(1, getProposals().size)
        assertTrue { cancelWager("Gabe", id) is Result.Success}
        assertEquals(0, getProposals().size)
    }

    @Test
    fun testFailureToCancelOtherPersonsWager() {
        val id = proposeWager("Gabe", 1, 2, defaultTerms)
        assertEquals(1, getProposals().size)
        assertTrue { cancelWager("Joe", id) is Result.Failure }
        assertEquals(1, getProposals().size)
    }

    @Test
    fun testFailureToCancelAcceptedWager() {
        val id = proposeWager("Gabe", 1, 2, defaultTerms)
        assertTrue { acceptWager("Joe", id) is Result.Success }
        assertTrue { cancelWager("Gabe", id) is Result.Failure }
        assertEquals(0, getCanceledWagers().size)
    }

    @Test
    fun testAcceptWager() {
        val id = proposeWager("Gabe", 3, 1, defaultTerms)
        assertEquals(1, getProposals().size)
        assertTrue {  acceptWager("Joe", id) is Result.Success }
        assertEquals(0, getProposals().size)
        assertEquals(1, getActiveWagers().size)

        val wager = getActiveWagers().first()

        assertEquals(3, wager[coffees1])
        assertEquals(1, wager[coffees2])
        assertEquals("Gabe", wager[person1])
        assertEquals("Joe", wager[person2])
    }

    @Test
    fun testAdjudicateWagerWhenPerson1Wins() {
        val id = proposeWager("Gabe", 3, 1, defaultTerms)
        assertEquals(1, getProposals().size)
        assertEquals(0, getActiveWagers().size)
        assertEquals(0, getCompletedWagers().size)

        assertTrue {  acceptWager("Joe", id) is Result.Success }
        assertEquals(0, getProposals().size)
        assertEquals(1, getActiveWagers().size)
        assertEquals(0, getCompletedWagers().size)

        assertTrue {  adjudicateWager(id, "Gabe") is Result.Success }

        assertEquals(0, getProposals().size)
        assertEquals(0, getActiveWagers().size)
        assertEquals(1, getCompletedWagers().size)

        val res = getId(id)
        assertNotNull(res)
        assertEquals("Gabe", res[winner])
        assertEquals(WagerState.Completed, res[state])
    }

    @Test
    fun testAdjudicateWagerWhenPerson2Wins() {
        val id = proposeWager("Gabe", 3, 1, defaultTerms)
        assertEquals(1, getProposals().size)
        assertEquals(0, getActiveWagers().size)
        assertEquals(0, getCompletedWagers().size)

        assertTrue {  acceptWager("Joe", id) is Result.Success }
        assertEquals(0, getProposals().size)
        assertEquals(1, getActiveWagers().size)
        assertEquals(0, getCompletedWagers().size)

        assertTrue {  adjudicateWager(id, "Joe") is Result.Success }
        assertEquals(0, getProposals().size)
        assertEquals(0, getActiveWagers().size)
        assertEquals(1, getCompletedWagers().size)

        val res = getId(id)
        assertNotNull(res)
        assertEquals("Joe", res[winner])
        assertEquals(WagerState.Completed, res[state])
    }

    @Test
    fun testFailToAdjudicateWhenPersonNotInBet() {
        val id = proposeWager("Gabe", 3, 1, defaultTerms)
        assertEquals(1, getProposals().size)
        assertEquals(0, getActiveWagers().size)
        assertEquals(0, getCompletedWagers().size)

        assertTrue {  acceptWager("Joe", id) is Result.Success }
        assertEquals(0, getProposals().size)
        assertEquals(1, getActiveWagers().size)
        assertEquals(0, getCompletedWagers().size)

        assertTrue {  adjudicateWager(id, "WhoDat") is Result.Failure }
        assertEquals(0, getProposals().size)
        assertEquals(1, getActiveWagers().size)
        assertEquals(0, getCompletedWagers().size)
    }

    @Test
    fun testFailureToAcceptOwnWager() {
        val wagerId = proposeWager("Gabe", 1, 1, defaultTerms)
        assertEquals(1, getProposals().size)
        assertTrue { acceptWager("Gabe", wagerId) is Result.Failure }
        assertEquals(1, getProposals().size)
        assertEquals(0, getActiveWagers().size)
    }

    @Test
    fun testFailureToAcceptNonExtantWager() {
        assertEquals(0, getProposals().size)
        assertTrue { acceptWager("Gabe", 0) is Result.Failure }
        assertEquals(0, getProposals().size)
        assertEquals(0, getActiveWagers().size)
    }

    @Test
    fun testGetIdWhenAccepted() {
        val id = proposeWager("gabe", 1, 2, defaultTerms)
        val proposal = getId(id)
        assertNotNull(proposal)
        validate(proposal, id, state = WagerState.Proposed)

        acceptWager("joe", id)
        val accepted = getId(id)
        assertNotNull(accepted)
        validate(accepted, id, person2 = "joe", state = WagerState.Accepted)
    }

    @Test
    fun testGetIdWhenCanceled() {
        val id = proposeWager("gabe", 1, 2, defaultTerms)
        val proposal = getId(id)
        assertNotNull(proposal)
        validate(proposal, id, state = WagerState.Proposed)

        cancelWager("gabe", id)
        val cancelled = getId(id)
        assertNotNull(cancelled)
        validate(cancelled, id, state = WagerState.Canceled)
    }

    @Test
    fun testFlow() {
        val dispatcher = Dispatcher(null, null)
        dispatcher.process(createMessage("gabe", "!bet 1 coffee that $defaultTerms"))
        dispatcher.process(createMessage("matt", "!accept 1"))
        dispatcher.process(createMessage("jason", "!adjudicate 1 matt"))
        assertEquals(1, getCompletedWagers().size)
        validate(getId(1), 1, "gabe", "matt",
                WagerState.Completed, 1, 1)
    }

    @Test
    fun testAsymmetricBet() {
        val dispatcher = Dispatcher(null, null)
        dispatcher.process(createMessage("gabe", "!bet 2 coffees to 3 coffees that $defaultTerms"))
        dispatcher.process(createMessage("matt", "!accept 1"))
        dispatcher.process(createMessage("jason", "!adjudicate 1 matt"))
        assertEquals(1, getCompletedWagers().size)
        validate(getId(1), 1, "gabe", "matt",
                WagerState.Completed, 2, 3)
    }

    @Test
    fun testBetWordings() {
        val dispatcher = Dispatcher(null, null)
        var expectedEntries = 0

        fun testWording(wording: String) {
            dispatcher.process(createMessage("gabe", wording))
            expectedEntries += 1
            assertEquals(expectedEntries, getProposals().size)
        }

        testWording("!bet 1 to 2 that x")
        testWording("!bet 1 coffee to 2 coffees that x")
        testWording("!bet three coffees to one that x")
        testWording("!bet three coffees to one cup of coffee that x")
        testWording("!bet 3 to two that x")
        testWording("!bet a cup to two that x")
        testWording("!bet a cup of coffee that x")
        testWording("!bet a coffee that x")
        testWording("!bet 1 coffee that x")
        testWording("!bet two cups that x")
    }

    private fun createMessage(name: String, message: String): Message {
        return Valid(User(name), message, RepliableMessageHandle(NullHandle))
    }

    private fun validate(resultRow: ResultRow?, id: Int, person1: String = "gabe",
                         person2: String? = null, state: WagerState,
                         coffees1: Int = 1, coffees2: Int = 2, terms: String = defaultTerms) {
        assertNotNull(resultRow)
        assertEquals(id, resultRow[CoffeeWager.id].value)
        assertEquals(person1, resultRow[CoffeeWager.person1])
        assertEquals(person2, resultRow[CoffeeWager.person2])
        assertEquals(coffees1, resultRow[CoffeeWager.coffees1])
        assertEquals(coffees2, resultRow[CoffeeWager.coffees2])
        assertEquals(terms, resultRow[CoffeeWager.terms])
        assertEquals(state, resultRow[CoffeeWager.state])
    }
}
