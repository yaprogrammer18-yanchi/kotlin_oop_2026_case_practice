package boxGame

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DialogTest {
    @Test fun `starts with ASK_NOMINAL and transitions to ANSWER_NOMINAL`() {
        val asker = Player("Alice").apply { addCardsInHand(listOf(Card(Nominal.ACE, Suit.HEARTS))) }
        val target = Player("Bob")
        val dialog = Dialog(asker, target)
        assertEquals(DialogState.ASK_NOMINAL, dialog.getCurrentState())
        val nextState = dialog.submitInput(listOf(Nominal.ACE.ordinal), isAsker = true)
        assertEquals(DialogState.ANSWER_NOMINAL, nextState)
    }

    @Test fun `returns GUESSED scenario and checking dialog current states`() {
        val asker = Player("Alice")
        val target = Player("Bob")

        asker.addCardsInHand(listOf(Card(Nominal.QUEEN, Suit.CLUBS)))
        target.addCardsInHand(listOf(
            Card(Nominal.QUEEN, Suit.HEARTS),
            Card(Nominal.QUEEN, Suit.SPADES)
        ))
        val dialog = Dialog(asker, target)
        dialog.submitInput(listOf(Nominal.QUEEN.ordinal), isAsker = true)
        assertEquals(DialogState.ANSWER_NOMINAL, dialog.getCurrentState())
        dialog.submitInput(listOf(0), isAsker = false)
        assertEquals(DialogState.ASK_QUANTITY, dialog.getCurrentState())
        dialog.submitInput(listOf(2), isAsker = true)
        assertEquals(DialogState.ANSWER_QUANTITY, dialog.getCurrentState())
        dialog.submitInput(listOf(0), isAsker = false)
        assertEquals(DialogState.ASK_SUITS, dialog.getCurrentState())
        dialog.submitInput(listOf(1, 4), isAsker = true)
        assertEquals(DialogState.ANSWER_SUITS, dialog.getCurrentState())
        val finalState = dialog.submitInput(listOf(0), isAsker = false)

        assertEquals(DialogState.GUESSED, finalState)
        assertEquals(2, dialog.getGuessedCards().size)
        assertTrue(target.hand.isEmpty())
        assertEquals(3, asker.hand.size)
    }

    @Test fun `returns NOT_GUESSED scenario`(){
        val asker = Player("Alice")
        val target = Player("Bob")

        asker.addCardsInHand(listOf(Card(Nominal.QUEEN, Suit.CLUBS)))
        target.addCardsInHand(listOf(
            Card(Nominal.QUEEN, Suit.HEARTS),
            Card(Nominal.QUEEN, Suit.SPADES)
        ))
        val dialog = Dialog(asker, target)
        dialog.submitInput(listOf(Nominal.QUEEN.ordinal), isAsker = true)
        assertEquals(DialogState.ANSWER_NOMINAL, dialog.getCurrentState())
        dialog.submitInput(listOf(0), isAsker = false)
        assertEquals(DialogState.ASK_QUANTITY, dialog.getCurrentState())
        dialog.submitInput(listOf(2), isAsker = true)
        assertEquals(DialogState.ANSWER_QUANTITY, dialog.getCurrentState())
        dialog.submitInput(listOf(0), isAsker = false)
        assertEquals(DialogState.ASK_SUITS, dialog.getCurrentState())
        dialog.submitInput(listOf(2, 3), isAsker = true)
        assertEquals(DialogState.ANSWER_SUITS, dialog.getCurrentState())
        val finalState = dialog.submitInput(listOf(1), isAsker = false)

        assertEquals(DialogState.NOT_GUESSED, finalState)
        assertEquals(0, dialog.getGuessedCards().size)
        assertTrue(!target.hand.isEmpty())
        assertEquals(1, asker.hand.size)
    }


}