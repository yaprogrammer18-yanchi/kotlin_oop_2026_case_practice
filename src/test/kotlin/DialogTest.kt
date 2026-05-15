package boxGame

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Тесты диалога (Dialog)")
class DialogTest {
    private lateinit var asker: Player
    private lateinit var target: Player
    private lateinit var dialog: Dialog

    @BeforeEach
    fun setUp() {
        asker = Player("Asker")
        target = Player("Target")
        dialog = Dialog(asker, target)
    }

    @Test
    @DisplayName("Начальное состояние ASK_NOMINAL")
    fun `initial state is ASK_NOMINAL`() {
        assertEquals(DialogState.ASK_NOMINAL, dialog.state)
    }

    @Nested
    @DisplayName("Номинал")
    inner class NominalTests {
        @Test
        fun `questionNominal with valid card, state should be ANSWER_NOMINAL`() {
            val card = Card(Nominal.QUEEN, Suit.HEARTS)
            asker.addCardsInHand(listOf(card))

            val state = dialog.questionNominal(Nominal.QUEEN)
            assertEquals(DialogState.ANSWER_NOMINAL, state)
            assertEquals(Nominal.QUEEN, dialog.assumeNominal)
        }

        @Test
        fun `questionNominal with not valid card, state should be ASK_AGAIN`() {
            asker.addCardsInHand(listOf(Card(Nominal.SIX, Suit.HEARTS)))
            val state = dialog.questionNominal(Nominal.QUEEN)
            assertEquals(DialogState.ASK_AGAIN, state)
        }

        @Test
        fun `answerNominal YES having the card, state should be  ASK_QUANTITY`() {
            target.addCardsInHand(listOf(Card(Nominal.KING, Suit.CLUBS)))
            dialog.assumeNominal = Nominal.KING
            dialog.state = DialogState.ANSWER_NOMINAL

            val state = dialog.answerNominal(Answer.YES)
            assertEquals(DialogState.ASK_QUANTITY, state)
            assertTrue(dialog.guessedNominal)
        }

        @Test
        fun `answerNominal NO not having the card, state shoul be NOT_GUESSED`() {
            dialog.assumeNominal = Nominal.ACE
            dialog.state = DialogState.ANSWER_NOMINAL

            val state = dialog.answerNominal(Answer.NO)
            assertEquals(DialogState.NOT_GUESSED, state)
            assertFalse(dialog.guessedNominal)
        }
    }

    @Nested
    @DisplayName("Количество")
    inner class QuantityTests {
        @BeforeEach
        fun prepare() {
            target.addCardsInHand((1..3).map { Card(Nominal.SEVEN, Suit.values()[it - 1]) })
            dialog.assumeNominal = Nominal.SEVEN
            dialog.state = DialogState.ASK_QUANTITY
        }

        @Test
        fun `correct quantity & YES then state ASK_SUITS`() {
            val qState = dialog.questionQuantity(3)
            assertEquals(DialogState.ANSWER_QUANTITY, qState)

            val aState = dialog.answerQuantity(Answer.YES)
            assertEquals(DialogState.ASK_SUITS, aState)
            assertEquals(3, dialog.realQuantity)
            assertTrue(dialog.guessedQuantity)
        }

        @Test
        fun `incorrect quantity & NO then state NOT_GUESSED`() {
            dialog.questionQuantity(2)
            val aState = dialog.answerQuantity(Answer.NO)
            assertEquals(DialogState.NOT_GUESSED, aState)
            assertFalse(dialog.guessedQuantity)
        }
    }

    @Nested
    @DisplayName("Масти")
    inner class SuitsTests {
        @BeforeEach
        fun prepare() {
            val cards = listOf(
                Card(Nominal.TEN, Suit.HEARTS),
                Card(Nominal.TEN, Suit.DIAMONDS)
            )
            target.addCardsInHand(cards)
            dialog.assumeNominal = Nominal.TEN
            dialog.realQuantity = 2
            dialog.state = DialogState.ASK_SUITS
        }

        @Test
        fun `questionSuits valid size, state then ANSWER_SUITS`() {
            val state = dialog.questionSuits(listOf(Suit.HEARTS, Suit.DIAMONDS))
            assertEquals(DialogState.ANSWER_SUITS, state)
        }

        @Test
        fun `answerSuits YES and GUESSED and suitsToGive`() {
            dialog.questionSuits(listOf(Suit.HEARTS, Suit.DIAMONDS))
            val state = dialog.answerSuits(Answer.YES)

            assertEquals(DialogState.GUESSED, state)
            assertTrue(dialog.guessedSuits)
            assertEquals(2, dialog.guessedCards.size)
        }
    }

    @Test
    @DisplayName("Ошибочные вызовы в неверном состоянии -> ERROR_QUESTION")
    fun `calling methods out of order returns ERROR_QUESTION`() {
        assertEquals(DialogState.ERROR_QUESTION, dialog.questionQuantity(5))
        assertEquals(DialogState.ERROR_QUESTION, dialog.answerSuits(Answer.YES))
    }
}