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
    ERROR;
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

// Dialog - менеджер Phase, он просто знает, что с ними делать
// возвращает статусы, на которые реагирует ViewModel

class Dialog(
    val asker: Player,
    val target: Player
) {
    private var currentPhase: Phase? = NominalQuestion(asker, target)
    private var resultCards: List<Card> = emptyList()
    private var isFinished = false
    private var isSuccess = false

    // Отправить ввод и получить состояние для View
    // возвращает DialogState
    fun submitInput(input: List<Int>, isAsker: Boolean = false): DialogState {
        if (currentPhase == null) return DialogState.NOT_GUESSED
        val player = if (isAsker) asker else target
        val nextPhase = currentPhase?.communicate(input, player)

        if (currentPhase is SuitsAnswer && nextPhase == null) {
            val suitsAnswer = currentPhase as SuitsAnswer
            resultCards = suitsAnswer.getGuessedCards()
            isSuccess = resultCards.isNotEmpty()
            isFinished = true
            currentPhase = null
            return if (isSuccess) DialogState.GUESSED else DialogState.NOT_GUESSED
        }

        if (nextPhase == null) {
            isFinished = true
            currentPhase = null
            return DialogState.NOT_GUESSED
        }

        currentPhase = nextPhase
        return getCurrentState()
    }

    // Какой сейчас вопрос нужно задать
    // Фазы - статус, чтобы ViewModel реагировал
    fun getCurrentState(): DialogState {
        return when (currentPhase) {
            is NominalQuestion -> DialogState.ASK_NOMINAL
            is NominalAnswer -> DialogState.ANSWER_NOMINAL
            is QuantityQuestion -> DialogState.ASK_QUANTITY
            is QuantityAnswer -> DialogState.ANSWER_QUANTITY
            is SuitsQuestion -> DialogState.ASK_SUITS
            is SuitsAnswer -> DialogState.ANSWER_SUITS
            else -> DialogState.ERROR
        }
    }

    fun getCurrentQuestion(): String {
        return when (currentPhase) {
            is QuantityQuestion -> "Сколько карт?"
            is SuitsQuestion -> "Какие масти?"
            else -> "Верно?"
        }
    }

    fun getMaxQuantity(): Int {
        return if (currentPhase is QuantityQuestion) 4 else 0
    }

    fun getAvailableSuits(): List<String> {
        return if (currentPhase is SuitsQuestion) {
            Suit.values().map { it.displayName }
        } else emptyList()
    }

    fun getGuessedCards(): List<Card> = resultCards
}