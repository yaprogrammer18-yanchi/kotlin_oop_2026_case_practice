package boxGame

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Тесты игры (Game.kt) - юнит")
class GameUnitTest {

    @Nested
    @DisplayName("Инициализация")
    inner class InitializationTests {
        @Test
        fun `addPlayer works correctly`() {
            val game = Game(1)
            game.addPlayer(Player("A"))
            assertEquals(1, game.players.size)
        }

        @Test
        fun `initialisation fails without players`() {
            val game = Game(1)
            game.initialisation(4)
            assertEquals(GameStatus.WAITING, game.status)
        }

        @Test
        fun `initialisation fails with insufficient deck`() {
            val game = Game(1)
            game.addPlayer(Player("A"))
            game.addPlayer(Player("B"))
            game.initialiseNewDeck(mutableListOf(Card(Nominal.SIX, Suit.HEARTS))) // 1 карта, нужно 8
            game.initialisation(4)
            assertEquals(GameStatus.WAITING, game.status)
        }

        @Test
        fun `initialisation distributes cards and starts game`() {
            val game = Game(1)
            val p1 = Player("A")
            val p2 = Player("B")
            game.addPlayer(p1)
            game.addPlayer(p2)
            val deck = mutableListOf<Card>()
            repeat(8) { deck.add(Card(Nominal.values()[it % 9], Suit.values()[it % 4])) }
            game.initialiseNewDeck(deck)
            game.initialisation(4)

            assertEquals(GameStatus.IN_PROGRESS, game.status)
            assertEquals(4, p1.hand.size)
            assertEquals(4, p2.hand.size)
            assertTrue(game.mainDeck.deckIsEmpty())
        }
    }

    @Test
    @DisplayName("forceEndGame sets FINISHED")
    fun `forceEndGame changes status`() {
        val game = Game(1)
        game.addPlayer(Player("Test"))
        game.initialiseNewDeck(mutableListOf())
        game.forceEndGame()
        assertEquals(GameStatus.FINISHED, game.status)
    }
}