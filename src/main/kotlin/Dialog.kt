package boxGame

enum class DialogState {
    // Номинал
    ASK_NOMINAL,
    ANSWER_NOMINAL,

    // Количество
    ASK_QUANTITY,
    ANSWER_QUANTITY,

    // Масти
    ASK_SUITS,
    ANSWER_SUITS,

    // Проверка и результат
    GUESSED,
    NOT_GUESSED,

    // Ошибки
    ASK_AGAIN,
    ANSWER_AGAIN,
    ERROR_QUESTION,
    ERROR
}

enum class Answer(val displayName: String){
    YES("YES"),
    NO("NO");
    companion object {
        fun fromDisplayName(displayName: String?): Answer? {
            return Answer.values().find { it.displayName.equals(displayName, ignoreCase = true) }
        }
    }

}


class Dialog(var asker: Player, var target: Player) {
    var assumeNominal: Nominal? = null
    var answerForNominal: Answer? = null
    var assumeQuantity: Int = 0
    var realQuantity: Int = 0
    var answerForQuantity: Answer? = null
    var assumeSuits: List<Suit> = listOf()
    var realSuits: List<Suit> = listOf()
    var answerForSuits: Answer? = null

    var guessedCards: List<Card> = emptyList()
    var state = DialogState.ASK_NOMINAL

    var guessedNominal = false
    var guessedQuantity = false
    var guessedSuits = false


    fun questionNominal(nominal: Nominal?): DialogState
    {
        if (state == DialogState.ASK_NOMINAL) {
            assumeNominal = nominal
            val isCorrect = checkNominalValidation(asker)
            if (isCorrect) {
                state = DialogState.ANSWER_NOMINAL
                return DialogState.ANSWER_NOMINAL
            }
            state = DialogState.ASK_NOMINAL
            return DialogState.ASK_AGAIN
        }
        return DialogState.ERROR_QUESTION
    }

    fun answerNominal(answer: Answer?): DialogState{
        answerForNominal = answer
        val haveNominal: Boolean = checkNominalValidation(target)
            if (haveNominal && (answerForNominal == Answer.YES)){
                state = DialogState.ASK_QUANTITY
                guessedNominal = true
                return DialogState.ASK_QUANTITY
            }
            if (!haveNominal && (answerForNominal == Answer.NO)){
                state = DialogState.NOT_GUESSED
                guessedNominal = false
                return DialogState.NOT_GUESSED
            }
    return DialogState.ANSWER_AGAIN
    }

    fun checkNominalValidation(player: Player): Boolean{

        val arr: List<Card> = player.getCardsByNominal(assumeNominal!!)
        return arr.isNotEmpty()
    }


    // РАБОТА С КОЛИЧЕСТВОМ
    fun questionQuantity(quantity: Int): DialogState {
        if (state != DialogState.ASK_QUANTITY) return DialogState.ERROR_QUESTION
        assumeQuantity = quantity
        state = DialogState.ANSWER_QUANTITY
        return DialogState.ANSWER_QUANTITY
    }

    fun answerQuantity(answer: Answer?): DialogState {
        if (state != DialogState.ANSWER_QUANTITY) return DialogState.ERROR_QUESTION
        answerForQuantity = answer
        val isValid = checkQuantityValidation(target, assumeQuantity)
        if (isValid && answer == Answer.YES) {
            state = DialogState.ASK_SUITS
            guessedQuantity = true
            return DialogState.ASK_SUITS
        }
        else if (isValid && answer == Answer.NO){
            guessedQuantity = false
            return DialogState.NOT_GUESSED
        }
        return DialogState.ANSWER_AGAIN
    }

    fun checkQuantityValidation(player: Player, assumquantity: Int): Boolean {
        // реальное кол-во карт нужного номинала
        val cardsCount = player.getCardsByNominal(assumeNominal!!).size
        realQuantity = cardsCount
        if (answerForQuantity == Answer.YES && cardsCount == assumquantity){
            return true
        }
        if (answerForQuantity == Answer.NO && cardsCount != assumquantity){
            return true
        }
        return false
    }


// РАБОТА С МАСТЯМИ

    fun questionSuits(suits: List<Suit>): DialogState {
        if (state != DialogState.ASK_SUITS) return DialogState.ERROR_QUESTION
        assumeSuits = suits
        val isValid = checkSuitsValidationForAsker()

        if (isValid) {
            state = DialogState.ANSWER_SUITS
            return DialogState.ANSWER_SUITS
        }
        state = DialogState.ASK_SUITS
        return DialogState.ASK_AGAIN
    }

    fun answerSuits(answer: Answer?): DialogState {
        if (state != DialogState.ANSWER_SUITS) return DialogState.ERROR_QUESTION
        answerForSuits = answer
        val isValid = checkSuitsValidationForTarget()

        if (isValid && (answer == Answer.YES)) {
            state = DialogState.GUESSED
            guessedSuits = true
            suitsToGive()
            return DialogState.GUESSED
        }

        else if (isValid && (answer == Answer.NO)){
            state = DialogState.NOT_GUESSED
            guessedSuits = false
            return DialogState.NOT_GUESSED
        }

        return DialogState.ANSWER_AGAIN
    }

    fun checkSuitsValidationForTarget(): Boolean {
        val cards = target.getCardsByNominal(assumeNominal!!).map {it.suit}
        realSuits = cards

        if (realSuits.any { it in assumeSuits } && (answerForSuits == Answer.YES)){
            return true
        }

        if (!(realSuits.any { it in assumeSuits }) && (answerForSuits == Answer.NO)){
            return true
        }
        return false
    }

    fun checkSuitsValidationForAsker(): Boolean{
        return assumeSuits.size == realQuantity
    }

    fun suitsToGive() {
        val matchedSuits = assumeSuits.intersect(realSuits).toList()
        guessedCards = target.getCardsByNominal(assumeNominal!!).filter { it.suit in matchedSuits }
    }

}