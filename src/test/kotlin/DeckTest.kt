package boxGame

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Тесты колоды")
class DeckTest {
    private lateinit var deck: Deck
    private val cardA = Card(Nominal.SIX, Suit.HEARTS)
    private val cardB = Card(Nominal.SEVEN, Suit.CLUBS)
    private val cardC = Card(Nominal.KING, Suit.SPADES)

    @BeforeEach
    fun setUp() { deck = Deck() }

    @Test
    @DisplayName("initialiseDeck добавляет карты")
    fun `initialiseDeck adds cards`() {
        deck.initialiseDeck(mutableListOf(cardA, cardB))
        assertEquals(2, deck.size)
    }

    @Test
    @DisplayName("deckIsEmpty возвращает true для пустой колоды")
    fun `deckIsEmpty returns true when empty`() {
        assertTrue(deck.deckIsEmpty())
    }

    @Nested
    @DisplayName("getCards")
    inner class GetCardsTests {
        @BeforeEach
        fun initDeck() {
            deck.initialiseDeck(mutableListOf(cardA, cardB, cardC))
        }

        @Test
        fun `extracts requested number of cards`() {
            val result = deck.getCards(2)
            assertEquals(2, result.size)
            assertEquals(cardA, result[0])
            assertEquals(cardB, result[1])
            assertEquals(1, deck.size) // карты удалены из колоды
        }

        @Test
        fun `returns all cards when requested more or equal size`() {
            val result = deck.getCards(5)
            assertEquals(3, result.size)
            assertTrue(deck.deckIsEmpty())
        }

        @Test
        fun `returns empty list when deck is empty`() {
            deck.getCards(3) // забираем все
            assertTrue(deck.getCards(1).isEmpty())
        }
    }
}