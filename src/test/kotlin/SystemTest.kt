package boxGame

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class GameTests1 {

    private val originalIn = System.`in`
    private val originalOut = System.out
    private lateinit var outContent: ByteArrayOutputStream

    private fun simulateInput(vararg lines: String) {
        val input = lines.joinToString("\n")
        System.setIn(ByteArrayInputStream(input.toByteArray()))
    }

    @Test
    fun `test_successfulTurnAndCardTransfer`() {
        val game = Game(id = 2)
        val asker = Player("Asker")
        val target = Player("Target")
        game.addPlayer(asker)
        game.addPlayer(target)

        val allCards = mutableListOf(
            Card(Nominal.QUEEN, Suit.DIAMONDS),
            Card(Nominal.SEVEN, Suit.SPADES),
            Card(Nominal.QUEEN, Suit.HEARTS),
            Card(Nominal.QUEEN, Suit.SPADES),
            Card(Nominal.ACE, Suit.DIAMONDS)
        )
        game.initialiseNewDeck(allCards)
        game.initialisation(2)




        // Эмулируем ввод
        simulateInput("QUEEN", "YES", "2", "YES", "HEARTS SPADES", "YES",
            "ACE", "NO", "SEVEN", "NO", "ACE", "NO" )

        game.runGame()
        assertEquals(4, asker.hand.size, "Asker должен получить 2 угаданные карты")
        assertEquals(1, target.hand.size, "Target должен потерять 2 карты")
        assertEquals(0, asker.quantityOfBoxes, "Сундуки не должны засчитаться, т.к. 2 < 4")
    }

    @Test
    fun `test_fullGameLifecycleAndWinnerDetermination`() {
        try {
            val game = Game(id = 3)
            val p1 = Player("Violetta")
            val p2 = Player("Boris")
            game.addPlayer(p1)
            game.addPlayer(p2)

            val allCards = mutableListOf(
                Card(Nominal.KING, Suit.HEARTS),
                Card(Nominal.KING, Suit.DIAMONDS),
                Card(Nominal.KING, Suit.CLUBS),
                Card(Nominal.SIX, Suit.HEARTS),
                Card(Nominal.SIX, Suit.SPADES),
                Card(Nominal.KING, Suit.SPADES),
                Card(Nominal.SIX, Suit.DIAMONDS)
            )
            game.initialiseNewDeck(allCards)
            game.initialisation(3)

            simulateInput(
                "KING", "YES", "1", "YES", "SPADES", "YES",      // Ход 1
                "SIX", "YES", "2", "YES", "HEARTS SPADES", "YES" // Ход 2
            )

            game.runGame()

            assertEquals(GameStatus.FINISHED, game.status)
            assertNotNull(game.winner, "Победитель должен быть определён")
            assertEquals(1, game.winner?.quantityOfBoxes, "Победитель должен иметь 1 сундук")
            assertTrue(game.isEmptyDeck(), "Колода должна быть пуста")

        } finally {
            System.setIn(originalIn)
        }
    }
}