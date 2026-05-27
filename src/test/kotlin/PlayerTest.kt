package boxGame

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Тесты игрока (Player)")
class PlayerTest {
    private val player = Player("Test")

    @Nested
    @DisplayName("Управление рукой")
    inner class HandTests {
        @Test
        fun `addCardsInHand updates hand`() {
            val cards = listOf(Card(Nominal.SIX, Suit.HEARTS))
            player.addCardsInHand(cards)
            assertEquals(1, player.hand.size)
        }

        @Test
        fun `removeCardsFromHand removes specific cards`() {
            val c1 = Card(Nominal.SIX, Suit.HEARTS)
            val c2 = Card(Nominal.SEVEN, Suit.CLUBS)
            player.addCardsInHand(listOf(c1, c2))
            player.removeCardsFromHand(listOf(c1))
            assertEquals(1, player.hand.size)
            assertEquals(c2, player.hand.first())
        }

        @Test
        fun `getCardsByNominal filters correctly`() {
            val six = Card(Nominal.SIX, Suit.HEARTS)
            val seven = Card(Nominal.SEVEN, Suit.CLUBS)
            player.addCardsInHand(listOf(six, seven))
            val result = player.getCardsByNominal(Nominal.SIX)
            assertEquals(1, result.size)
            assertEquals(six, result.first())
        }
    }

    @Nested
    @DisplayName("Логика сундуков (Boxes)")
    inner class BoxLogicTests {
        @Test
        fun `4 same nominals create 1 box`() {
            val cards = (1..4).map { Card(Nominal.QUEEN, Suit.values()[it - 1]) }
            player.addCardsInHand(cards)
            assertEquals(1, player.quantityOfBoxes)
            assertTrue(player.hand.isEmpty()) // сундучки уходят из руки
        }
    }
}