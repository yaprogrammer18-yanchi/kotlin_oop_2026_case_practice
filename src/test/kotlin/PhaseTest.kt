package boxGame

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class PhaseTest {
    @Test fun `nominalQuestion and nominalAnswer return next phase`() {
        val asker = Player("Alice")
        val target = Player("Bob")

        asker.addCardsInHand(listOf(Card(Nominal.KING, Suit.CLUBS)))
        val phase = NominalQuestion(asker, target)

        // попыталась такая смухлевать, но ничего не вышло
        val retryPhase = phase.communicate(listOf(Nominal.ACE.ordinal), asker)
        assertTrue(retryPhase is NominalQuestion)

        val nextPhase = phase.communicate(listOf(Nominal.KING.ordinal), asker)
        assertTrue(nextPhase is NominalAnswer)
    }

    @Test fun `askQuantity and answer quantity return next phase`() {
        val asker = Player("Alice")
        val target = Player("Bob")

        val qtyQuestion = QuantityQuestion(Nominal.ACE, asker, target)

        // сначала скажем неверно
        val retryQty = qtyQuestion.communicate(listOf(0), asker)
        assertTrue(retryQty is QuantityQuestion)

        // потом верно
        val freshQtyQuestion = QuantityQuestion(Nominal.ACE, asker, target)
        val nextQtyPhase = freshQtyQuestion.communicate(listOf(2), asker)
        assertTrue(nextQtyPhase is QuantityAnswer)

        target.addCardsInHand(listOf(
            Card(Nominal.ACE, Suit.HEARTS),
            Card(Nominal.ACE, Suit.SPADES)
        ))
        val qtyAnswer = QuantityAnswer(Nominal.ACE, 2, asker, target)

        val nextPhase = qtyAnswer.communicate(listOf(0), target)
        assertTrue(nextPhase is SuitsQuestion)
    }


    @Test fun `suitsQuestion returns next phase and suitsAnswer says that everything is ok`() {
        val asker = Player("Alice")
        val target = Player("Bob")
        target.addCardsInHand(listOf(
            Card(Nominal.ACE, Suit.HEARTS),
            Card(Nominal.ACE, Suit.DIAMONDS)
        ))

        val suitsQuestion = SuitsQuestion(Nominal.ACE, 2, asker, target)
        val nextPhase = suitsQuestion.communicate(listOf(1, 2), asker)
        assertTrue(nextPhase is SuitsAnswer)
        val suitsAnswer = nextPhase as SuitsAnswer
        val finalPhase = suitsAnswer.communicate(listOf(0), target)
        assertNull(finalPhase)
        assertEquals(2, suitsAnswer.getGuessedCards().size)
        assertTrue(target.hand.isEmpty())
        assertEquals(2, asker.hand.size)}
}