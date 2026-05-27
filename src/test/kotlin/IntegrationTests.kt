package boxGame

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class GameTests2 {

    @Test
    fun `test_initialisationDistributesCardsCorrectly`() {
        val game = Game(id = 1)
        val p1 = Player("Alice")
        val p2 = Player("Bob")
        game.addPlayer(p1)
        game.addPlayer(p2)

        val allCards = mutableListOf(
            Card(Nominal.KING, Suit.HEARTS),
            Card(Nominal.KING, Suit.DIAMONDS),
            Card(Nominal.KING, Suit.CLUBS),
            Card(Nominal.SIX, Suit.HEARTS),
            Card(Nominal.SIX, Suit.SPADES),
            Card(Nominal.KING, Suit.SPADES),)
        game.initialiseNewDeck(allCards)

        game.initialisation(cardsPerPlayer = 3)

        assertEquals(3, p1.hand.size)
        assertEquals(3, p2.hand.size)
        assertTrue(game.isEmptyDeck(), "Колода должна быть пустой после раздачи")
    }

    @Test
    fun `test_boxManagementAndHandCleanup`() {

        val player = Player("TestPlayer")
        val fourOfAKind = listOf(
            Card(Nominal.QUEEN, Suit.HEARTS),
            Card(Nominal.QUEEN, Suit.DIAMONDS),
            Card(Nominal.QUEEN, Suit.CLUBS),
            Card(Nominal.QUEEN, Suit.SPADES)
        )
        val leftovers = listOf(
            Card(Nominal.KING, Suit.HEARTS),
            Card(Nominal.KING, Suit.SPADES)
        )

        player.addCardsInHand(fourOfAKind + leftovers)

        assertEquals(1, player.quantityOfBoxes, "Должен быть засчитан 1 сундук за 4 номинала")
        assertEquals(2, player.hand.size, "В руке должны остаться только неполные комбинации")
        assertTrue(player.hand.all { it.nominal == Nominal.KING }, "В руке остались только короли")
    }}