package boxGame

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GameTest {
    @Test fun `addPlayer increases player list size`() {
        val game = Game(1)
        game.addPlayer(Player("Alice"))
        game.addPlayer(Player("Bob"))
        assertEquals(2, game.players.size)
    }

    @Test fun `initialisation gives correct cards when deck is enough`() {
        val game = Game(1)
        game.addPlayer(Player("Alice"))
        game.addPlayer(Player("Bob"))
        val cards = (1..8).map { Card(Nominal.values()[it % 9], Suit.values()[it % 4]) }.toMutableList()
        game.initialiseNewDeck(cards)
        game.initialisation(4)
        assertEquals(4, game.players[0].hand.size)
        assertEquals(4, game.players[1].hand.size)
        assertEquals(GameStatus.IN_PROGRESS, game.status)
    }

    @Test fun `initialisation correctly reduces deck size and changes status`() {
        val game = Game(1)
        game.addPlayer(Player("Alice"))
        game.addPlayer(Player("Bob"))
        val cards = (1..10).map { Card(Nominal.values()[it % 9], Suit.values()[it % 4]) }.toMutableList()
        game.initialiseNewDeck(cards)
        game.initialisation(4)
        assertEquals(2, game.mainDeck.size)
        assertEquals(GameStatus.IN_PROGRESS, game.status)
    }

}