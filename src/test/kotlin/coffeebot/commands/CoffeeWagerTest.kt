package coffeebot.commands

import coffeebot.database.CoffeeWager
import coffeebot.database.CoffeeWager.coffees1
import coffeebot.database.CoffeeWager.coffees2
import coffeebot.database.CoffeeWager.person1
import coffeebot.database.CoffeeWager.person2
import coffeebot.database.Result
import coffeebot.database.WagerState
import coffeebot.database.acceptWager
import coffeebot.database.cancelWager
import coffeebot.database.connect
import coffeebot.database.createTables
import coffeebot.database.getActiveWagers
import coffeebot.database.getCanceledWagers
import coffeebot.database.getId
import coffeebot.database.getProposals
import coffeebot.database.proposeWager
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

    private fun validate(resultRow: ResultRow, id: Int, person2: String? = null, state: WagerState) {
        assertEquals(id, resultRow[CoffeeWager.id].value)
        assertEquals("gabe", resultRow[person1])
        assertEquals(person2, resultRow[CoffeeWager.person2])
        assertEquals(1, resultRow[coffees1])
        assertEquals(2, resultRow[coffees2])
        assertEquals(state, resultRow[CoffeeWager.state])
    }
}
